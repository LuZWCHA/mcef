package com.nowandfuture.mod.utilities;

public class DownloadConfig {
    public long WAIT_TIME;
    public int MAX_RETRY_COUNT;
    public String TEMP_POSTFIX;

    public DownloadConfig(long WAIT_TIME, int MAX_RETRY_COUNT, String TEMP_POSTFIX) {
        this.WAIT_TIME = WAIT_TIME;
        this.MAX_RETRY_COUNT = MAX_RETRY_COUNT;
        this.TEMP_POSTFIX = TEMP_POSTFIX;
    }

    public static DownloadConfig createDefault(){
        return new DownloadConfig(200, 3, ".temp");
    }
}
