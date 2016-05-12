
package com.chanjet.ccs.tpuls_client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
/**
 * 该T+端用于性能测试，仅对含20k，50k，100k，200k，500k的URL做出响应。
 * 相应的Response大小为20k，50k，100k，200k，500k。
 */
public class TPlusClientForPerformance {
	public static Logger logger = Logger.getLogger(TPlusClientForPerformance.class);
	
	private static Bootstrap b = new Bootstrap();
	
//	private final static String HOST = "182.92.245.70";//"172.18.2.210";//"172.18.22.35";//"172.18.22.34";//"10.11.64.16";//"172.18.9.202";
	
	private final static String HOST = "172.18.21.217";
	private final static int PORT = 8992;
	private static String appkey1 = "hahaha";
	private static String uuid1 = "b3df71d4-7b2b-4a86-a490-f7155b0a1a2b";//"c654e2cf-6132-4269-832d-babe1aacab60";

//	private static String orgid1 = "60000198401";
//	private static String token1 = getToken("conf/60000198401token.txt");
//	private static String domain1 = "ugadx6b1b198401l.tpluscloud.com";
//	private static String verifyMsg = String.format("0010{orgid:'%s',token:'%s','domain':'%s','uuid':'UUID',appkey:'TPlus'}",orgid,token,domain);
	
//	private static String orgid1 = "60000714415";
//	private static String token1 = getToken("conf/60000714415token.txt");//"9c6a92a4-1e6c-4c25-b295-2c40d1c32be1";	
//	private static String domain1 = "ugadx6b1b00l.tpluscloud.com";
//	private static String verifyMsg = String.format("0010{orgid:'%s',token:'%s','domain':'%s','uuid':'UUID',appkey:'TPlus'}",orgid1,token1,domain1,uuid1,appkey1);

//	private static String orgid1 = "60000222222";
//	private static String token1 = "2c2a22a2-2e2c-2c22-b222-2c22d2c22be2";	
//	private static String domain1 = "ugadx6b1b22l.tpluscloud.com";
//	private static String verifyMsg = String.format("0010{orgid:'%s',token:'%s','domain':'%s','uuid':'UUID',appkey:'TPlus'}",orgid1,token1,domain1,uuid1,appkey1);

//	private static String orgid1 = "60000333333";
//	private static String token1 = "3c3a33a3-3e3c-3c33-b333-3c33d3c33be3";	
//	private static String domain1 = "ugadx6b1b33l.tpluscloud.com";
//	private static String verifyMsg = String.format("0010{orgid:'%s',token:'%s','domain':'%s','uuid':'UUID',appkey:'TPlus'}",orgid1,token1,domain1,uuid1,appkey1);

//	private static String orgid1 = "60000444444";
//	private static String token1 = "4c4a44a4-4e4c-4c44-b444-4c44d4c44be4";	
//	private static String domain1 = "ugadx6b1b44l.tpluscloud.com";
//	private static String verifyMsg = String.format("0010{orgid:'%s',token:'%s','domain':'%s','uuid':'UUID',appkey:'TPlus'}",orgid1,token1,domain1,uuid1,appkey1);


	private static String orgid1 = "60004164471";
	private static String token1 = "aca4ea39-24ab-4c89-8637-29878f31b155";//"9c6a92a4-1e6c-4c25-b295-2c40d1c32be1";	
	private static String domain1 = "ugadx6b1b55l.free.tpluscloud.com";
	
	static String verifyMsg = String.format("0010{orgid:'%s',token:'%s','domain':'%s','uuid':'%s',appkey:'%s'}",orgid1,token1,domain1,uuid1,appkey1);
	
	private static ByteBuf bufVerfyMsg = Unpooled.copiedBuffer(verifyMsg.getBytes());//连接验证消息的ByteBuf形式
	
	//将每个channel都放入Map中供线程逐条读取后，发心跳or重连
	public static HashMap<Channel,String> connMap = new HashMap<Channel,String>();
	
	//线程开关：因为线程是遍历Map里每一个channel并发心跳，所以只要有一个channel接到0200TRUE启动了线程，后续的不用再启了
	public static boolean startThread = true;
	public static int HEARTBEAT = 10000;
	public static String heartbeatMessage="0310PING";
	
	//true：将msg进行字符串编码和解码传输；false：上传文件时需要msg使用最原始的ByteBuf传输。注意：不同的消息类型handler使用不同的基类。
//	private static final boolean useStringCode = true;	
	
	//收到请求的计数器
	public static int count = 0;
	
	//load好文件后，这些变量供handler使用
	public static InputStream inptstrm20,inptstrm50,inptstrm100,inptstrm200,inptstrm500;//inptstrm800;
	public static byte[] fileBuf20,fileBuf50,fileBuf100,fileBuf200,fileBuf500;//,fileBuf800
	
	public TPlusClientForPerformance(){
		EventLoopGroup wokrer = new NioEventLoopGroup();
		
		b.group(wokrer);
		b.channel(NioSocketChannel.class);
		b.handler(new ChannelInitializer<SocketChannel>(){
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
	
				//对于过长内容Netty会分包多次发送，以下语句可以使接收端按照decoder指定的长度Integer.MAX_VALUE=2147483647Byte=2G接收完整后才会调用handler继续处理信息
				//参数：信息最大长度，长度属性起始位从0起，长度属性的长度为4字节，？，跳过的字节数
				ch.pipeline().addLast("decoder", new LengthFieldBasedFrameDecoder(2147483647, 0, 4, 0, 4));//设置定长解码器，长度为2G
				ch.pipeline().addLast("encoder", new LengthFieldPrepender(4, false));//长度是否包含长度属性的长度
//				if(useStringCode){
					ch.pipeline().addLast(new StringDecoder());//设置字符串解码器 自动将二进制转为字符串
					ch.pipeline().addLast(new StringEncoder());//设置字符串编码器 自动将字符串转为二进制				
					ch.pipeline().addLast(new HttpResponseDecoder());
					
					ch.pipeline().addLast(new ChunkedWriteHandler());  //big file
					
					ch.pipeline().addLast(new TPlusClientHandler());
//				}else{	
//					ch.pipeline().addLast(new TPlusClientUploadHandler());//去除httpClient上传文件时，datahttp在http请求前面加上的0610和msgID。
//					ch.pipeline().addLast(new HttpRequestDecoder());//将去除了0610、msgID的二进制流（类型为ByteBuf）进行httpRequest编码。
//					ch.pipeline().addLast(new HttpObjectAggregator(1024*1024*100));//HttpObjectAggregator 这个handler就是将同一个http请求或响应的多个消息对象变成一个 fullHttpRequest完整的消息对象（大小不超过100M）
//					
//					ch.pipeline().addLast(new TPlusClientUploadHandler2());//从编码后的http请求中提取文件流并保存为文件。					
//				}
			}}
		);
		b.option(ChannelOption.SO_KEEPALIVE, true);  
        b.option(ChannelOption.TCP_NODELAY, true);  
        b.option(ChannelOption.SO_RCVBUF, 1024*1024*200);	
	}
	
	public void init() throws Exception {
		//PropertyConfigurator.configure("log4j.properties");//指定log的配置文件	
		PropertyConfigurator.configure(this.getClass().getResource("/"));
		
		//加载好html文件，供handler使用（每次来了连接即在new handler的时候才加载文件，会影响性能，并且文件加载一次就够了）
		loadFile20();		System.out.println("load file size: "+fileBuf20.length);
		loadFile50();		System.out.println("load file size: "+fileBuf50.length);
		loadFile100();		System.out.println("load file size: "+fileBuf100.length);
		loadFile200();		System.out.println("load file size: "+fileBuf200.length);
		loadFile500();		System.out.println("load file size: "+fileBuf500.length);
//		800k is to much，a single machine with limited bandwidth cannot afford this.
//		loadFile800();		System.out.println("load file size: "+fileBuf800.length);
		
		//TPlusClientForPerformance TClient = new TPlusClientForPerformance();		
		connectDataExchange();
	}
	
	private  String getToken(String filePath){
		FileReader fr = null;
		BufferedReader br = null;
		String token = null;
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr);
			token = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!(fr==null)){
			try {
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!(br==null)){
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return token;
	}
	private void loadFile20(){
		System.out.println(this.getClass().getResource("/data/20k.html"));
		URL url = this.getClass().getResource("/data/20k.html");
		File file = new File(url.getFile());
		if(file.isFile()&&file.exists()&&!file.isHidden()&&file.canRead()){
			try {
				inptstrm20 = new FileInputStream(file);
				fileBuf20 = new byte[inptstrm20.available()];
				inptstrm20.read(fileBuf20);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					inptstrm20.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}else{
			System.out.println("20k.html文件不存在或不可操作");
			return;
		}
	}
	private  void loadFile50(){
		URL url = this.getClass().getResource("/data/50k.html");
		File file = new File(url.getFile());
		if(file.isFile()&&file.exists()&&!file.isHidden()&&file.canRead()){
			try {
				inptstrm50 = new FileInputStream(file);
				fileBuf50 = new byte[inptstrm50.available()];
				inptstrm50.read(fileBuf50);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					inptstrm50.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}else{
			System.out.println("50k.html文件不存在或不可操作");
			return;
		}
	}
	private  void loadFile100(){
		URL url = this.getClass().getResource("/data/50k.html");
		File file = new File(url.getFile());
		if(file.isFile()&&file.exists()&&!file.isHidden()&&file.canRead()){
			try {
				inptstrm100 = new FileInputStream(file);
				fileBuf100 = new byte[inptstrm100.available()];
				inptstrm100.read(fileBuf100);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					inptstrm100.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}else{
			System.out.println("100k.html文件不存在或不可操作");
			return;
		}
	}
	private  void loadFile200(){
		URL url = this.getClass().getResource("/data/200k.html");
		File file = new File(url.getFile());
		if(file.isFile()&&file.exists()&&!file.isHidden()&&file.canRead()){
			try {
				inptstrm200 = new FileInputStream(file);
				fileBuf200 = new byte[inptstrm200.available()];
				inptstrm200.read(fileBuf200);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					inptstrm200.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}else{
			System.out.println("200k.html文件不存在或不可操作");
			return;
		}
	}
	private  void loadFile500(){
		URL url = this.getClass().getResource("/data/500k.html");
		File file = new File(url.getFile());
		if(file.isFile()&&file.exists()&&!file.isHidden()&&file.canRead()){
			try {
				inptstrm500 = new FileInputStream(file);
				fileBuf500 = new byte[inptstrm500.available()];
				inptstrm500.read(fileBuf500);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					inptstrm500.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}else{
			System.out.println("500k.html文件不存在或不可操作");
			return;
		}
	}
//	private static void loadFile800(){
//		File file = new File("files/800k.html");
//		if(file.isFile()&&file.exists()&&!file.isHidden()&&file.canRead()){
//			try {
//				inptstrm800 = new FileInputStream(file);
//				fileBuf800 = new byte[inptstrm800.available()];
//				inptstrm800.read(fileBuf800);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				try {
//					inptstrm800.close();
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//			}
//		}else{
//			System.out.println("800k.html文件不存在或不可操作");
//			return;
//		}
//	}
	
	public void connectDataExchange() throws Exception{

		logger.info("发起连接："+verifyMsg);
		Channel channel = b.connect(HOST,PORT).sync().channel();//必须sync，否则没那么快active
		if(channel.isActive()){
//			if(useStringCode)
				channel.writeAndFlush(verifyMsg).sync();
//			else
//				channel.writeAndFlush(bufVerfyMsg).sync();
			connMap.put(channel, verifyMsg);
		}
	}
	
	/*
	 * 重连Exchange
	 */
	public static Channel reconnectDataExchange(String verifyMessage){
		logger.info("发起重连:" + verifyMessage);
		Channel ch =null;
		try{
			ch=b.connect(HOST, PORT).sync().channel();//必须sync，否则没那么快active
			if (ch.isActive())
//				if(useStringCode)
					ch.writeAndFlush(verifyMessage);
//				else
//					ch.writeAndFlush(Unpooled.copiedBuffer(verifyMessage.getBytes()));
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return ch;
	}
}


class HeartbeatAndReconnect implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			Iterator<Channel> chs = TPlusClientForPerformance.connMap.keySet().iterator();
			List<Channel> inactiveChannelList = new ArrayList<Channel>();
			HashMap<Channel,String> newConnMap = new HashMap<Channel,String>();
			while(chs.hasNext()){
				Channel ch = chs.next();
				String connInfo = TPlusClientForPerformance.connMap.get(ch);
				if(ch.isActive()){
					String heartbeatMsg = TPlusClientForPerformance.heartbeatMessage;
					ByteBuf bufHeartbeatMsg = Unpooled.copiedBuffer(heartbeatMsg.getBytes());
					ch.writeAndFlush(bufHeartbeatMsg);
					TPlusClientForPerformance.logger.info(heartbeatMsg + " " + connInfo);//System.out.println("发心跳  " + heartbeatMsg + "  " + connInfo);
				}else{
					//重连
					TPlusClientForPerformance.logger.info("连接失活，需重连...");//System.out.println("连接失活，需重连...");	
					Channel newChannel = TPlusClientForPerformance.reconnectDataExchange(connInfo);//重连
					if(newChannel!=null && newChannel.isActive()){
						inactiveChannelList.add(ch);//已断的连接加入List
						newConnMap.put(newChannel, connInfo);//重连后的新连接存入Map
					}
				}
			}
			//移除inactive的channel
			for(int i=0; i<inactiveChannelList.size(); i++){
				TPlusClientForPerformance.connMap.remove(inactiveChannelList.get(i));
			}
			
			//加入重连后的新channel
			Iterator<Channel> newChs = newConnMap.keySet().iterator();
			while(newChs.hasNext()){
				Channel newCh = newChs.next();
				String newConnStr = newConnMap.get(newCh);
				TPlusClientForPerformance.connMap.put(newCh, newConnStr);
			}
			
			try{
				Thread.sleep(TPlusClientForPerformance.HEARTBEAT);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
}
