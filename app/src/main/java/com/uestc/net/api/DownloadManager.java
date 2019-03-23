package com.uestc.net.api;

import android.util.Log;

import com.uestc.net.callback.FileTransportListener;
import com.uestc.net.callback.NetStateListener;
import com.uestc.net.callback.TransportListener;
import com.uestc.net.protocol.DownloadTask;
import com.uestc.net.protocol.ExceptionMessage;
import com.uestc.net.protocol.Message;
import com.uestc.net.protocol.TimedOutReason;
import com.uestc.util.SharePreferenceUtil;

import java.io.File;

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

    private String ip;
    private int port;
    private String savedPath;
    private String fileName;
    private TransportListener transportListener;

    private int unreachableCount = 0;

    /**
     * 文件下载监听器
     */
    private FileTransportListener fileListener = new FileTransportListener() {

        /**
         * 传输进度
         *  @param fileId    文件id
         * @param percentage  传输进度
         * @param totalSize 总大小
         */
        @Override
        public void onProgress(String fileId, double percentage, long totalSize) {
            unreachableCount = 0;
            transportListener.onProgress(percentage, totalSize);
        }

        /**
         * 下载完成
         *
         * @param fileId       唯一文件id
         * @param isSuccess    是否下载成功
         * @param tempFilePath 下载的临时文件路径
         */
        @Override
        public void onComplete(String fileId, boolean isSuccess, String tempFilePath) {

            if (!isSuccess) {
                transportListener.onExceptionCaught("file MD5 wrong!");

            }
        }

        @Override
        public void onExceptionCaught(String exception) {

            Log.i(TAG, "onExceptionCaught: exception:" + exception);
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

            switch (timeOutReason) {
                case readTimedOut:
                case connectionTimedOut:

                    Log.i(TAG, "onReadTimeOut: connectionTimedOut");
                    Log.i(TAG, "onReadTimeOut: readTimedOut");

                    //先停止
                    task.onStop();
                    Log.i(TAG, "onReadTimeOut: wait to start ");
                    waitTime(2);
                    //重启下载
                    onStart();

                    break;
                case writeTimedOut:
                    break;

                case readAndWriteTimedOut:
                    break;
            }

        }

        @Override
        public void onExceptionCaught(String exception) {
            Log.i(TAG, "onExceptionCaught: exception:" + exception);

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
                // TODO: 2019/3/23 判断网络可用性
                transportListener.onExceptionCaught(ExceptionMessage.NETWORK_UNREACHABLE);
            }

            //网络不可达
            if (exception.contains("Network is unreachable")) {

                unreachableCount++;
                //若网络不可达重试五次以上仍无法连接，则判断为无法连接
                if (unreachableCount > 5) {
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
        }

        @Override
        public void onChannelInactive(ChannelHandlerContext ctx) {

            Log.i(TAG, "onChannelInactive: channel has inactive");

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

    public DownloadManager(String ip, int port, String fileName, String savedPath, TransportListener transportListener) {
        this.ip = ip;
        this.port = port;
        this.savedPath = savedPath;
        this.fileName = fileName;
        this.transportListener = transportListener;
    }


    /**
     * 下载
     */
    public void onStart() {

        //下载请求
        Message msg = new Message();
        msg.setType(Message.Type.REQUEST);
        msg.setAction("fileDownloadRequest");
        msg.addParam("fileName", fileName);
        msg.setHasFile(false);

        long offset = 0;
        String tempFilePath = SharePreferenceUtil.getTempPath(fileName);
        if (!tempFilePath.equals("")) {
            File tempFile = new File(tempFilePath);
            if (tempFile.exists()) {
                offset = tempFile.length();
            }
        }
        msg.addParam("fileOffset", offset + "");

        task = new DownloadTask(ip, port, msg, transportListener, fileListener, netStateListener);
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

}