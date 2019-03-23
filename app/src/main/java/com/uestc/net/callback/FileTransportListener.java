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
     * 传输进度
     *
     * @param fileId    文件id
     * @param progress  传输进度
     * @param totalSize 总大小
     */
    void onProgress(String fileId, double progress, long totalSize);

    /**
     * 下载完成
     *
     * @param fileId       唯一文件id
     * @param isSuccess    是否下载成功
     * @param tempFilePath 下载的临时文件路径
     */
    void onComplete(String fileId, boolean isSuccess, String tempFilePath);


    void onExceptionCaught(String exception);
}
