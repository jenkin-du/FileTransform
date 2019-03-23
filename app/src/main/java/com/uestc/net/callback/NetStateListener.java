package com.uestc.net.callback;

import com.uestc.net.protocol.TimedOutReason;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author jenkin
 *         网络状态监听器
 */
public interface NetStateListener {

    /**
     * 超时
     */
    void onTimedOut(TimedOutReason timeOutReason);


    //网络出现错误
    void onExceptionCaught(String exception);

    //网络断开
    void onChannelInactive(ChannelHandlerContext ctx);


}
