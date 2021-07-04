package com.nowandfuture.mod.utilities.httputils;

public interface ProgressListener {
    /**
     * 开始下载
     */
    void start(long max);

    /**
     * 正在下载
     */
    void loading(long readBytes, long totalBytes);

    /**
     * 下载完成
     */
    void complete(String path);

    /**
     * 请求失败
     */
    void fail(int code, String message);

    /**
     * 下载过程中失败
     */
    void loadfail(String message);
}
