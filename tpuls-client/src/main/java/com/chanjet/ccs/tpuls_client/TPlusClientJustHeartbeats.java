package com.chanjet.ccs.tpuls_client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * 该T+端用于性能测试。
 * 仅与Exchange心跳+重连。
 * 需要关掉exchange的cia验证（.../dataexchange/src/main/resources/properties-line/bsscia.properties设置USE_AUTHORIZED=false）
 * 【注意】如果ex跑在虚机或者共享网卡的机器上，会有网络瓶颈，则有可能netstat -an|grep 8992|wc -l 的连接数量上不去，这边发完number个请求后，一大堆失活的连接将出现。
 * @author Li Jie
 *
 */
public class TPlusClientJustHeartbeats {
	public static Logger logger = Logger.getLogger(TPlusClientJustHeartbeats.class);
			
	private static Bootstrap b = new Bootstrap();	
	private final static String HOST = "182.92.245.70";//"172.18.4.60";//"172.18.22.34";//"172.18.4.61";//"172.18.2.210";//"172.18.22.35";//"10.11.64.16";//"172.18.9.202";
	private final static int PORT = 8992;
	
	private static int number = 2500;//T+端数量
	public static boolean done = false;
	private static String domain = "http://%s.free.tpluscloud.com/";
	private static String[] orgids = new String[number];
	private static String[] tokens = new String[number];
	private static String[] domains = new String[number];
	private static String verifyMsg = "0010{orgid:'%s',token:'%s','domain':'%s','uuid':'UUID',appkey:'TPlus'}";
	
	//将每个channel都放入Map中供线程逐条读取后，发心跳or重连
	public static HashMap<Channel,String> connMap = new HashMap<Channel,String>();
	
	//线程开关：因为线程是遍历Map里每一个channel并发心跳，所以只要有一个channel接到0200TRUE启动了线程，后续的不用再启了
	public static boolean startThread = true;
	public static int HEARTBEAT = 40000;
	public static String heartbeatMessage="0310PING";
	
	public TPlusClientJustHeartbeats(){
		EventLoopGroup group = new NioEventLoopGroup();
		
		b.group(group);
		b.channel(NioSocketChannel.class);
		b.handler(new ChannelInitializer<SocketChannel>(){
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
	
				//对于过长内容Netty会分包多次发送，以下语句可以使接收端按照decoder指定的长度Integer.MAX_VALUE=2147483647Byte=2G接收完整后才会调用handler继续处理信息
				//参数：信息最大长度，长度属性起始位从0起，长度属性的长度为4字节，？，跳过的字节数
				ch.pipeline().addLast("decoder", new LengthFieldBasedFrameDecoder(2147483647, 0, 4, 0, 4));//设置定长解码器，长度为2G
				ch.pipeline().addLast("encoder", new LengthFieldPrepender(4, false));//长度是否包含长度属性的长度
				ch.pipeline().addLast(new StringDecoder());//设置字符串解码器 自动将二进制转为字符串
				ch.pipeline().addLast(new StringEncoder());//设置字符串编码器 自动将字符串转为二进制				
				ch.pipeline().addLast(new HttpResponseDecoder());
				
				ch.pipeline().addLast(new TPlusClientJustHeartbeatHandler());
			}}
		);
		b.option(ChannelOption.SO_KEEPALIVE, true);  
        b.option(ChannelOption.TCP_NODELAY, true);  
        b.option(ChannelOption.SO_RCVBUF, 1024*1024*200);
	}

	/**
	 * 初始化测试数据数组：orgids，tokens和domains
	 */
	private static void initialParameters(){
		for(int i=0; i<number; i++){
			String zerofill=String.valueOf(i);
			if(i<10){
				zerofill = "000"+i;
			}else if((i>9)&(i<100)){
				zerofill = "00"+i;
			}else if((i>99)&(i<1000)){
				zerofill = "0"+i;
			}

			orgids[i] = "60000761"+zerofill;			
			tokens[i] = "9c6a92a4-1e6c-4c25-b295-2c40d1c61"+zerofill;
			domains[i] = String.format(domain, "test61"+zerofill);
		}
	}
	
	private void connectExchange(String verifyMessage) throws InterruptedException{
//		logger.info("发起连接："+verifyMessage);
		Channel channel = b.connect(HOST, PORT).sync().channel();//必须sync，否则没那么快active
		if(channel.isActive()){
			channel.writeAndFlush(verifyMessage).sync();			
		}else{
			logger.info("notActive"+" "+verifyMessage);
		}
		connMap.put(channel, verifyMessage);
	}
	
	
	public static Channel reconnectExchange(String verifyMessage) throws InterruptedException{
		logger.info("发起重连："+verifyMessage);
		Channel ch =null;
		ch = b.connect(HOST, PORT).sync().channel();//必须sync，否则没那么快active
		if(ch.isActive()) 
			ch.writeAndFlush(verifyMessage).sync();
		return ch;
	}
	
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
		PropertyConfigurator.configure("conf/log4j.properties");//指定log的配置文件
		logger.info("注意 关掉 exchange的CIA验证");
		initialParameters();
		
		TPlusClientJustHeartbeats client = new TPlusClientJustHeartbeats();
		for(int i=0;i<number;i++){
			String s = String.format(verifyMsg, orgids[i],tokens[i],domains[i]);
			client.connectExchange(s);
			Thread.sleep(20);
			System.out.println(i+" done");
		}
		done = true;
	}

}

class Heartbeats implements Runnable{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			Iterator<Channel> chs = TPlusClientJustHeartbeats.connMap.keySet().iterator();
			List<Channel> inactiveChannelList = new ArrayList<Channel>();
			HashMap<Channel,String> newConnMap = new HashMap<Channel,String>();
			while(chs.hasNext()){
				Channel ch = chs.next();
				String verifyMsg = TPlusClientJustHeartbeats.connMap.get(ch);
				if(ch.isActive()){
					String ping = TPlusClientJustHeartbeats.heartbeatMessage;
					ch.writeAndFlush(ping);
//					TPlusClientJustHeartbeats.logger.info(ping+"  "+verifyMsg);
				}else{
					try {
						Channel newChannel = TPlusClientJustHeartbeats.reconnectExchange(verifyMsg);
						TPlusClientJustHeartbeats.logger.info("连接失活，需重连...");
						if(newChannel!=null&&newChannel.isActive()){
							inactiveChannelList.add(ch);//已断的连接加入List
							newConnMap.put(newChannel, verifyMsg);//重连后的新连接存入Map
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//移除inactive的channel
			for(int i=0; i<inactiveChannelList.size(); i++){
				TPlusClientJustHeartbeats.connMap.remove(inactiveChannelList.get(i));
			}
			//加入重连后的新channel
			Iterator<Channel> newChs = newConnMap.keySet().iterator();
			while(newChs.hasNext()){
				Channel newCh = newChs.next();
				String newConnStr = newConnMap.get(newCh);
				TPlusClientJustHeartbeats.connMap.put(newCh, newConnStr);
			}
			
			try{
				Thread.sleep(TPlusClientJustHeartbeats.HEARTBEAT);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
}

class TPlusClientJustHeartbeatHandler extends SimpleChannelInboundHandler<Object>{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		// TODO Auto-generated method stub
		if(((String)msg).startsWith("02")){
			if(((String)msg).contains("TRUE")){
//				TPlusClientJustHeartbeats.logger.info("连接成功"+msg);
				Thread.sleep(2000);
				//轮询主程序是否启动完毕若干个T+端,未启动完毕则return，即最后一个启动的T+收到0210true后，启动心跳线程(即connMap已加满全部的channel)
				if(!TPlusClientJustHeartbeats.done) return;
				TPlusClientJustHeartbeats.done = false;
				System.out.println("0210 is DONE！");
				if(TPlusClientJustHeartbeats.startThread){
					TPlusClientJustHeartbeats.startThread=false;
					System.out.println("I am in and start heartbeat!");
					new Thread(new Heartbeats()).start();
				}
				
			}else{
				TPlusClientJustHeartbeats.logger.info("连接失败"+msg);
			}
		}
	}
	
}
