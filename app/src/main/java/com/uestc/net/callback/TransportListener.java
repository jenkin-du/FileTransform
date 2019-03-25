package com.uestc.net.callback;

import com.uestc.net.protocol.ExceptionMessage;

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
     * 下载过程
     *
     * @param percentage 传输百分比
     * @param totalSize  总大小 字节
     */
    void onProgress(double percentage, long totalSize);

    /**
     * 传输结束
     */
    void onComplete();

    /**
     * 传输异常
     */
    void onExceptionCaught(ExceptionMessage exceptionMessage);

}
