package com.nowandfuture.mod.utilities;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.nowandfuture.mod.utilities.httputils.DownloadConfig;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.remote.Mirror;
import net.montoyo.mcef.remote.MirrorManager;
import net.montoyo.mcef.utilities.IProgressListener;
import net.montoyo.mcef.utilities.Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

public class Utils {

    public static Optional<List<RemoteFile>> collectFiles(String dir, final String method, @Nonnull Predicate<Path> skipPath, int maxDepth) throws IOException {
        File f = new File(dir);
        final List<RemoteFile> files = new ArrayList<>();
        if (f.exists() && f.isDirectory()) {
            //Files.walk() is too slow and can't catch the exceptions
            Files.walkFileTree(f.toPath(), EnumSet.noneOf(FileVisitOption.class), maxDepth, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if(!skipPath.test(dir))
                        return FileVisitResult.CONTINUE;
                    return FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Optional<RemoteFile> remoteFile = getRemoteFile(file, f, method);
                    remoteFile.ifPresent(files::add);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return files.isEmpty() ? Optional.empty() : Optional.of(files);
    }

    public static Optional<DownloadInfo> readFromConfigFile2(String savePath) throws
            IOException {
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();
        DownloadInfo downloadInfo;
        try (JsonReader reader = gson.newJsonReader(new BufferedReader(new FileReader(savePath)))) {
            downloadInfo = gson.fromJson(reader, DownloadInfo.class);

        }
        return Optional.ofNullable(downloadInfo);
    }

    public static List<RemoteFile> readFromConfigFile(String savePath) throws
            IOException {
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();

        try (JsonReader reader = gson.newJsonReader(new BufferedReader(new FileReader(savePath)))) {
            DownloadInfo downloadInfo = gson.fromJson(reader, DownloadInfo.class);
            String method = downloadInfo.getCheckSum();
            List<DownloadInfo.FilesBean> downloads = downloadInfo.getFiles();

            return downloads.stream().map(filesBean -> {

                RemoteFile remoteFile = RemoteFile.createFake();
                remoteFile.setMethod(method);
                remoteFile.setSum(filesBean.getSum());
                remoteFile.setRemotePath(filesBean.getPath());

                return remoteFile;
            }).collect(Collectors.toList());
        }catch (Exception e){
            e.printStackTrace();
        }
        return Lists.newArrayList();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File write2ConfigFile(@Nonnull List<RemoteFile> remoteFiles, String os, String savePath) throws
            IOException {
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();

        assert !remoteFiles.isEmpty();
        String checkSum = remoteFiles.get(0).getMethod();
        List<DownloadInfo.FilesBean> list = remoteFiles.stream()
                .map(remoteFile -> new DownloadInfo.FilesBean(remoteFile.getRemotePath().replace(File.separatorChar, '/'), remoteFile.getSum().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());

        File output = new File(savePath);
        output.createNewFile();

        try (JsonWriter writer = gson.newJsonWriter(new BufferedWriter(new FileWriter(output)));) {
            DownloadInfo downloadInfo = new DownloadInfo(os, checkSum, MCEF.VERSION, list);
            gson.toJson(downloadInfo, DownloadInfo.class, writer);
        }

        return output;
    }

    public static List<RemoteFile> collectLostFiles(String path, List<RemoteFile> expectList) {
        final File f = new File(path);
        return expectList.stream()
                .map(remoteFile -> {
                    Path combine = Paths.get(path, remoteFile.getRemotePath());
                    File checkFile = combine.toFile();
                    if (checkFile.exists()) {
                        return getRemoteFile(combine, f, remoteFile.getMethod())
                                .filter(toCheck -> !remoteFile.getSum().equals(toCheck.getSum()));
                    }
                    return Optional.of(remoteFile);
                }).collect(ArrayList::new,
                        (remoteFiles, remoteFile) -> remoteFile.ifPresent(remoteFiles::add),
                        (BiConsumer<List<RemoteFile>, List<RemoteFile>>) List::addAll);
    }

    public static int downloadFromSite(String siteUrl, final RemoteFile fakeFile, final File saveFile, DownloadConfig config, IProgressListener downloadListener) throws InterruptedException {
        return downloadFromSite(siteUrl, Lists.newArrayList(fakeFile), file -> saveFile, config, downloadListener);
    }

    public static int downloadFromSite(String siteUrl, List<RemoteFile> fakeFileList, Function<RemoteFile, File> saveFun, DownloadConfig config, IProgressListener downloadListener) throws InterruptedException {
        int failedCount = 0;
        int finishCount = 0;
        final int count = fakeFileList.size();
        final AtomicDouble curProgress = new AtomicDouble();
        final BlockingQueue<String> taskMessages = new LinkedBlockingDeque<>();
        IProgressListener innerProgress = new IProgressListener() {

            @Override
            public void onError(String task, Throwable d) {
                taskMessages.add(task + " download throw a error: " + d.getMessage());
            }

            @Override
            public void onProgressed(double d) {
                curProgress.set(d);
                downloadListener.onProgressed(d);
            }

            @Override
            public void onTaskChanged(String name) {
                taskMessages.add(name);
            }

            @Override
            public void onProgressEnd() {
                curProgress.set(100d);
            }
        };

        for (RemoteFile rf :
                fakeFileList) {
            downloadListener.onTaskChanged("Downloading " + rf.getRemotePath() + " (" + finishCount + "/" + count + ")");
            //try to remove the last char '/'
            siteUrl = siteUrl.endsWith("/") ? siteUrl.substring(0, siteUrl.length() - 1) : siteUrl;

            int retry = 0;
            long waitTime = config.WAIT_TIME;
            int retryTime = config.MAX_RETRY_COUNT;
            String tempPostfix = config.TEMP_POSTFIX;

            while (!download(rf, saveFun.apply(rf), tempPostfix, innerProgress)) {
                downloadListener.onTaskChanged(String.format("Download %s failed, retry %d.", rf.getRemotePath(), retry + 1));
                retry++;
                if (retry > retryTime) {
                    failedCount++;
                    downloadListener.onError(rf.getRemotePath(), new Throwable(String.format("Download failed %d times, skip!", retryTime)));
                    break;
                }
                Thread.sleep(waitTime << retry);
            }

            finishCount++;
        }

        downloadListener.onProgressEnd();
        downloadListener.onTaskChanged(String.format("Success download %d/%d files.", (finishCount - failedCount), count));

        taskMessages.clear();
        return failedCount;
    }

    public static void setupMirror(String url) {
        MirrorManager.INSTANCE.forceSetCurrent(new Mirror("nowandfuture", url, 0));
    }

    public static boolean download(RemoteFile remoteFile, File saveFile, String postfix, IProgressListener downloadListener) {
        String relativePath = remoteFile.getRemotePath();
        //To speed up development, just use monotoy download tools
        boolean flag = Util.download(relativePath, saveFile, downloadListener);
        String oldName = saveFile.getName();
        if (flag && oldName.endsWith(postfix)) {
            String saveName = oldName.substring(0, oldName.length() - postfix.length());
            File newFile = new File(saveFile.getParentFile(), saveName);
            if (newFile.exists()) {
                Util.delete(newFile);
            }
            flag = Util.rename(saveFile, saveName) != null;
        }

        return flag;
    }


    //crc
    public static long checkSumCRC32(String path) {

        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        CRC32 checksum = new CRC32();
        InputStream is;
        int length;
        try {
            is = new FileInputStream(path);
            checksum.reset();
            while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
                checksum.update(buffer, 0, length);
            }

            is.close();

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return checksum.getValue();
    }

    //MD5, SHA-1, SHA-256, SHA-384
    public static String checkSum(String path, @Nullable String method) {
        if (method == null) method = "SHA-1";
        byte[] b;
        try {
            b = Files.readAllBytes(Paths.get(path));
            byte[] hash = MessageDigest.getInstance(method).digest(b);
            return printHexBinary(hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    //MD5, SHA-1, SHA-256, SHA-384
    public static DigestInputStream getCheckSumStream(InputStream stream, String method) {
        if (method == null) method = "SHA-1";

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(method);
            stream = new DigestInputStream(stream, digest);
            return (DigestInputStream) stream;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static Optional<RemoteFile> getRemoteFile(Path path, File rootDir, String method) {
        final int BUFFER_SIZE = 1024 * 100;
        byte[] buffer = new byte[BUFFER_SIZE];
        try(DigestInputStream fis = Utils.getCheckSumStream(new FileInputStream(path.toFile()), method)) {
            if (fis != null) {
                while (fis.read(buffer, 0, BUFFER_SIZE) != -1) ;
                byte[] bytes = fis.getMessageDigest().digest();
                RemoteFile remoteFile = new RemoteFile(rootDir);
                remoteFile.setSum(printHexBinary(bytes).toLowerCase(Locale.ROOT));
                remoteFile.setRemotePath(rootDir.toPath().relativize(path).toString());
                remoteFile.setMethod(method);
                return Optional.of(remoteFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    //from DatatypeConverter by javax.xml.bind that is deprecated with Java 9 and removed with Java 11,
    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }
}
