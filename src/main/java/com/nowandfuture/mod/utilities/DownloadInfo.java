package com.nowandfuture.mod.utilities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DownloadInfo implements Serializable {

    @SerializedName("platform")
    private String platform;
    @SerializedName("check_sum")
    private String checkSum;
    @SerializedName("version")
    private String version;

    @SerializedName("files")
    private List<FilesBean> files;

    public DownloadInfo(String platform, String checkSum, String version, List<FilesBean> files) {
        this.platform = platform;
        this.checkSum = checkSum;
        this.version = version;
        this.files = files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadInfo that = (DownloadInfo) o;

        if (!platform.equals(that.platform)) return false;
        if (!checkSum.equals(that.checkSum)) return false;
        return files.equals(that.files);
    }

    @Override
    public int hashCode() {
        int result = platform.hashCode();
        result = 31 * result + checkSum.hashCode();
        result = 31 * result + files.hashCode();
        return result;
    }

    public List<FilesBean> getFiles() {
        return files;
    }

    public String getPlatform() {
        return platform;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public static class FilesBean implements Serializable {

        @SerializedName("path")
        private String path;
        @SerializedName("sum")
        private String sum;

        public FilesBean(String path, String sum) {
            this.path = path;
            this.sum = sum;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilesBean filesBean = (FilesBean) o;

            if (!path.equals(filesBean.path)) return false;
            return sum.equals(filesBean.sum);
        }

        @Override
        public int hashCode() {
            int result = path.hashCode();
            result = 31 * result + sum.hashCode();
            return result;
        }

        public String getPath() {
            return path;
        }

        public String getSum() {
            return sum;
        }
    }
}
