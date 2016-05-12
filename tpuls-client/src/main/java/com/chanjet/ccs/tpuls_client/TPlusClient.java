package com.chanjet.ccs.tpuls_client;


import org.apache.log4j.Logger;

import com.httpclient.TPlusClientHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class TPlusClient {
	public static Logger logger = Logger.getLogger(TPlusClient.class);
	
	private static Bootstrap b;
	private static String inetHost;
	private static Integer inetPort;
	public static void init(String host, Integer port, Integer nThreads) {
		inetHost = host;
		inetPort = port;
		b = new Bootstrap();
		EventLoopGroup worker = new NioEventLoopGroup(nThreads);
		b.group(worker);
		b.channel(NioSocketChannel.class);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast("decoder", new LengthFieldBasedFrameDecoder(2147483647, 0, 4, 0, 4));// 设置定长解码器，长度为2G
				ch.pipeline().addLast("encoder",new LengthFieldPrepender(4, false));// 长度是否包含长度属性的长度
				ch.pipeline().addLast(new StringDecoder());// 设置字符串解码器
				ch.pipeline().addLast(new StringEncoder());// 设置字符串编码器
				ch.pipeline().addLast(new HttpResponseDecoder());
				ch.pipeline().addLast(new ChunkedWriteHandler()); // big file
				ch.pipeline().addLast(new TPlusClientHandler());
			}
		});
		
		b.option(ChannelOption.SO_KEEPALIVE, true);  
        b.option(ChannelOption.TCP_NODELAY, true);  
        b.option(ChannelOption.SO_RCVBUF, 1024*1024*200);
        
 //       worker.shutdownGracefully();
	}
	
	private static Channel doConnect() {
		Channel channel = null;
		try {
			channel = b.connect(inetHost, inetPort).sync().channel();
		} catch (InterruptedException e) {
			logger.error("异步连接error", e);
		}
		return channel;
	}
	
	public static void connectToProxy(Integer clientNumber) throws Exception{
		if (clientNumber == null || clientNumber <= 0) {
			throw new Exception(" invalid parameter clientNumber");
		}
		for (int counter = 0; counter < clientNumber; counter++) {
			Channel channel = doConnect();
			ChannelManager.addChannel(channel, counter + VerifyMessage.domain1);
		}
		

	}
	
	public static void reConnectToProxy(String verifyMsg) {
		
		try {
			logger.info("statrt reconnect to proxy server ");
			Channel channel = doConnect();
			ChannelManager.addChannel(channel, verifyMsg);
			logger.info("t+ Client reconnect to proxy server success" + verifyMsg);
		} catch (Exception e) {
			logger.error("重连失败", e);
		} catch (Throwable cause) {
			logger.error("重连失败", cause);
		}
		

		
	}
}
