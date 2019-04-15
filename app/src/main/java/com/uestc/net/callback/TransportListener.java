package com.uestc.net.callback;

import com.uestc.net.protocol.ExceptionMessage;
import com.uestc.net.protocol.Message;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/14
 *     desc   : 传输回调
 *     version: 1.0
 * </pre>
 */
public interface TransportListener {

    /**
     * 开始传输文件
     *
     * @param fileSize   文件大小
     * @param fileOffset 已传输的文件偏移量
     */
    void onBegin(long fileSize, long fileOffset);

    /**
     * 下载过程
     *
     * @param percentage 传输百分比
     * @param totalSize  总大小 字节
     */
    void onProgress(double percentage, long totalSize);

    /**
     * 传输结束
     */
    void onComplete(Message message);

    /**
     * 传输异常
     */
    void onExceptionCaught(ExceptionMessage exceptionMessage);

}
