package com.an.app.netty;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.an.base.utils.DataService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DataSendClientHandler extends ChannelInboundHandlerAdapter {

    private int count = 0;
    private Context context;
    private SharedPreferences sp;
    private boolean sendding = true;


    public DataSendClientHandler(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("SuperActivity", Activity.MODE_PRIVATE);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        sendding = sp.getBoolean("sendding", false);
       /* while (sendding) {
            // 客户端不断从发送数据发送给服务端

            System.out.println("--qydq--channelActive--" + sendding);
            String dataString = DataService.INSTANCE.getLongDateTime() + System.getProperty("line.separator");
            System.out.println("--qydq--時間--" + dataString);
            sendding = false;
//            if (sendding) {
            byte[] req = dataString.getBytes("UTF-8");
            ByteBuf buf = Unpooled.buffer(req.length);
            buf.writeBytes(req);
            ctx.writeAndFlush(buf);

            Thread.sleep(3000);


//            }else{
//                System.out.println("--qydq--等待--" + dataString);
//            }
        }*/




        String dataString = DataService.INSTANCE.getLongDateTime() + System.getProperty("line.separator");
        System.out.println("--qydq--時間--" + dataString);
//        sendding = false;
//            if (sendding) {
        byte[] req = dataString.getBytes("UTF-8");
        ByteBuf buf = Unpooled.buffer(req.length);
        buf.writeBytes(req);
        ctx.writeAndFlush(buf);
        System.out.println("--qydq--fasong shuj --" + buf);
        Thread.sleep(1000);

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
//        sendding = true;
        System.out.println("--qydq--客户端收到消息" + msg + "www" + sendding);
        String str = (String) msg;
        System.out.println("--qydq--客户端收到消息" + str + " " + ++count);
        channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client handler exceptionCaught");
        cause.printStackTrace();
        ctx.close();
    }
}
