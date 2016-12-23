package com.an.app.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class SaveRevicedDataServer extends Thread{

	private static final int PORT = 6008;
	ServerBootstrap b = new ServerBootstrap();
	
	@Override
	public void run() { 
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    	EventLoopGroup workGroup = new NioEventLoopGroup();
    	ServerBootstrap b = new ServerBootstrap();
        try {
             b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LineBasedFrameDecoder(1024))
                                    .addLast(new StringDecoder())
                                    .addLast(new SaveRevicedDataServerHandler());
                        }
                    });
         // Start the server.通过调用sync同步方法阻塞直到绑定成功
 			ChannelFuture f = b.bind(PORT).sync();
 			f.channel().closeFuture().sync(); 
         
        } catch (InterruptedException e) {
			e.printStackTrace();
		} 
        finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
	
	
	public static void main(String[] args) throws Exception {
		SaveRevicedDataServer test = new SaveRevicedDataServer();
		test.start();
	}
 
}