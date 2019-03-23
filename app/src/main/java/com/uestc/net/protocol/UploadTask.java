package com.uestc.net.protocol;

import android.util.Log;

import com.uestc.net.callback.FileTransportListener;
import com.uestc.net.callback.NetStateListener;
import com.uestc.net.callback.TransportListener;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/01/14
 *     desc   : 下载
 *     version: 1.0
 * </pre>
 */
public class UploadTask extends Thread {

    private String TAG = "UploadTask";

    private TransportClient client;

    private String ip;
    private int port;
    private Message msg;
    private TransportListener transportListener;
    private FileTransportListener fileListener;
    private NetStateListener netStateListener;


    public UploadTask(String ip, int port, Message msg, TransportListener transportListener, FileTransportListener fileListener, NetStateListener netStateListener) {
        this.ip = ip;
        this.port = port;
        this.msg = msg;
        this.transportListener = transportListener;
        this.fileListener = fileListener;
        this.netStateListener = netStateListener;
    }


    @Override
    public void run() {

        try {
            client = new TransportClient(ip, port, fileListener, transportListener, netStateListener);
            client.startConnect();
            //下载文件
            client.transportMessage(msg);

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "run: e:" + e.getLocalizedMessage());

            String exception = e.getLocalizedMessage();
            netStateListener.onExceptionCaught(exception);
        }
    }

    /**
     * 关闭连接
     */
    public void onStop() {
        client.closeChannel();
        client = null;
    }

}
