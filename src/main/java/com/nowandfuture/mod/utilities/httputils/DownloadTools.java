package com.nowandfuture.mod.utilities.httputils;

import joptsimple.internal.Strings;
import okhttp3.*;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.UUID;

// TODO: 2021/7/3 use common download not a downloader dependant on MirrorManager, and to support header check and breakpoint resume.
public class DownloadTools {
    private static DownloadTools instance;
    private OkHttpClient okHttpClient;

    private DownloadTools() {
        okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(TrustAll.socketFactory(), new TrustAll.trustManager())
                .build();
    }

    public Call download(String url, String savePath, DownloadConfig config, ProgressListener downloadListener) {
        Request headReq = getHeaderRequest(url);

        long startsPoint = config.START_POINT;
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + startsPoint + "-" + (config.END_POINT == -1 ? "" : config.END_POINT))
                .build();

        Interceptor interceptor = chain -> {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), downloadListener))
                    .build();
        };

        okHttpClient = okHttpClient.newBuilder()
                .addNetworkInterceptor(interceptor)
                .build();

        Call headCall = okHttpClient.newCall(headReq);
        Call downloadCall = okHttpClient.newCall(request);
        Response response = null;
        try {
            response = headCall.execute();
        } catch (IOException e) {
            e.printStackTrace();
            downloadListener.loadfail(e.getMessage());
        }

//        String contentLengthString = response.header("Content-Length");
//        final long contentLength = contentLengthString == null ? -1 : Long.parseLong(contentLengthString);

        downloadListener.start(startsPoint);

        Response finalResponse = response;
        downloadCall.enqueue(new Callback() {
            private String fileName = getHeaderFileName(finalResponse);
            private boolean isFirst = true;

            private File getFile(boolean complete) {
                String name = Strings.EMPTY;
                switch (config.NAME_STRATEGY) {
                    case URL_NAME:
                        name = url.substring(url.lastIndexOf("/") + 1);
                        break;
                    case RENAME:
                    case ORIGIN:
                        if (!fileName.equals(Strings.EMPTY)) {
                            name = fileName;
                            break;
                        }
                    case UUID:
                        name = UUID.randomUUID().toString();
                        break;
                }

                return new File(savePath + "/" + name + (complete ? Strings.EMPTY : config.TEMP_POSTFIX));
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                downloadListener.loadfail(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                long length = response.body().contentLength();

                if (isFirst) {
                    fileName = getHeaderFileName(response);
                    isFirst = false;
                }

                File file = getFile(false);

                if (length == 0) {
                    file = getFile(true);
                    downloadListener.complete(String.valueOf(file.getAbsoluteFile()));
                    return;
                }
                downloadListener.start(length + startsPoint);

                final byte[] buff = new byte[2048];
                int len;

                try (BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(response.body()).byteStream());
                     RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd")) {

                    randomAccessFile.seek(startsPoint);
                    while ((len = bis.read(buff)) != -1) {
                        randomAccessFile.write(buff, 0, len);
                    }

                    file = getFile(true);
                    downloadListener.complete(String.valueOf(file.getAbsoluteFile()));

                } catch (Exception e) {
                    e.printStackTrace();
                    downloadListener.loadfail(e.getMessage());
                }
            }
        });

        return downloadCall;
    }

    public Request getHeaderRequest(String url) {

        return new Request.Builder()
                .url(url)
                .method("HEAD", null).build();
    }

    public static DownloadTools instance() {
        if (instance == null) {
            synchronized (DownloadTools.class) {
                if (instance == null) {
                    instance = new DownloadTools();
                }
            }
        }
        return instance;
    }

    private static String getHeaderFileName(Response response) {
        String dispositionHeader = response.header("Content-Disposition");
        if (!TextUtils.isEmpty(dispositionHeader)) {
            dispositionHeader = dispositionHeader.replace("attachment;filename=", "");
            dispositionHeader = dispositionHeader.replace("filename*=utf-8", "");
            String[] strings = dispositionHeader.split("; ");
            if (strings.length > 1) {
                dispositionHeader = strings[1].replace("filename=", "");
                dispositionHeader = dispositionHeader.replace("\"", "");
                return dispositionHeader;
            }
        }
        return Strings.EMPTY;
    }
}
