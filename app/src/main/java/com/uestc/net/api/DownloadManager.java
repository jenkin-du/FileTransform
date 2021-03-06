package com.uestc.net.api;

import android.util.Log;

import com.uestc.net.callback.FileTransportListener;
import com.uestc.net.callback.NetStateListener;
import com.uestc.net.callback.TransportListener;
import com.uestc.net.config.NetConfig;
import com.uestc.net.protocol.DownloadTask;
import com.uestc.net.protocol.ExceptionMessage;
import com.uestc.net.protocol.Message;
import com.uestc.net.protocol.TimedOutReason;
import com.uestc.net.util.MD5Util;
import com.uestc.net.util.SharePreferenceUtil;

import java.io.File;
import java.util.HashMap;

import io.netty.channel.ChannelHandlerContext;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/19
 *     desc   : 下载管理器
 *     version: 1.0
 * </pre>
 */
public class DownloadManager {

    private String TAG = "DownloadManager";


    private DownloadTask task;
    private int unreachableCount = 0;
    private int timedOutCount = 0;


    private HashMap<String, String> params;
    private String savedPath;
    private String fileName;
    private TransportListener transportListener;

    private boolean isFinished = false;
    private boolean exceptionHandled = false;


    public DownloadManager(String fileName, String savedPath, HashMap<String, String> params, TransportListener transportListener) {
        this.savedPath = savedPath;
        this.fileName = fileName;
        this.params = params;
        this.transportListener = transportListener;
    }

    /**
     * 文件下载监听器
     */
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
            timedOutCount = 0;
            transportListener.onProgress(percentage, totalSize);
        }

        /**
         * 下载完成
         *
         * @param isSuccess    是否下载成功
         * @param tempFilePath 下载的临时文件路径
         */
        @Override
        public void onComplete(boolean isSuccess, String tempFilePath) {

            if (!isSuccess) {
                transportListener.onExceptionCaught(ExceptionMessage.FILE_MD5_WRONG);
            } else {
                File temp = new File(tempFilePath);
                File file = new File(savedPath);
                boolean success = temp.renameTo(file);
                Log.i(TAG, "onComplete: renameTo " + success);
            }
        }

        @Override
        public void onExceptionCaught(String exception) {

            Log.i(TAG, "FileTransportListener onExceptionCaught:" + exception);

            //文件不存在
            if (exception.contains("file not exist")) {
                transportListener.onExceptionCaught(ExceptionMessage.FILE_NOT_EXIST);
            }

            //没有获取存储权限
            if (exception.contains("Permission denied")) {
                transportListener.onExceptionCaught(ExceptionMessage.STORAGE_PERMISSION_DENIED);
            }

            //空间不充足
            if (exception.contains("storage is not enough")) {
                transportListener.onExceptionCaught(ExceptionMessage.STORAGE_NOT_ENOUGH);
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
                    task.onStop();
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
                    task.onStop();
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
                task.onStop();
                Log.i(TAG, "onExceptionCaught: wait to start");
                //等待两秒
                waitTime(2);
                //重启下载
                onStart();
            }

            //人为断开
            else if (exception.contains("Software caused connection abort")) {
                //网络不可用
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
                    task.onStop();
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
                task.onStop();
                //等待两秒
                waitTime(1);
                //重启下载
                onStart();
            } else {
                task.onStop();
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


    /**
     * 下载
     */
    public void onStart() {

        //下载请求
        Message msg = new Message();
        msg.setAction(Message.Action.FILE_DOWNLOAD_REQUEST);

        if (params != null && params.size() > 0) {

            for (String key : params.keySet()) {
                msg.addParam(key, params.get(key));
            }
        }

        Message.File file = new Message.File();
        file.setFileName(fileName);

        msg.setFile(file);
        msg.setHasFileData(false);

        long offset = 0;
        String tempFilePath = SharePreferenceUtil.get(MD5Util.getTempFileKey(msg));
        if (!tempFilePath.equals("")) {
            File tempFile = new File(tempFilePath);
            if (tempFile.exists()) {
                offset = tempFile.length();
            }
        }
        msg.getFile().setFileOffset(offset);

        task = new DownloadTask( msg, transportListener, fileListener, netStateListener);
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
     * 重新下载
     */
    public void onRestart() {
        onStart();
    }

}
