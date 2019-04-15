package com.uestc.net.callback;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/04/09
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface HttpListener {

    /**
     * 结果响应
     *
     * @param response 消息
     */
    void onResponse(String response);


    /**
     * 出现错误
     *
     * @param error 错误
     */
    void onError(String error);

}
