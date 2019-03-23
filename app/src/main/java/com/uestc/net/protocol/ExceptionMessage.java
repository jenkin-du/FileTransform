package com.uestc.net.protocol;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/23
 *     desc   : 异常常量类
 *     version: 1.0
 * </pre>
 */
public class ExceptionMessage {

    //网络不可用
    public static final String NETWORK_UNREACHABLE = "network unreachable";

    //服务器拒绝连接
    public static final String CONNECTION_REFUSED = "connection refused";

    // 文件不存在
    public static final String FILE_NOT_EXIST = "file is not exist";

    //文件被加锁，不可写
    public static final String FILE_LOCKED = "file is locked";
}
