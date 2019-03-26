package com.uestc.net.protocol;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/23
 *     desc   : 超时原因枚举
 *     version: 1.0
 * </pre>
 */
public enum TimedOutReason {
    READ,
    WRITE,
    READ_AND_WRITE
}
