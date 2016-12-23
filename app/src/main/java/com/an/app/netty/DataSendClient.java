package com.an.app.netty;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 模拟客户端不断从map获取下位机数据传到服务端
 */
public class DataSendClient extends Thread {
    private static String HOST = "192.168.0.24";
    //    private static final String HOST = "117.78.48.143";
    private static final int PORT = 6008;
    private Context context;
    private SharedPreferences sp;
    private boolean sendding = false;

    public DataSendClient(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("SuperActivity", Activity.MODE_PRIVATE);
        HOST = sp.getString("hostip", HOST);
        sendding = sp.getBoolean("sendding", false);
    }

    @Override
    public void run() {
        System.out.println("--qydq--正在發送數據--" + sendding);
        try {
            System.out.println("--qydq--跑呀跑跑呀跑");
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect() throws Exception {
        //配置客户端的NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        System.out.println("DataSendClient--qydq--IP地址" + HOST);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    //用LineBasedFrameDecode（回车换行符\n 或者 \r\n）解决TCP粘包问题
                                    .addLast(new LineBasedFrameDecoder(1024))
                                    .addLast(new StringDecoder())
                                    .addLast(new DataSendClientHandler(context));
                        }
                    });
            //发起异步连接操作
            ChannelFuture f = b.connect(HOST, PORT).sync();
            //等待客户端链路关闭
            f.channel().closeFuture().sync();
        } finally {
            //退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

//    public void sendDataToService() throws Exception {
//        new DataSendClient(context).connect();
//    }

}
