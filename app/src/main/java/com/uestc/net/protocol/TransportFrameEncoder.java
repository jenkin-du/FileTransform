package com.uestc.net.protocol;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.uestc.net.callback.FileTransportListener;
import com.uestc.util.MD5Util;

import java.io.File;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/08
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class TransportFrameEncoder extends MessageToByteEncoder<Message> {

    private static final int SEGMENT_LENGTH = 1024 * 1024 * 5;
    private FileTransportListener fileListener;

    TransportFrameEncoder(FileTransportListener fileListener) {
        this.fileListener = fileListener;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf out) throws Exception {

        Log.i("TransportFrameEncoder", "encode: message:" + message);

        boolean hasFile = message.isHasFile();
        if (hasFile) {
            String filePath = message.getParams().get("filePath");
            File file = new File(filePath);
            if (file.exists()) {
                //添加文件属性
                long fileLength = file.length();
                message.addParam("fileLength", fileLength + "");

                String md5 = MD5Util.getFileMd5(new File(filePath));
                message.addParam("fileMD5", md5);

                long offset = Long.parseLong(message.getParam("fileOffset"));
                Log.i("TransportFrameEncoder", "encode: offset:" + offset);

                RandomAccessFile raf = new RandomAccessFile(new File(filePath), "rw");
                // 跳过已读的字节数
                raf.seek(offset);
                //读取指定长度的数据
                byte[] bytes = new byte[1024];
                //剩下数据不足一个分段
                if (offset + SEGMENT_LENGTH >= fileLength) {

                    long segmentLength = fileLength - offset;
                    if (segmentLength > 0) {
                        message.addParam("segmentLength", segmentLength + "");

                        //转化为json格式的支付串
                        String jsonMessage = JSON.toJSONString(message);
                        byte[] byteMsg = jsonMessage.getBytes();
                        int msgLength = byteMsg.length;

                        Log.i("TransportFrameEncoder", "encode: jsonMessage:" + jsonMessage);
                        Log.i("TransportFrameEncoder", "encode: msgLength = " + msgLength);

                        //写入参数
                        out.writeInt(msgLength);
                        out.writeBytes(byteMsg);
                        //写入数据
                        while (raf.read(bytes) != -1) {
                            out.writeBytes(bytes);
                        }

                        Log.i("TransportFrameEncoder", "encode: write end!!!");
                        raf.close();
                    } else {
                        fileListener.onExceptionCaught("file encode wrong");
                        channelHandlerContext.close();
                    }

                } else {

                    message.addParam("segmentLength", SEGMENT_LENGTH + "");
                    //转化为json格式的支付串
                    String jsonMessage = JSON.toJSONString(message);
                    byte[] byteMsg = jsonMessage.getBytes();
                    int msgLength = byteMsg.length;

                    Log.i("TransportFrameEncoder", "encode: jsonMessage:" + jsonMessage);
                    Log.i("TransportFrameEncoder", "encode: msgLength = " + msgLength);

                    //写入参数
                    out.writeInt(msgLength);
                    out.writeBytes(byteMsg);

                    //读取一个分段的数据
                    for (int i = 0; i < SEGMENT_LENGTH / 1024; i++) {
                        if (raf.read(bytes) != -1) {
                            out.writeBytes(bytes);
                        }
                    }
                }

                Log.i("TransportFrameEncoder", "encode: write end!!!");
                raf.close();
            } else {
                //出现异常
                fileListener.onExceptionCaught("file not exist");
                //关闭连接
                channelHandlerContext.close();
            }
        } else {
            //没有文件传输
            // 转化为json格式的支付串
            String jsonMessage = JSON.toJSONString(message);

            System.out.println("TransportFrameEncoder encode: jsonMessage:" + jsonMessage);

            byte[] byteMsg = jsonMessage.getBytes();
            int msgLength = byteMsg.length;

            System.out.println("TransportFrameEncoder encode: msgLength = " + msgLength);

            // 写入参数
            out.writeInt(msgLength);
            out.writeBytes(byteMsg);
        }
    }
}
