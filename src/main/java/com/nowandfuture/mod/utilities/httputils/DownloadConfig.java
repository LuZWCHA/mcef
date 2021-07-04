package com.nowandfuture.mod.utilities.httputils;

import java.util.UUID;

public class DownloadConfig {
    public long WAIT_TIME;
    public int MAX_RETRY_COUNT;
    public String TEMP_POSTFIX;
    public NameStrategy NAME_STRATEGY;
    public String FILE_NAME;
    public long START_POINT;
    public long END_POINT;

    public DownloadConfig(long WAIT_TIME, int MAX_RETRY_COUNT, String TEMP_POSTFIX) {
        this.WAIT_TIME = WAIT_TIME;
        this.MAX_RETRY_COUNT = MAX_RETRY_COUNT;
        this.TEMP_POSTFIX = TEMP_POSTFIX;
        this.NAME_STRATEGY = NameStrategy.UUID;
        this.START_POINT = 0;
        this.END_POINT = -1;
        this.FILE_NAME = UUID.randomUUID().toString();
    }

    public static DownloadConfig createDefault() {
        return new DownloadConfig(200, 3, ".temp");
    }

    enum NameStrategy {
        UUID,
        ORIGIN,
        URL_NAME,
        RENAME
    }

}
