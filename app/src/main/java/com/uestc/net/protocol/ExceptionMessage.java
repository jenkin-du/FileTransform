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
public enum ExceptionMessage{
    //网络不可用
    NETWORK_UNREACHABLE,
    //服务器拒绝连接
    CONNECTION_REFUSED,
    // 文件不存在
    FILE_NOT_EXIST,
    //文件校验失败
    FILE_MD5_WRONG,
    //文件加密失败
    STORAGE_PERMISSION_DENIED,
    //空间不充足
    STORAGE_NOT_ENOUGH,
}
