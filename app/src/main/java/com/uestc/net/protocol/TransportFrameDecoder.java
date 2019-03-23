package com.uestc.net.protocol;


import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.uestc.net.api.TransportClientHandler;
import com.uestc.net.callback.FileTransportListener;
import com.uestc.net.callback.NetStateListener;
import com.uestc.util.MD5Util;
import com.uestc.util.SharePreferenceUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * <pre>
 *     author : jenkin
 *     e-mail : jekin-du@foxmail.com
 *     time   : 2019/03/08
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class TransportFrameDecoder extends ChannelInboundHandlerAdapter {

    private static final String TAG = "TransportFrameDecoder";

    // 消息头是否读
    private boolean msgHeaderRead = false;
    // 消息是否读
    private boolean msgRead = false;
    // 是否携带文件
    private boolean hasFile = false;
    //首次写文件
    private boolean isFirstWrite = true;

    // 消息所占字节数
    private int msgSize = 0;
    // 文件大小
    private long fileSize = 0;
    // 文件剩余传输字节数
    private long fileLeftSize = 0;
    // 文件已传输字节数
    private long fileOffset = 0;
    // 传输的每段的字节数
    private long segmentLeftSize = 0;
    // 已读的每段的字节数
    private long segmentRead = 0;

    private byte[] remainingByte;//本次没有读完的数据

    //文件传输时写入的临时文件
    private File tempFile;
    private RandomAccessFile randomAccessFile;

    //业务逻辑处理器
    private TransportClientHandler clientHandler;
    //消息
    private Message msg;


    //文件传输监听器
    private FileTransportListener fileListener;
    //网络状态监听器
    private NetStateListener netListener;

    // 文件锁
    private FileLock lock;

    TransportFrameDecoder(TransportClientHandler clientHandler, FileTransportListener listener, NetStateListener netListener) {
        this.clientHandler = clientHandler;
        this.fileListener = listener;
        this.netListener = netListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object byteBuf) throws Exception {
        ByteBuf buf = (ByteBuf) byteBuf;

        /*
         * 读取参数
         */

        //读参数头
        if (!msgHeaderRead) {
            msgSize = buf.readInt();
            msgHeaderRead = true;

            Log.d(TAG, "channelRead: msgSize =" + msgSize);
        }

        //读参数
        if (!msgRead && msgSize != 0) {

            //第一帧数据中能读出参数
            byte[] msgByte;
            if (remainingByte == null && buf.readableBytes() >= msgSize) {
                msgByte = new byte[msgSize];
                int remaining = buf.readableBytes() - msgSize;
                //读取参数字节
                buf.readBytes(msgByte);
                //读取剩余字节
                if (remaining >= 0) {
                    //读取剩余字节
                    remainingByte = new byte[remaining];
                    buf.readBytes(remainingByte);

                }
                msgRead = true;

                //解析数据
                String jsonMsg = new String(msgByte);
                Log.d(TAG, "channelRead: jsonMsg:" + jsonMsg);
                msg = JSON.parseObject(jsonMsg, Message.class);

                //有文件传输
                if (msg.isHasFile()) {

                    fileSize = Long.parseLong(msg.getParams().get("fileLength"));
                    fileOffset = Long.parseLong(msg.getParam("fileOffset"));
                    fileLeftSize = fileSize - fileOffset;
                    segmentLeftSize = Long.parseLong(msg.getParam("segmentLength"));
                    hasFile = true;

                    //没有文件传输
                } else {

                    clientHandler.handleMessage(ctx, msg);
                    //重置控制变量
                    reset();
                }

            }

            //第一帧数据中不能读出参数
            if (remainingByte == null && buf.readableBytes() < msgSize) {

                long remaining = buf.readableBytes();
                remainingByte = new byte[(int) remaining];
                buf.readBytes(remainingByte);

            }

            //第二帧数据中不能读出参数,直到能读出数据为止
            if (remainingByte != null && remainingByte.length + buf.readableBytes() < msgSize) {
                byte[] data = new byte[buf.readableBytes()];
                buf.readBytes(data);
                remainingByte = byteMerger(remainingByte, data);
            }

            //第二帧数据中能读出参数
            if (remainingByte != null && remainingByte.length + buf.readableBytes() >= msgSize) {

                int toRead = msgSize - remainingByte.length;
                if (toRead > 0) {

                    byte[] toReadByte = new byte[toRead];
                    int remaining = buf.readableBytes() - toRead;

                    buf.readBytes(toRead);
                    msgByte = byteMerger(remainingByte, toReadByte);

                    if (remaining >= 0) {
                        //读取剩余字节
                        remainingByte = new byte[remaining];
                        buf.readBytes(remainingByte);

                    }
                    msgRead = true;

                    //解析数据
                    String jsonMsg = new String(msgByte);
                    Log.d(TAG, "channelRead: jsonMsg:" + jsonMsg);

                    msg = JSON.parseObject(jsonMsg, Message.class);
                    //有文件传输
                    if (msg.isHasFile()) {

                        fileSize = Long.parseLong(msg.getParams().get("fileLength"));
                        fileOffset = Long.parseLong(msg.getParam("fileOffset"));
                        fileLeftSize = fileSize - fileOffset;
                        segmentLeftSize = Long.parseLong(msg.getParam("segmentLength"));
                        hasFile = true;

                        //没有文件传输
                    } else {

                        //处理业务逻辑
                        clientHandler.handleMessage(ctx, msg);
                        //重置控制变量
                        reset();
                    }
                }
            }
        }

        /*
         * 写文件
         */
        if (hasFile && segmentLeftSize != 0) {

            if (randomAccessFile == null) {

                String tempFilePath = SharePreferenceUtil.getTempPath(msg.getParam("fileName"));
                if (!tempFilePath.equals("")) {
                    tempFile = new File(tempFilePath);
                    if (tempFile.exists()) {
                        randomAccessFile = new RandomAccessFile(tempFile, "rw");
                        // 对文件进行加锁
                        lock = randomAccessFile.getChannel().tryLock();
                        if (lock == null) {
                            fileListener.onExceptionCaught(ExceptionMessage.FILE_LOCKED);
                            randomAccessFile.close();
                            ctx.close();
                            return;
                        }
                    } else {
                        SharePreferenceUtil.removeTempPath(msg.getParam("fileName"));

                        try {
                            // 生成临时文件路径
                            createTempPath();
                        } catch (IOException e) {
                            ctx.channel().close();
                            e.printStackTrace();

                            fileListener.onExceptionCaught(e.getLocalizedMessage());
                            return;
                        }
                        // 对文件进行加锁
                        lock = randomAccessFile.getChannel().tryLock();
                        if (lock == null) {
                            fileListener.onExceptionCaught(ExceptionMessage.FILE_LOCKED);
                            randomAccessFile.close();
                            ctx.close();
                            return;
                        }
                    }
                } else {
                    try {
                        // 生成临时文件路径
                        createTempPath();
                    } catch (IOException e) {
                        ctx.channel().close();
                        e.printStackTrace();

                        fileListener.onExceptionCaught(e.getLocalizedMessage());
                        return;
                    }
                    // 对文件进行加锁
                    lock = randomAccessFile.getChannel().tryLock();
                    if (lock == null) {
                        fileListener.onExceptionCaught(ExceptionMessage.FILE_LOCKED);
                        randomAccessFile.close();
                        ctx.close();
                        return;
                    }
                }


                Log.i(TAG, "channelRead: tempFile.path:" + tempFile.getAbsolutePath());
            }

            //读取剩余的数据
            if (remainingByte != null) {

                if (segmentLeftSize <= remainingByte.length) {

                    byte[] data = subBytes(remainingByte, 0, (int) segmentLeftSize);
                    //写文件,首次写文件需要设置写位置，以支持断点续传
                    if (isFirstWrite) {

                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(data);

                        isFirstWrite = false;
                    } else {
                        randomAccessFile.write(data);
                    }
                    // 跟新数据传输进度
                    segmentRead += segmentLeftSize;
                    fileListener.onProgress("", (fileOffset + segmentRead) * 1.0 / fileSize, fileSize);

                    // 实时检测整个文件大小是否读取完毕
                    fileLeftSize -= segmentLeftSize;

                    if (fileLeftSize == 0) {
                        // 检查文件是否完整
                        checkFileMD5(ctx, msg, tempFile);
                    } else {
                        if (msg.getAction().equals("fileDownloadSegmentAck")) {
                            // 完成数据传输，处理业务逻辑
                            handleSegmentResponse(ctx, msg);
                        }
                    }
                    // 重置控制变量
                    reset();
                    segmentLeftSize = 0;
                    remainingByte = null;

                } else {
                    //写文件,首次写文件需要设置写位置，以支持断点续传
                    if (isFirstWrite) {
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(remainingByte);

                        isFirstWrite = false;
                    } else {
                        randomAccessFile.write(remainingByte);
                    }

                    // 实时检测整个文件大小是否读取完毕
                    fileLeftSize -= remainingByte.length;
                    segmentLeftSize -= remainingByte.length;
                    segmentRead += remainingByte.length;
                    remainingByte = null;

                    // 数据传输进度
                    fileListener.onProgress("", (fileOffset + segmentRead) * 1.0 / fileSize, fileSize);
                }
            }

            //读取一帧的数据
            if (remainingByte == null && segmentLeftSize > 0) {

                if (segmentLeftSize <= buf.readableBytes()) {
                    byte[] data = new byte[(int) segmentLeftSize];
                    buf.readBytes(data);
                    //写文件,首次写文件需要设置写位置，以支持断点续传
                    if (isFirstWrite) {
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(data);

                        isFirstWrite = false;
                    } else {
                        randomAccessFile.write(data);
                    }
                    lock.release();
                    randomAccessFile.close();

                    // 更新数据传输进度
                    segmentRead += segmentLeftSize;
                    fileListener.onProgress("", (fileOffset + segmentRead) * 1.0 / fileSize, fileSize);

                    // 实时检测整个文件大小是否读取完毕
                    fileLeftSize -= segmentLeftSize;

                    if (fileLeftSize == 0) {
                        // 检查文件是否完整
                        checkFileMD5(ctx, msg, tempFile);
                    } else {
                        if (msg.getAction().equals("fileDownloadSegmentAck")) {
                            // 完成数据传输，处理业务逻辑
                            handleSegmentResponse(ctx, msg);
                        }
                    }
                    // 重置控制变量
                    reset();
                    segmentLeftSize = 0;
                } else {
                    // 实时检测整个文件大小是否读取完毕
                    fileLeftSize -= buf.readableBytes();
                    segmentLeftSize -= buf.readableBytes();
                    segmentRead += buf.readableBytes();

                    byte[] data = new byte[buf.readableBytes()];
                    buf.readBytes(data);
                    //写文件,首次写文件需要设置写位置，以支持断点续传
                    if (isFirstWrite) {
                        randomAccessFile.seek(randomAccessFile.length());
                        randomAccessFile.write(data);

                        isFirstWrite = false;
                    } else {
                        randomAccessFile.write(data);
                    }
                    // 数据传输进度
                    fileListener.onProgress("", (fileOffset + segmentRead) * 1.0 / fileSize, fileSize);
                }
            }
        }

        buf.release();

    }

    /**
     * 处理分段数据传输完毕
     */
    private void handleSegmentResponse(ChannelHandlerContext ctx, Message msg) {

        long fileOffset = Long.parseLong(msg.getParam("fileOffset"));
        long segmentLength = Long.parseLong(msg.getParam("segmentLength"));

        msg.setType(Message.Type.REQUEST);
        msg.setAction("fileDownloadSegmentResult");
        msg.setHasFile(false);
        msg.addParam("result", Message.Result.SUCCESS);
        msg.addParam("fileOffset", (fileOffset + segmentLength) + "");

        //回应
        ctx.channel().writeAndFlush(msg);
    }

    /**
     * 生成临时文件路径
     *
     * @throws IOException 异常
     */
    private void createTempPath() throws IOException {

        UUID uuid = UUID.randomUUID();
        // TODO:2019/3/23 临时文件夹需要修改
        // TODO:需要注意的异常，读写权限没有赋予
        File tempFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        if (!tempFolder.exists()) {
            tempFolder.mkdir();
        }
        tempFile = new File(tempFolder.getAbsolutePath() + "/" + uuid + ".tp");
        Log.i(TAG, "createTempPath: createTempPath: " + tempFile.getAbsolutePath());

        tempFile.createNewFile();
        randomAccessFile = new RandomAccessFile(tempFile, "rw");
        // 保存临时文件路径
        SharePreferenceUtil.saveTempPath(msg.getParam("fileName"), tempFile.getAbsolutePath());
    }

    //检查文件MD5
    private void checkFileMD5(ChannelHandlerContext ctx, Message msg, File tempFile) {

        String md5 = MD5Util.getFileMd5(tempFile);
        String fileMD5 = msg.getParam("fileMD5");

        Log.i(TAG, "checkFileMD5: md5 " + md5);
        Log.i(TAG, "checkFileMD5: fileMD5 " + fileMD5);

        assert md5 != null;
        if (md5.equals(fileMD5)) {
            //数据传输进度
            fileListener.onProgress("", 1.0, fileSize);
            //完成数据读写
            fileListener.onComplete("", true, this.tempFile.getAbsolutePath());
            //完成数据传输，处理业务逻辑
            clientHandler.handleMessage(ctx, msg);
            //删除临时文件记录
            SharePreferenceUtil.removeTempPath(msg.getParam("fileName"));
        } else {

            //数据传输进度
            fileListener.onProgress("", 0, fileSize);
            //完成数据读写
            fileListener.onComplete("", false, this.tempFile.getAbsolutePath());
            //重传
            Message message = new Message();
            message.setType(Message.Type.REQUEST);
            message.setAction("fileDownloadResult");
            message.setHasFile(false);
            message.addParam("result", Message.Result.FILE_MD5_WRONG);
            message.addParam("fileName", msg.getParam("fileName"));
            message.addParam("filePath", msg.getParam("filePath"));

            //删除临时文件
            tempFile.delete();
            //删除临时文件记录
            SharePreferenceUtil.removeTempPath(msg.getParam("fileName"));

            //下载错误，响应服务器
            ctx.channel().writeAndFlush(message);
        }

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.d(TAG, "onChannelInactive: channel has inactive");

        if (randomAccessFile != null) {
            lock.release();
            randomAccessFile.close();
        }
        reset();
        //channel 断开了
        netListener.onChannelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {

            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                ctx.close();
                Log.i(TAG, "userEventTriggered: IdleState.ALL_IDLE");
                //超时
                netListener.onTimedOut(TimedOutReason.readAndWriteTimedOut);
            }

            if (event.state() == IdleState.READER_IDLE) {
                ctx.close();
                Log.i(TAG, "userEventTriggered: IdleState.READER_IDLE");
                //超时
                netListener.onTimedOut(TimedOutReason.readTimedOut);
            }

            if (event.state() == IdleState.WRITER_IDLE) {
//                ctx.close();
                Log.i(TAG, "userEventTriggered: IdleState.WRITER_IDLE");
                //超时
                netListener.onTimedOut(TimedOutReason.writeTimedOut);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

        if (ctx.channel().isActive()) {
            ctx.channel().close();
        }

        //channel 异常
        netListener.onExceptionCaught(cause.getLocalizedMessage());
    }

    /**
     * 字节数组融合
     */
    private static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    /**
     * 截取字节数组
     */
    private static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    /**
     * 重置控制变量
     */
    private void reset() {

        msgHeaderRead = false;
        msgRead = false;
        hasFile = false;
        isFirstWrite = true;

        msgSize = 0;
        segmentLeftSize = 0;
        segmentRead = 0;

        remainingByte = null;
        // 文件传输时写入的临时文件
        tempFile = null;
        randomAccessFile = null;

        // 消息
        msg = null;
    }

}
