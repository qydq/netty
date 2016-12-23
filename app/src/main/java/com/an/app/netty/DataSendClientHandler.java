package com.an.app.netty;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.an.base.utils.DataService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DataSendClientHandler extends ChannelInboundHandlerAdapter {

    private int count = 0;
    private final Context context;
    private final SharedPreferences sp;
    private boolean sendding = true;


    public DataSendClientHandler(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("SuperActivity", Activity.MODE_PRIVATE);
        sendding = sp.getBoolean("sendding", true);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (sendding) {
            String dataString = DataService.INSTANCE.getLongDateTime() + System.getProperty("line.separator");
            System.out.println("--qydq--時間--" + dataString);
            byte[] req = dataString.getBytes("UTF-8");
            ByteBuf buf = Unpooled.buffer(req.length);
            buf.writeBytes(req);
            ctx.writeAndFlush(buf);
            Thread.sleep(2000);
        } else {
            System.out.println("--qydq--跑累了停止发送--" + sendding);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--qydq-- Server is inactive ---");
        // 10s 之后尝试重新连接服务器
        System.out.println("10s 之后尝试重新连接服务器...");
        Thread.sleep(10 * 1000);
        new DataSendClient(context).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String str = (String) msg;
        System.out.println("--qydq--客户端收到消息" + str + "context" + context.toString());
        Intent intent = new Intent(context, MainActivity.DataAcceptBroadcastReceiver.class);
        intent.setAction("DataAcceptBroadcastReceiver");
        context.sendBroadcast(intent);
        if (sendding) {
            channelActive(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client handler exceptionCaught");
        cause.printStackTrace();
        ctx.close();
    }
}
