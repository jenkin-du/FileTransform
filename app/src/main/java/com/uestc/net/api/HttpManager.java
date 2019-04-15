package com.uestc.net.api;

import android.util.Log;

import com.uestc.app.App;
import com.uestc.net.callback.HttpListener;
import com.uestc.net.callback.NetStateListener;
import com.uestc.net.callback.TransportListener;
import com.uestc.net.config.NetConfig;
import com.uestc.net.protocol.ExceptionMessage;
import com.uestc.net.protocol.Message;
import com.uestc.net.protocol.TimedOutReason;
import com.uestc.net.protocol.TransportClient;

import io.netty.channel.ChannelHandlerContext;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/04/09
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class HttpManager extends Thread {

    private String TAG = "HttpManager";

    private TransportClient client;
    private int unreachableCount = 0;
    private int timedOutCount = 0;

    private Message msg;
    private HttpListener httpListener;

    private boolean isFinished = false;
    private boolean exceptionHandled = false;

    public HttpManager(Message msg, HttpListener httpListener) {
        this.msg = msg;
        this.httpListener = httpListener;
    }

    private TransportListener transportListener = new TransportListener() {
        @Override
        public void onBegin(long fileSize, long fileOffset) {

        }

        @Override
        public void onProgress(double percentage, long totalSize) {

        }

        @Override
        public void onComplete(Message message) {
            Log.i(TAG, "onComplete: " + message.getResponse());
            httpListener.onResponse(message.getResponse());
        }

        @Override
        public void onExceptionCaught(ExceptionMessage exceptionMessage) {

            switch (exceptionMessage) {
                case CONNECTION_REFUSED:
                    httpListener.onError("connection refused");
                    break;
                case NETWORK_UNREACHABLE:
                    httpListener.onError("network unreachable");
                    break;
                default:
                    httpListener.onError("unknown error");
                    break;
            }
        }
    };


    /**
     * 网络状态监听器
     */
    private NetStateListener netStateListener = new NetStateListener() {

        /**
         * 超时
         *
         * @param timeOutReason 超时原因
         */
        @Override
        public void onTimedOut(TimedOutReason timeOutReason) {

            exceptionHandled = true;
            switch (timeOutReason) {
                case READ:
                    Log.i(TAG, "onReadTimeOut: READ");

                    //先停止
                    client.closeChannel();
                    Log.i(TAG, "onReadTimeOut: wait to start ");
                    waitTime(2);
                    //重启下载
                    onStart();

                    break;
                case WRITE:
                    break;

                case READ_AND_WRITE:
                    break;
            }

        }

        @Override
        public void onExceptionCaught(String exception) {

            exceptionHandled = true;
            Log.i(TAG, "NetStateListener onExceptionCaught: exception:" + exception);

            if (exception.contains("connection timed out")) {
                //网络超时
                timedOutCount++;
                //若网络不可达重试，则判断为无法连接
                if (timedOutCount > NetConfig.TIMED_OUT_COUNT) {
                    transportListener.onExceptionCaught(ExceptionMessage.NETWORK_UNREACHABLE);
                } else {
                    //先停止
                    client.closeChannel();
                    Log.i(TAG, "onExceptionCaught: wait to start");
                    //等待两秒
                    waitTime(2);
                    //重启下载
                    onStart();
                }
            }

            //服务器断开连接
            else if (exception.contains("Connection reset by peer")) {

                //先停止
                client.closeChannel();
                Log.i(TAG, "onExceptionCaught: wait to start");
                //等待两秒
                waitTime(2);
                //重启下载
                onStart();
            }

            //人为断开
            else if (exception.contains("Software caused connection abort")) {
                transportListener.onExceptionCaught(ExceptionMessage.NETWORK_UNREACHABLE);
            }

            //网络不可达
            else if (exception.contains("Network is unreachable")) {

                unreachableCount++;
                //若网络不可达重试，则判断为无法连接
                if (unreachableCount > NetConfig.NETWORK_UNREACHABLE_COUNT) {
                    transportListener.onExceptionCaught(ExceptionMessage.NETWORK_UNREACHABLE);
                } else {
                    //先停止
                    client.closeChannel();
                    Log.i(TAG, "onExceptionCaught: wait to start");
                    //等待两秒
                    waitTime(2);
                    //重启下载
                    onStart();
                }

            }

            //服务器拒绝连接
            else if (exception.contains("Connection refused")) {
                transportListener.onExceptionCaught(ExceptionMessage.CONNECTION_REFUSED);
            }

            //没有获取存储权限
            else if (exception.contains("Permission denied")) {
                transportListener.onExceptionCaught(ExceptionMessage.STORAGE_PERMISSION_DENIED);
            }

            //出现错误，重启
            else if (exception.contains("event executor terminated")) {
                //先停止
                client.closeChannel();
                //等待两秒
                waitTime(1);
                //重启下载
                onStart();
            } else {
                client.closeChannel();
                transportListener.onExceptionCaught(ExceptionMessage.UNKNOWN_EXCEPTION);
            }
        }

        @Override
        public void onComplete(ChannelHandlerContext ctx) {
            isFinished = true;
            exceptionHandled = true;
        }

        @Override
        public void onChannelInactive(ChannelHandlerContext ctx) {
            if (!isFinished && !exceptionHandled) {
                //重启下载
                onStart();
            }

        }

    };

    @Override
    public void run() {

        onStart();
    }

    /**
     * 开始传输
     */
    private void onStart() {

        try {
            client = new TransportClient(App.ip, App.port, null, transportListener, netStateListener);
            client.startConnect();


            client.transportMessage(msg);

        } catch (Exception e) {
            e.printStackTrace();

            String exception = e.getLocalizedMessage();
            netStateListener.onExceptionCaught(exception);
        }
    }

    /**
     * 等待几秒
     *
     * @param second 秒
     */
    private void waitTime(int second) {
        try {
            Thread.sleep(1000 * second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() {

        if (client != null) {
            client.closeChannel();
            client = null;
        }
    }
}
