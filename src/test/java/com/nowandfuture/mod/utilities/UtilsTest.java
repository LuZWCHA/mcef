package com.nowandfuture.mod.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.nowandfuture.mod.utilities.httputils.DownloadConfig;
import net.montoyo.mcef.remote.Mirror;
import net.montoyo.mcef.remote.MirrorManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

class UtilsTest {
    @TempDir
    static Path temPath;
    static DownloadInfo downloadInfo;

    @BeforeAll
    @Test
    public static void onBefore() {
        //create fake file system
        System.out.println(temPath);
        String localDownloadConfigFake = "";
        //init the expect download config strings
        {
            localDownloadConfigFake =
                    "{\n \"platform\": \"win64\",\n" +
                            "  \"check_sum\": \"MD5\",\n" +
                            "  \"files\": [\n" +
                            "    {\n" +
                            "      \"path\": \"cef.pak\",\n" +
                            "      \"sum\": \"e1f7a395e47ac41cdec5d331d146eac4\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"cef_100_percent.pak\",\n" +
                            "      \"sum\": \"41529207707fd4de727ee120c6c93744\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"cef_200_percent.pak\",\n" +
                            "      \"sum\": \"da283820ddf69995ae040d67a8b90858\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"cef_extensions.pak\",\n" +
                            "      \"sum\": \"10cc99305c3cc8290bc5f032cef45729\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"chrome_elf.dll\",\n" +
                            "      \"sum\": \"5098b1d4448fc151c2d7110dad93188f\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"d3dcompiler_47.dll\",\n" +
                            "      \"sum\": \"222d020bd33c90170a8296adc1b7036a\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"devtools_resources.pak\",\n" +
                            "      \"sum\": \"3b99c33de1ee9931a1453c2358ed0673\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"icudtl.dat\",\n" +
                            "      \"sum\": \"01edb1580a7015b5440ad0cb4afd0d14\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"jcef.dll\",\n" +
                            "      \"sum\": \"51b3805f3f8c8b2650a35bb1b3796e9c\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"jcef_helper.exe\",\n" +
                            "      \"sum\": \"66426966f92584b590e7a733469a3182\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"libcef.dll\",\n" +
                            "      \"sum\": \"eb9a9cf4319b9abfea7a3682f886a013\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"libEGL.dll\",\n" +
                            "      \"sum\": \"4e2c407bb125cd0b07b3725ac925d8c8\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"libGLESv2.dll\",\n" +
                            "      \"sum\": \"7fbf4364b84912b1c3223eaea9952209\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/am.pak\",\n" +
                            "      \"sum\": \"c43e6d342c8aa3c7f318c632ca4f471d\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/ar.pak\",\n" +
                            "      \"sum\": \"e6022f840b51758398b64d82c1674773\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/bg.pak\",\n" +
                            "      \"sum\": \"ebd5fc2ab55c91922e4026c4b0db644e\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/bn.pak\",\n" +
                            "      \"sum\": \"6ef0212ba01e19c29dd5d66b11d03065\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/ca.pak\",\n" +
                            "      \"sum\": \"afa604434ad348f5612a30a1fd1919b2\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/cs.pak\",\n" +
                            "      \"sum\": \"417a88d6b7b2e846aaa04e20eff942af\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/da.pak\",\n" +
                            "      \"sum\": \"3c3c89bb95efab347bc910e83744deb2\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/de.pak\",\n" +
                            "      \"sum\": \"0dcd7bfcbaa35bad0582499401338138\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/el.pak\",\n" +
                            "      \"sum\": \"7b84400e40a360b6b0de88808b66ffba\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/en-GB.pak\",\n" +
                            "      \"sum\": \"49a24223d9f1e1cd10300a2efc52ae07\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/en-US.pak\",\n" +
                            "      \"sum\": \"f2628669188a5a4bd69f493de0d4889b\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/es-419.pak\",\n" +
                            "      \"sum\": \"f45ca94f90e57750745d131c4c2d5aca\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/es.pak\",\n" +
                            "      \"sum\": \"60e0f9a362949698faa89d2e2b8d2130\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/et.pak\",\n" +
                            "      \"sum\": \"312139c9bc4fb924525e2800c950cec4\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/fa.pak\",\n" +
                            "      \"sum\": \"7cdaeee099656abb8443f9ef63553318\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/fi.pak\",\n" +
                            "      \"sum\": \"8139a951fbbb488c39d5612e59e895b2\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/fil.pak\",\n" +
                            "      \"sum\": \"df62a25b133d1b33d8d6f10f5fc30dec\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/fr.pak\",\n" +
                            "      \"sum\": \"71b9c139d3bfa53efb5983096c3de77a\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/gu.pak\",\n" +
                            "      \"sum\": \"6b6bb955a8aeb5927e321cd8b4e80d42\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/he.pak\",\n" +
                            "      \"sum\": \"b018e0a034fff3242c597fc7818c7fc1\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/hi.pak\",\n" +
                            "      \"sum\": \"527420a136858f16fa238f37607cfe99\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/hr.pak\",\n" +
                            "      \"sum\": \"f9d09f7feb20232d9ab4ab9bc58aa8f1\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/hu.pak\",\n" +
                            "      \"sum\": \"d1385fa83ca1960e07327864284402ea\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/id.pak\",\n" +
                            "      \"sum\": \"dde1ca3620657ecfa7e94af1e33d045a\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/it.pak\",\n" +
                            "      \"sum\": \"30f3ec3c249575c3aa059b5acf08981c\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/ja.pak\",\n" +
                            "      \"sum\": \"96f429a355f97d6efccebbab6c980a1a\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/kn.pak\",\n" +
                            "      \"sum\": \"8265500ceecf31483392745c4241b61b\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/ko.pak\",\n" +
                            "      \"sum\": \"6a8b667156f57eb8e269b6299c6b59e0\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/lt.pak\",\n" +
                            "      \"sum\": \"ed600b6ec98d5a34291dc3411239a128\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/lv.pak\",\n" +
                            "      \"sum\": \"eb05c01bf6ce77dc90aa9da818e0b6a3\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/ml.pak\",\n" +
                            "      \"sum\": \"f813e6f475711ce337b5f4242eed7b51\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/mr.pak\",\n" +
                            "      \"sum\": \"a36a70c0c505886bb7c6650361fd33ad\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/ms.pak\",\n" +
                            "      \"sum\": \"6ece4fa500ab95c64bbb43f9ce160e8f\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/nb.pak\",\n" +
                            "      \"sum\": \"bf9ce9164d71d50b10addf5d3d1fb159\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/nl.pak\",\n" +
                            "      \"sum\": \"423fa0f49f68a5d97c938b6229bc2bc6\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/pl.pak\",\n" +
                            "      \"sum\": \"c13a696a4bf3cf61017ff70598474731\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/pt-BR.pak\",\n" +
                            "      \"sum\": \"443b39b744712aa266032da7b2a2f296\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/pt-PT.pak\",\n" +
                            "      \"sum\": \"96c2def1cbbbf4b33eea6b47f6722019\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/ro.pak\",\n" +
                            "      \"sum\": \"55bff56b907d3d26690f995d20fb8b34\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/ru.pak\",\n" +
                            "      \"sum\": \"72ee01d7254e4b8222c0296c6423344e\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/sk.pak\",\n" +
                            "      \"sum\": \"c365760dca8f2f15321050516db55799\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/sl.pak\",\n" +
                            "      \"sum\": \"64666aedc59c069436312b2e5cff68ad\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/sr.pak\",\n" +
                            "      \"sum\": \"f344b641160bcc45d226a4b5c9f323dd\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/sv.pak\",\n" +
                            "      \"sum\": \"1f63007ed85d17c6c5d3630b7c5d4a50\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/sw.pak\",\n" +
                            "      \"sum\": \"e2d28a0e411199e9c19ccd97013af628\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/ta.pak\",\n" +
                            "      \"sum\": \"361093a733c532601c146964108e58cc\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/te.pak\",\n" +
                            "      \"sum\": \"c314fca94a25da405837a8f9d6d5a3a8\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/th.pak\",\n" +
                            "      \"sum\": \"5ed54f5da3d5f7cb86ddedcd68fadcdd\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/tr.pak\",\n" +
                            "      \"sum\": \"83df708731c42ca5e7903cb8c0f59c8d\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/uk.pak\",\n" +
                            "      \"sum\": \"eb914d5aad3ae0ab7d85e85c7d6a298e\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/vi.pak\",\n" +
                            "      \"sum\": \"6bd1aa7290718286e0ee7780221a5b69\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/zh-CN.pak\",\n" +
                            "      \"sum\": \"a1874f33d5d2fbbf9fc097ff45dc7145\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"MCEFLocales/zh-TW.pak\",\n" +
                            "      \"sum\": \"ff4852ba9f978af39265de9990b0d52b\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"snapshot_blob.bin\",\n" +
                            "      \"sum\": \"e30b9ce0db10446bf7c61874b76f66b9\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"swiftshader/libEGL.dll\",\n" +
                            "      \"sum\": \"21ac28d0ca4831f45898c2bee93c330d\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"swiftshader/libGLESv2.dll\",\n" +
                            "      \"sum\": \"59ce553faf33a32ff563e82415435e22\"\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"path\": \"v8_context_snapshot.bin\",\n" +
                            "      \"sum\": \"b7fc54d06777e16439463a3e09716667\"\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}";
        }
        try {
            Files.createFile(new File(temPath.toString(), "cef_100_percent.pak").toPath());

            //create downloads file
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            downloadInfo = gson.fromJson(localDownloadConfigFake, DownloadInfo.class);


            File output = Paths.get(temPath.toString(), "downloads.json").toFile();
            output.createNewFile();

            try (JsonWriter writer = gson.newJsonWriter(new BufferedWriter(new FileWriter(output)));) {
                gson.toJson(downloadInfo, DownloadInfo.class, writer);
            }catch (Exception e){
                Assertions.fail(e);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail(e);
        }
    }

    @AfterAll
    public static void onAfter() {
    }

//    @Test
    public void testCollection() {
        try {
            Path parent = temPath;
            Utils.collectFiles(temPath.toString(), "MD5", path -> parent.relativize(path).startsWith("MCEFCache"), 2)
                    .ifPresent(remoteFiles -> {
                        try {
                            File file = Utils.write2ConfigFile(remoteFiles, "win64", Paths.get(temPath.toString(), "collect.json").toString());
                            Assertions.assertTrue(file.exists());
                            List<RemoteFile> remoteFileList = Utils.readFromConfigFile(file.toString());
                            Assertions.assertEquals(1, remoteFileList.size());
                            Assertions.assertEquals("cef_100_percent.pak", remoteFileList.get(0).getRemotePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Assertions.fail(e);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail(e);
        }
    }

//    @Test
    public void testMissFiles() {
        try {
            Path downloadConfigPath = Paths.get(temPath.toString(), "downloads.json");
            List<RemoteFile> remoteFileList = Utils.readFromConfigFile(downloadConfigPath.toString());

            //modify the sum to eq to fake file
            for (RemoteFile rf :
                    remoteFileList) {
                if (rf.getRemotePath().equals("cef_100_percent.pak")){
                    File realFile = new File(temPath.toString(), "cef_100_percent.pak");
                    String fakeSum = Utils.checkSum(realFile.getAbsolutePath(), rf.getMethod());
                    rf.setSum(fakeSum);
                }
            }

            List<RemoteFile> losts = Utils.collectLostFiles(temPath.toString(), remoteFileList);
            Assertions.assertEquals(losts.size(),  remoteFileList.size() - 1);

        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail(e);
        }
    }

    //    @Test
    public void testDownloadConfig() throws IOException {
        MCEFDownloader.prepareConfigsMirror();
        Path downloadConfigPath = Paths.get(temPath.toString(), "downloads.json");
        Files.deleteIfExists(downloadConfigPath);
        File file = MCEFDownloader.downloadConfigFile(downloadConfigPath.toString(), DownloadConfig.createDefault(), System.out::println);
        Assertions.assertNotNull(file);
        Assertions.assertTrue(file.exists());
        Optional<DownloadInfo> remoteInfo = Utils.readFromConfigFile2(file.getAbsolutePath());
        Assertions.assertTrue(remoteInfo.isPresent());
        Assertions.assertEquals(remoteInfo.get(), downloadInfo);
        //wrong download url
        MCEFDownloader.prepareLibsMirror();
        file = MCEFDownloader.downloadConfigFile(downloadConfigPath.toString(), DownloadConfig.createDefault(), System.out::println);
        Assertions.assertNull(file);

        MirrorManager.INSTANCE.addExtraMirrors(new Mirror("nowandfuture", MCEFDownloader.getConfigUrl(), Mirror.FLAG_FORCED | Mirror.FLAG_SECURE));
        file = MCEFDownloader.downloadConfigFile(downloadConfigPath.toString(), DownloadConfig.createDefault(), System.out::println);
        Assertions.assertNotNull(file);
    }

    //    @Test
    public void testDownloadLibs() throws IOException {
        MCEFDownloader.prepareConfigsMirror();
        Path downloadConfigPath = Paths.get(temPath.toString(), "downloads.json");
        Files.deleteIfExists(downloadConfigPath);
        File file = MCEFDownloader.downloadConfigFile(downloadConfigPath.toString(), DownloadConfig.createDefault(), System.out::println);
        Assertions.assertNotNull(file);
        Assertions.assertTrue(file.exists());
        Optional<DownloadInfo> remoteInfo = Utils.readFromConfigFile2(file.getAbsolutePath());
        Assertions.assertTrue(remoteInfo.isPresent());
        Assertions.assertEquals(remoteInfo.get(), downloadInfo);

        MCEFDownloader.prepareLibsMirror();
        //read download information first, then download miss files
        boolean flag = MCEFDownloader.downloadLibFilesBy(file, temPath.toString(), DownloadConfig.createDefault(), System.out::println);
        Assertions.assertTrue(flag);
    }

}