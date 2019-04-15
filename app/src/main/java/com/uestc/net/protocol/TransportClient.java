package com.uestc.net.protocol;

import android.util.Log;

import com.uestc.net.api.TransportClientHandler;
import com.uestc.net.callback.FileTransportListener;
import com.uestc.net.callback.NetStateListener;
import com.uestc.net.callback.TransportListener;
import com.uestc.net.config.NetConfig;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/08
 *     desc   : 传输客户端
 *     version: 1.0
 * </pre>
 */
public class TransportClient {

    private String TAG = "TransportClient";

    //传输通道
    private Channel channel = null;

    private static volatile EventLoopGroup group = null;

    private String host;
    private int port;

    //文件监听器
    private FileTransportListener fileListener;
    //传输监听器
    private TransportListener transportListener;
    //网络监听器
    private NetStateListener netStateListener;

    public TransportClient(String host, int port, FileTransportListener fileTransportListener, TransportListener transportListener, NetStateListener netStateListener) {
        this.host = host;
        this.port = port;
        this.fileListener = fileTransportListener;
        this.transportListener = transportListener;
        this.netStateListener = netStateListener;
    }




    //开启连接
    public void startConnect() throws InterruptedException {
        //工作组
        if (group == null || group.isShutdown()) {
            group = new NioEventLoopGroup();
        }
        //引导类
        Bootstrap b = new Bootstrap();
        //处理响应消息的handler,客户端使用
        final TransportClientHandler transportClientHandler = new TransportClientHandler(transportListener, fileListener, netStateListener);

        //指定通道类型
        b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)    //TCP，无延迟
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)        //设置连接超时为1秒
                .option(ChannelOption.SO_TIMEOUT, 1)
                .option(ChannelOption.SO_KEEPALIVE, true)                //长连接
                .handler(new ChannelInitializer<SocketChannel>() {        //设置通道对象
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        //ch.pipeline().addLast("SSLHandler",new SslHandler(sslEngine));		//这里决定是否启用SSL
                        ch.pipeline().addLast(new IdleStateHandler(NetConfig.READ_IDLE_TIME, NetConfig.WRITE_IDLE_TIME, NetConfig.ALL_IDLE_TIME));
                        //自己实现的msg编码器,继承了MessageToByteEncoder,出站处理器
                        ch.pipeline().addLast("encoder", new TransportFrameEncoder(fileListener));
                        //自己实现的帧解码器,继承了ChannelInboundHandlerAdapter,入站处理器
                        //TCP帧解码，对收到的字节流进行解码
                        ch.pipeline().addLast("decoder", new TransportFrameDecoder(transportClientHandler, fileListener, netStateListener));
                    }
                });

        //建立一个监听者
        ChannelFuture channelFuture;
        //连接端口并注册监听,阻塞到连接成功为止,n秒超时
        channelFuture = b.connect(host, port).sync();
        Log.i(TAG, "Server starts，remote server address:" + host + ":" + port);
        //channel相当于该连接
        channel = channelFuture.channel();
    }

    /**
     * 关掉客户端的连接池
     */
    public void closeGroup() {
        if (group != null) {
            try {
                //阻塞地关闭整个线程池
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关掉连接
     */
    public void closeChannel() {
        //阻塞关连接
        if (channel != null) {
            channel.close();
            channel = null;
            //                closeGroup();
        }
    }


    /**
     * 传输消息
     *
     * @param msg 传输下载的请求
     */
    public void transportMessage(final Message msg) {

        channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (channelFuture.isSuccess()) {
//                    Log.i(TAG, "Send Message " + msg + " success.");
                } else {
//                    Log.i(TAG, "Send Message" + msg + " failed.");
                }
            }
        });
    }
}
