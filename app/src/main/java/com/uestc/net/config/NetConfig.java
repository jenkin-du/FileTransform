package com.uestc.net.config;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/28
 *     desc   : 网络配置类
 *     version: 1.0
 * </pre>
 */
public class NetConfig {

    //网络超时次数
    public static final int TIMED_OUT_COUNT = 50;

    //网络不可连接次数
    public static final int NETWORK_UNREACHABLE_COUNT = 5;

    //上传分段大小
    public static final int UPLOAD_SEGMENT = 1024 * 1024 * 5;

    //网络读超时时间 秒
    public static final int READ_IDLE_TIME = 30;

    //网络读超时时间 秒
    public static final int WRITE_IDLE_TIME = 90;

    //网络读写超时时间 秒
    public static final int ALL_IDLE_TIME = 90;
}
