package com.uestc.net.protocol;

import com.uestc.app.App;
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
public class DownloadTask extends Thread {

    private String TAG = "DownloadTask";

    private TransportClient client;

    private String ip= App.ip;
    private int port=App.port;
    private FileTransportListener fileListener;
    private NetStateListener netStateListener;
    private TransportListener transportListener;

    private Message msg;

    public DownloadTask(Message msg, TransportListener transportListener, FileTransportListener fileListener, NetStateListener netStateListener) {
        this.msg = msg;
        this.fileListener = fileListener;
        this.transportListener = transportListener;
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

            String exception = e.getLocalizedMessage();
            netStateListener.onExceptionCaught(exception);
        }
    }

    /**
     * 关闭连接
     */
    public void onStop() {

        if (client != null) {
            client.closeChannel();
            client = null;
        }

    }
}
