package com.nowandfuture.mod.utilities;

import com.nowandfuture.mod.utilities.httputils.DownloadConfig;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.utilities.IProgressListener;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MCEFDownloader {
    private static String DOMAIN = "http://share.nowandfuture.top/cef";
    public static String CONFIG_NAME = "downloads.json";

    public static void prepareConfigsMirror() {
        Utils.setupMirror(getConfigUrl());
    }

    public static String getConfigUrl() {
        return DOMAIN + "/config/" + MCEF.VERSION + "/" + getOSName();
    }

    public static boolean checkJcefRoot(String root) throws IOException {
        Path configPath = Paths.get(root);
        if (!configPath.toFile().exists()) {
            Files.createDirectories(configPath);
        }
        return true;
    }

    public static boolean checkLocalConfigFile(String root) {
        Path configPath = Paths.get(root, CONFIG_NAME);
        File checkFile = configPath.toFile();
        if (checkFile.isFile()) {
            try {
                Optional<DownloadInfo> downloadInfo = Utils.readFromConfigFile2(configPath.toString());
                return downloadInfo.filter(downloadInfo1 -> MCEF.VERSION.equals(downloadInfo1.getVersion())).isPresent();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static File downloadConfigFile(String savePath, DownloadConfig config, @Nonnull Consumer<String> messageConsumer) {
        RemoteFile fake = RemoteFile.createFake();
        fake.setRemotePath(CONFIG_NAME);
        File savedFile = new File(savePath);
        IProgressListener iProgressListener = new IProgressListener() {
            @Override
            public void onError(String task, Throwable d) {
                messageConsumer.accept(MessageFormat.format("Error: {0}.", d.toString()));
            }

            @Override
            public void onProgressed(double d) {
                messageConsumer.accept(MessageFormat.format("Downloading: {0}/100.", d));
            }

            @Override
            public void onTaskChanged(String name) {
                messageConsumer.accept(MessageFormat.format("Go: {0}.", name));
            }

            @Override
            public void onProgressEnd() {
                messageConsumer.accept("Downloaded information file.");
            }
        };
        long waitTime = config.WAIT_TIME;
        int retryTime = config.MAX_RETRY_COUNT;
        String tempPostfix = config.TEMP_POSTFIX;
        boolean flag;
        while (flag = !Utils.download(fake, savedFile, tempPostfix, iProgressListener)) {
            retryTime--;
            if (retryTime < 0) break;
            try {
                Thread.sleep(waitTime << retryTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            iProgressListener.onTaskChanged(MessageFormat.format("Go: Restart Downloading Task (retry {0})", config.MAX_RETRY_COUNT - retryTime));
        }

        if (!flag) {
            return savedFile;
        }

        return null;
    }

    public static void prepareLibsMirror() {
        Utils.setupMirror(getLibsUrl());
    }

    public static String getLibsUrl() {
        return DOMAIN + "/libs/" + MCEF.VERSION + "/" + getOSName();
    }

    public static boolean downloadLibFilesBy(File configFile, String root, DownloadConfig config, @Nonnull Consumer<String> messageConsumer) {
        try {
            List<RemoteFile> remoteFiles = Utils.readFromConfigFile(configFile.getAbsolutePath());
            List<RemoteFile> expFiles = Utils.collectLostFiles(root, remoteFiles);
            if (expFiles.isEmpty()) {
                messageConsumer.accept("Lib files are prepared! Skip download step.");
                return true;
            } else {
                messageConsumer.accept(MessageFormat.format("CEF: {0} files to download. Download start... ", expFiles.size()));
            }
            String url = getLibsUrl();
            int failedCount = Utils.downloadFromSite(url, expFiles, file -> new File(root + '/' + file.getRemotePath()), config, new IProgressListener() {
                @Override
                public void onError(String task, Throwable d) {
                    messageConsumer.accept(MessageFormat.format("Error: {0}.", d.toString()));
                }

                @Override
                public void onProgressed(double d) {
                    messageConsumer.accept(MessageFormat.format("Downloading: {0}/100.", d));
                }

                @Override
                public void onTaskChanged(String name) {
                    messageConsumer.accept(MessageFormat.format("Go: {0}.", name));
                }

                @Override
                public void onProgressEnd() {
                    messageConsumer.accept("Downloaded information file.");
                }
            });

            if (failedCount == 0) {
                messageConsumer.accept("downloaded success!");
                return true;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private enum OSType {
        OSUndefined,
        OSLinux,
        OSWindows,
        OSMacintosh,
        OSUnknown,
    }

    static OSType osType = OSType.OSUndefined;

    private static final OSType getOSType() {
        if (osType == OSType.OSUndefined) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.startsWith("windows"))
                osType = OSType.OSWindows;
            else if (os.startsWith("linux"))
                osType = OSType.OSLinux;
            else if (os.startsWith("mac"))
                osType = OSType.OSMacintosh;
            else
                osType = OSType.OSUnknown;
        }
        return osType;
    }

    public static String getOSName() {
        OSType osType = getOSType();
        String arch = System.getProperty("sun.arch.data.model");
        String os;
        switch (osType) {
            case OSLinux:
                os = "linux";
                break;
            case OSWindows:
                os = "win";
                break;
            case OSMacintosh:
                os = "mac";
                break;
            case OSUnknown:
            case OSUndefined:
            default:
                return null;
        }
        return os + arch;

    }
}
