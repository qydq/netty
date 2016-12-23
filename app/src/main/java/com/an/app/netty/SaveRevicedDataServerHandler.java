package com.an.app.netty;

import com.an.base.utils.DataService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SaveRevicedDataServerHandler extends ChannelInboundHandlerAdapter {

    private static final String TAG = "SaveRevicedDataServerHandler";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务端收到数据" + msg);
        if (null != msg) {
            String dataString = DataService.INSTANCE.getLongDateTime();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
    }
}