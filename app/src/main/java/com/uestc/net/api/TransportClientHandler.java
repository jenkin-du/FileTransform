package com.uestc.net.api;

import android.util.Log;

import com.uestc.net.callback.TransportListener;
import com.uestc.net.protocol.ExceptionMessage;
import com.uestc.net.protocol.Message;

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
    private TransportListener mTransportListener;

    public TransportClientHandler(TransportListener transportListener) {
        this.mTransportListener = transportListener;
    }

    public void handleMessage(ChannelHandlerContext ctx, Message msg) {

        String action = msg.getAction();
        if (action != null) {

            switch (action) {
                //下载响应
                case "fileDownloadAck":
                    handleFileDownloadAck(ctx, msg);
                    Log.i(TAG, "handleMessage: fileDownloadAck");
                    break;

                //上传确认
                case "fileUploadAck":
                    handleFileUploadAck(ctx, msg);
                    break;

                //处理分段上传结果
                case "fileUploadSegmentResult":
                    handleFileUploadSegmentResult(ctx, msg);
                    break;
                //文件上传结果
                case "fileUploadResult":
                    handleFileUploadResult(ctx, msg);
                    break;

            }
        }
    }

    /**
     * 处理分段上传结果
     */
    private void handleFileUploadSegmentResult(ChannelHandlerContext ctx, Message msg) {

        String result = msg.getParam("result");
        if (result.equals(Message.Result.SUCCESS)) {

            long fileLength = Long.parseLong(msg.getParam("fileLength"));
            long fileOffset = Long.parseLong(msg.getParam("fileOffset"));
            //更新进度
            mTransportListener.onProgress(fileOffset * 1.0 / fileLength, fileLength);

            Message responseMsg = new Message();
            responseMsg.setType(Message.Type.REQUEST);
            responseMsg.setAction("fileUploadSegment");
            responseMsg.setHasFile(true);
            responseMsg.addParam("fileName", msg.getParam("fileName"));
            responseMsg.addParam("filePath", msg.getParam("filePath"));
            responseMsg.addParam("fileOffset", msg.getParam("fileOffset"));

            //响应
            response(responseMsg, ctx.channel());
        }
    }

    /**
     * 处理文件上传结果
     */
    private void handleFileUploadResult(ChannelHandlerContext ctx, Message msg) {

        String result = msg.getParam("result");
        if (result.equals(Message.Result.SUCCESS)) {
            //下载完成
            ctx.close();

            //传输完成
            mTransportListener.onComplete();
        } else if (result.equals(Message.Result.FILE_MD5_WRONG)) {
            // 重新传输
            String fileName = msg.getParam("fileName");
            if (fileName != null) {

                Message responseMsg = new Message();
                responseMsg.setType(Message.Type.REQUEST);
                responseMsg.setAction("fileUploadSegment");
                responseMsg.setHasFile(true);
                responseMsg.addParam("fileName", msg.getParam("fileName"));
                responseMsg.addParam("filePath", msg.getParam("filePath"));
                responseMsg.addParam("fileOffset", 0 + "");

                //响应
                response(responseMsg, ctx.channel());
            }

        }

    }

    /**
     * 处理文件上传确认
     */
    private void handleFileUploadAck(ChannelHandlerContext ctx, Message msg) {

        String ack = msg.getParam("ack");
        if (ack.equals(Message.Ack.FILE_READY)) {
            //服务器文件可写
            Message responseMsg = new Message();
            responseMsg.setType(Message.Type.REQUEST);
            responseMsg.setAction("fileUploadSegment");
            responseMsg.setHasFile(true);
            responseMsg.addParam("fileName", msg.getParam("fileName"));
            responseMsg.addParam("filePath", msg.getParam("filePath"));
            responseMsg.addParam("fileOffset", msg.getParam("fileOffset"));

            Log.i(TAG, "handleFileUploadAck: file is ready");

            //响应
            response(responseMsg, ctx.channel());
        } else if (ack.equals(Message.Ack.FILE_LOCKED)) {
            //文件被加锁，不可写，等五秒钟在询问

            try {
                Thread.sleep(1000 * 3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //下载请求
            Message responseMsg = new Message();
            msg.setType(Message.Type.REQUEST);
            msg.setAction("fileUploadRequest");
            msg.addParam("fileName", msg.getParam("fileName"));
            msg.addParam("filePath", msg.getParam("filePath"));

            //响应
            response(responseMsg, ctx.channel());
        }


    }

    /**
     * 处理下载响应
     *
     * @param msg 消息
     */
    private void handleFileDownloadAck(ChannelHandlerContext ctx, Message msg) {

        String ack = msg.getParam("ack");
        //下载成功
        if (ack.equals(Message.Ack.FILE_READY)) {
            //下载成功，返回下载成功
            Message resultMsg = new Message();
            resultMsg.setType(Message.Type.REQUEST);
            resultMsg.setAction("fileDownloadResult");
            resultMsg.addParam("result", Message.Result.SUCCESS);
            resultMsg.addParam("fileName", msg.getParam("fileName"));

            //下载成功
            mTransportListener.onComplete();
            //响应服务器
            response(resultMsg, ctx.channel());
        }

        //服务器没有文件
        if (ack.equals(Message.Ack.FILE_NOT_EXIST)) {
            mTransportListener.onExceptionCaught(Message.Ack.FILE_NOT_EXIST);
            ctx.channel().close();
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

                    if (channelFuture.cause().getLocalizedMessage().contains("Software caused connection abort")) {
                        mTransportListener.onExceptionCaught(ExceptionMessage.NETWORK_UNREACHABLE);
                    }
                }
            }
        });
    }
}