package com.uestc.net.callback;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/11
 *     desc   : 文件传输监听器
 *     version: 1.0
 * </pre>
 */
public interface FileTransportListener {


    /**
     * 开始传输文件
     *
     * @param fileSize   文件大小
     * @param fileOffset 已传输的文件偏移量
     */
    void onBegin(long fileSize, long fileOffset);


    /**
     * 传输进度
     *
     * @param progress  传输进度
     * @param totalSize 总大小
     */
    void onProgress(double progress, long totalSize);

    /**
     * 下载完成
     *
     * @param isSuccess    是否下载成功
     * @param tempFilePath 下载的临时文件路径
     */
    void onComplete(boolean isSuccess, String tempFilePath);


    void onExceptionCaught(String exception);
}
