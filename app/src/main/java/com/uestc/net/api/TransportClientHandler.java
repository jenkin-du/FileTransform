package com.uestc.net.api;

import android.util.Log;

import com.uestc.net.callback.FileTransportListener;
import com.uestc.net.callback.NetStateListener;
import com.uestc.net.callback.TransportListener;
import com.uestc.net.protocol.ExceptionMessage;
import com.uestc.net.protocol.Message;
import com.uestc.net.util.MD5Util;
import com.uestc.net.util.SharePreferenceUtil;

import java.io.File;
import java.net.SocketAddress;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;


/**
 * 客户端使用
 */
public class TransportClientHandler {

    private static final String TAG = "TransportClientHandler";
    //传输监听器
    private TransportListener transportListener;
    //网络监听器
    private NetStateListener netStateListener;
    //文件监听器
    private FileTransportListener fileTransportListener;

    public TransportClientHandler(TransportListener transportListener, FileTransportListener fileTransportListener, NetStateListener netStateListener) {
        this.transportListener = transportListener;
        this.fileTransportListener = fileTransportListener;
        this.netStateListener = netStateListener;
    }

    public void handleMessage(ChannelHandlerContext ctx, Message msg) {

        String action = msg.getAction();
        Log.d(TAG, "handleMessage: action:" + action);
        if (action != null) {

            switch (action) {
                //下载响应
                case Message.Action.FILE_DOWNLOAD_RESPONSE:
                    handleFileDownloadResponse(ctx, msg);
                    break;

                //上传响应
                case Message.Action.FILE_UPLOAD_RESPONSE:
                    handleFileUploadResponse(ctx, msg);
                    break;

                //文件上传结果
                case Message.Action.FILE_UPLOAD_RESULT:
                    handleFileUploadResult(ctx, msg);
                    break;
                //处理检查的结果
                case Message.Action.CHECK_H264_FILE_RESULT:
                    handleCheckH264FileResult(ctx, msg);
                    break;
            }
        }
    }

    /**
     * 处理检查的结果
     */
    private void handleCheckH264FileResult(ChannelHandlerContext ctx, Message msg) {
        netStateListener.onComplete(ctx);
        transportListener.onComplete(msg);
        ctx.channel().close();
    }


    /**
     * 处理文件上传结果
     */
    private void handleFileUploadResult(ChannelHandlerContext ctx, Message msg) {

        String response = msg.getResponse();
        if (response.equals(Message.Response.SUCCESS)) {
            //下载完成
            try {
                ctx.close().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //传输完成
            transportListener.onComplete(msg);
            netStateListener.onComplete(ctx);
        } else if (response.equals(Message.Response.FILE_MD5_WRONG)) {
            // 重新传输
            fileTransportListener.onExceptionCaught("file md5 is wrong");
        }

    }

    /**
     * 处理文件上传确认
     */
    private void handleFileUploadResponse(ChannelHandlerContext ctx, Message msg) {

        String response = msg.getResponse();
        if (response.equals(Message.Response.FILE_READY)) {
            //服务器文件可写

            msg.setAction(Message.Action.FILE_UPLOAD_SEGMENT_RESPONSE);
            msg.setHasFileData(true);

            Log.i(TAG, "handleFileUploadResponse: file is ready");

            //响应
            response(msg, ctx.channel());
        } else if (response.equals(Message.Response.FILE_LOCKED)) {
            fileTransportListener.onExceptionCaught("file is locked");
        }
    }

    /**
     * 处理下载响应
     *
     * @param msg 消息
     */
    private void handleFileDownloadResponse(ChannelHandlerContext ctx, Message msg) {

        String response = msg.getResponse();
        //下载成功
        if (response.equals(Message.Response.FILE_READY)) {
            //下载成功，返回下载成功

            msg.setAction(Message.Action.FILE_DOWNLOAD_RESULT);
            msg.setResponse(Message.Response.SUCCESS);
            msg.setHasFileData(false);

            //下载成功
            transportListener.onComplete(msg);
            netStateListener.onComplete(ctx);
            //响应服务器
            response(msg, ctx.channel());
        }

        //服务器没有文件
        if (response.equals(Message.Response.FILE_NOT_EXIST)) {
            transportListener.onExceptionCaught(ExceptionMessage.FILE_NOT_EXIST);
            netStateListener.onComplete(ctx);
            ctx.channel().close();
        }

        //服务器数据加密错误，重新下载
        if (response.equals(Message.Response.FILE_ENCODE_WRONG)) {

            //删除已下载的文件
            String tempPath = SharePreferenceUtil.get(MD5Util.getTempFileKey(msg));
            File tempFile = new File(tempPath);
            if (tempFile.exists()) {
                tempFile.delete();
            }
            SharePreferenceUtil.remove(MD5Util.getTempFileKey(msg));


            //重新下载
            msg.setAction(Message.Action.FILE_DOWNLOAD_REQUEST);
            msg.setResponse("");
            msg.setHasFileData(false);
            msg.getFile().setFileOffset(0);

            response(msg, ctx.channel());
        }

        //服务器文件没有准备好
        if (response.equals(Message.Response.FILE_LOCKED)) {

            //文件被加锁，不可写，等1秒再询问
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //下载
            msg.setAction(Message.Action.FILE_DOWNLOAD_REQUEST);
            msg.setResponse("");
            msg.setHasFileData(false);

            response(msg, ctx.channel());
        }

    }

    /**
     * 响应服务器
     *
     * @param msg     收到的消息
     * @param channel 发送通道
     */
    private void response(final Message msg, final Channel channel) {
        // 从channel获取客户端的地址
        final SocketAddress socketAddress = channel.remoteAddress();

        // 将msg发送出去
        channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (channelFuture.isSuccess()) {
                    Log.d(TAG, "Sent result {" + msg + "} to client {" + socketAddress + "}");
                } else {
                    Log.i(TAG, "operationComplete: channelFuture:" + channelFuture.cause());

                    netStateListener.onExceptionCaught(channelFuture.cause().getLocalizedMessage());
                }
            }
        });
    }
}
