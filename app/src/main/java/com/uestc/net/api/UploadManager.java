package com.uestc.net.api;

import android.util.Log;

import com.uestc.net.callback.FileTransportListener;
import com.uestc.net.callback.NetStateListener;
import com.uestc.net.callback.TransportListener;
import com.uestc.net.config.NetConfig;
import com.uestc.net.protocol.ExceptionMessage;
import com.uestc.net.protocol.Message;
import com.uestc.net.protocol.TimedOutReason;
import com.uestc.net.protocol.UploadTask;
import com.uestc.net.util.MD5Util;

import java.io.File;
import java.util.HashMap;

import io.netty.channel.ChannelHandlerContext;

import static java.lang.Thread.sleep;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class UploadManager {

    private static final String TAG = "UploadManager";

    private UploadTask task;


    private String fileName;
    private String filePath;
    private HashMap<String, String> params;
    private TransportListener transportListener;

    private boolean isFinished = false;
    private boolean exceptionHandled = false;

    public UploadManager(String fileName, String filePath, HashMap<String, String> params, TransportListener transportListener) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.params = params;
        this.transportListener = transportListener;
    }

    private int unreachableCount = 0;
    private int timedOutCount = 0;


    //文件传输监听器
    private FileTransportListener fileListener = new FileTransportListener() {

        /**
         * 开始传输文件
         *
         * @param fileSize   文件大小
         * @param fileOffset 已传输的文件偏移量
         */
        @Override
        public void onBegin(long fileSize, long fileOffset) {

            transportListener.onBegin(fileSize, fileOffset);
        }

        /**
         * 传输进度
         * @param percentage  传输进度
         * @param totalSize 总大小
         */
        @Override
        public void onProgress(double percentage, long totalSize) {
            unreachableCount = 0;
            transportListener.onProgress(percentage, totalSize);
        }

        /**
         * 传输完成
         *
         * @param isSuccess    是否下载成功
         * @param tempFilePath 下载的临时文件路径
         */
        @Override
        public void onComplete(boolean isSuccess, String tempFilePath) {


        }

        @Override
        public void onExceptionCaught(String exception) {

            Log.i(TAG, "onExceptionCaught: exception:" + exception);


            //文件不存在
            if (exception.contains("file not exist")) {
                transportListener.onExceptionCaught(ExceptionMessage.FILE_NOT_EXIST);
            }

            //没有获取存储权限
            else if (exception.contains("Permission denied")) {
                transportListener.onExceptionCaught(ExceptionMessage.STORAGE_PERMISSION_DENIED);
            }

            //文件加密错误，重新传输
            else if (exception.contains("file encode wrong")) {
                //先停止
                task.onStop();
                isFinished = false;
                //重传
                //下载请求
                Message msg = new Message();
                msg.setAction(Message.Action.FILE_RE_UPLOAD_REQUEST);
                msg.setHasFileData(false);
                if (params != null && params.size() > 0) {
                    for (String key : params.keySet()) {
                        msg.addParam(key, params.get(key));
                    }
                }

                Message.File file = new Message.File();
                file.setFileName(fileName);
                file.setFilePath(filePath);

                String md5 = MD5Util.getFileMD5(new File(filePath));
                file.setMd5(md5);
                Log.i(TAG, "onStart: md5:" + md5);
                msg.setFile(file);

                task = new UploadTask(msg, transportListener, fileListener, netStateListener);
                task.start();
            }

            //文件加锁，稍后再试
            else if (exception.contains("file is locked")) {

                Log.i(TAG, "onExceptionCaught: stop");
                //先停止
                task.onStop();
                Log.i(TAG, "onExceptionCaught: wait to start");
                //等待两秒
                waitTime(2);
                //重启下载
                onStart();
            }

            //文件校验错误
            else if (exception.contains("file md5 is wrong")) {
                //先停止
                task.onStop();
                //重启下载
                onStart();
            } else {

                task.onStop();
                transportListener.onExceptionCaught(ExceptionMessage.UNKNOWN_EXCEPTION);
            }
        }
    };

    //网络状态缉监听器
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
                case READ_AND_WRITE:
                    Log.i(TAG, "onTimedOut: READ_AND_WRITE");

                    //等待三秒再次连接
                    try {
                        Log.i(TAG, "run: wait to ReStart!!!");
                        sleep(1000 * 2);

                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    //重新上传
                    onStart();

                    break;
                case WRITE:
                    break;
                case READ:
                    break;
            }
        }

        @Override
        public void onExceptionCaught(String exception) {
            Log.i(TAG, "onExceptionCaught: exception:" + exception);

            exceptionHandled = true;
            if (exception != null) {
                if (exception.contains("connection timed out")) {
                    //网络超时
                    timedOutCount++;
                    //若网络不可达重试五次以上仍无法连接，则判断为无法连接
                    if (timedOutCount > NetConfig.TIMED_OUT_COUNT) {
                        transportListener.onExceptionCaught(ExceptionMessage.NETWORK_UNREACHABLE);
                    } else {
                        //先停止
                        task.onStop();
                        Log.i(TAG, "onExceptionCaught: wait to start");
                        //等待两秒
                        waitTime(2);
                        //重启下载
                        onStart();
                    }
                }


                //服务器断开连接
                if (exception.contains("Connection reset by peer")) {

                    //先停止
                    task.onStop();
                    Log.i(TAG, "onExceptionCaught: wait to start");
                    //等待两秒
                    waitTime(2);
                    //重启下载
                    onStart();
                }

                //人为断开
                if (exception.contains("Software caused connection abort")) {
                    transportListener.onExceptionCaught(ExceptionMessage.NETWORK_UNREACHABLE);
                }

                //网络不可达
                if (exception.contains("Network is unreachable")) {

                    unreachableCount++;
                    //若网络不可达重试五次以上仍无法连接，则判断为无法连接
                    if (unreachableCount > NetConfig.NETWORK_UNREACHABLE_COUNT) {
                        transportListener.onExceptionCaught(ExceptionMessage.NETWORK_UNREACHABLE);
                    } else {
                        //先停止
                        task.onStop();
                        Log.i(TAG, "onExceptionCaught: wait to start");
                        //等待两秒
                        waitTime(2);
                        //重启下载
                        onStart();
                    }

                }

                //服务器拒绝连接
                if (exception.contains("Connection refused")) {
                    transportListener.onExceptionCaught(ExceptionMessage.CONNECTION_REFUSED);
                }

                //没有获取存储权限
                if (exception.contains("Permission denied")) {
                    transportListener.onExceptionCaught(ExceptionMessage.STORAGE_PERMISSION_DENIED);
                }

                //出现错误，重启
                if (exception.contains("event executor terminated")) {
                    //先停止
                    task.onStop();
                    //等待两秒
                    waitTime(1);
                    //重启下载
                    onStart();
                }

                //
            }

        }

        @Override
        public void onComplete(ChannelHandlerContext ctx) {
            isFinished = true;
            exceptionHandled = true;
        }

        @Override
        public void onChannelInactive(ChannelHandlerContext ctx) {

            Log.i(TAG, "onChannelInactive: channel has inactive");

            if (!isFinished && !exceptionHandled) {
                onStart();
            }

        }
    };


    /**
     * 开始上传
     */
    public void onStart() {

        //下载请求
        Message msg = new Message();
        msg.setAction(Message.Action.FILE_UPLOAD_REQUEST);

        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                msg.addParam(key, params.get(key));
            }
        }

        Message.File file = new Message.File();
        file.setFileName(fileName);
        file.setFilePath(filePath);

        String md5 = MD5Util.getFileMD5(new File(filePath));
        file.setMd5(md5);
        Log.i(TAG, "onStart: md5:" + md5);

        msg.setFile(file);
        msg.setHasFileData(false);

        task = new UploadTask(msg, transportListener, fileListener, netStateListener);
        task.start();
    }


    /**
     * 暂停下载
     */
    public void onPause() {

        if (task != null) {
            task.onStop();
            task = null;
        }
    }

    /**
     * 恢复下载
     */
    public void onResume() {
        //重新下载
        onStart();
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
}
