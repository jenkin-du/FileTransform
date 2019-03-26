package com.uestc.net.api;

import android.util.Log;

import com.uestc.net.callback.NetStateListener;
import com.uestc.net.callback.TransportListener;
import com.uestc.net.protocol.ExceptionMessage;
import com.uestc.net.protocol.Message;
import com.uestc.util.SharePreferenceUtil;

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

    public TransportClientHandler(TransportListener transportListener, NetStateListener netStateListener) {
        this.transportListener = transportListener;
        this.netStateListener = netStateListener;
    }

    public void handleMessage(ChannelHandlerContext ctx, Message msg) {

        String action = msg.getAction();
        if (action != null) {

            switch (action) {
                //下载响应
                case "fileDownloadAck":
                    handleFileDownloadAck(ctx, msg);
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

            long fileLength = msg.getFile().getFileLength();
            long fileOffset = msg.getFile().getFileOffset();
            //更新进度
            transportListener.onProgress(fileOffset * 1.0 / fileLength, fileLength);

            Message responseMsg = new Message();
            responseMsg.setAction("fileUploadSegment");
            responseMsg.setHasFileData(true);
            responseMsg.setFile(msg.getFile());

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
            transportListener.onComplete();
        } else if (result.equals(Message.Result.FILE_MD5_WRONG)) {
            // 重新传输
            String fileName = msg.getFile().getFileName();
            if (fileName != null) {

                Message responseMsg = new Message();
                responseMsg.setAction("fileUploadSegment");
                responseMsg.setHasFileData(true);

                Message.File file = msg.getFile();
                file.setFileOffset(0);
                responseMsg.setFile(file);

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
            responseMsg.setAction("fileUploadSegment");
            responseMsg.setHasFileData(true);
            responseMsg.setFile(msg.getFile());

            Log.i(TAG, "handleFileUploadAck: file is ready");

            //响应
            response(responseMsg, ctx.channel());
        } else if (ack.equals(Message.Ack.FILE_LOCKED)) {
            //文件被加锁，不可写，等2秒再询问

            try {
                Thread.sleep(1000 * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //下载请求
            Message responseMsg = new Message();
            msg.setAction("fileUploadRequest");
            msg.setFile(msg.getFile());

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
            resultMsg.setAction("fileDownloadResult");
            resultMsg.addParam("result", Message.Result.SUCCESS);
            resultMsg.setFile(msg.getFile());

            //下载成功
            transportListener.onComplete();
            //响应服务器
            response(resultMsg, ctx.channel());
        }

        //服务器没有文件
        if (ack.equals(Message.Ack.FILE_NOT_EXIST)) {
            transportListener.onExceptionCaught(ExceptionMessage.FILE_NOT_EXIST);
            ctx.channel().close();
        }

        //服务器数据加密错误，重新下载
        if (ack.equals(Message.Ack.FILE_ENCODE_WRONG)) {

            //删除已下载的文件
            String fileName = msg.getFile().getFileName();
            String tempPath = SharePreferenceUtil.get(fileName);
            File tempFile = new File(tempPath);
            if (tempFile.exists()) {
                tempFile.delete();
            }
            SharePreferenceUtil.remove(fileName);


            //重新下载
            msg.setAction("fileDownloadRequest");
            msg.removeParam("ack");
            msg.setHasFileData(false);
            msg.getFile().setFileOffset(0);

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
