package com.chanjet.ccs.tpuls_client;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

public class TPlusClientHandler extends SimpleChannelInboundHandler<Object>{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(((String) msg).startsWith("02"))
		{
			if(((String) msg).contains("TRUE")){//0210TRUE
				//TPlusClientForPerformance.logger.info("连接成功 " + msg);//System.out.println("连接成功 " + msg);
				if(TPlusClientForPerformance.startThread){
					TPlusClientForPerformance.startThread = false;
					new Thread(new HeartbeatAndReconnect()).start();//心跳
				}
			}
			else{//0210FALSE
				System.out.println("连接失败"+msg);
			}
		}
		else if(((String) msg).startsWith("0410"))
		{
			//TPlusClientForPerformance.logger.info(msg);
		}
		else if(((String) msg).startsWith("0510"))
		{	
			String message = (String)msg;
						
			//浏览器会请求两次，其中没有用的那次请求中包含favicon.ico
//			if(message.contains("favicon.ico"))	return;
			
//			TPlusClientForPerformance.logger.info("\r\n===================收到http请求,msg====================\r\n"+msg+"=======================http请求接收完毕===========================\r\n");
			
			//如果请求文件
			if(message.contains("20k")||message.contains("50k")||message.contains("100k")||
					message.contains("200k")||message.contains("500k")
					//||message.contains("800k")
					)					
			{	
				String msgId = ((String) msg).substring(4, 40);//System.out.println("msgId: "+msgId);
				InputStream in = null;
				byte[] fb =null;
				
				if(message.contains("20k")){
					in = TPlusClientForPerformance.inptstrm20;
					fb = TPlusClientForPerformance.fileBuf20;
				}else if(message.contains("50k")){
					in = TPlusClientForPerformance.inptstrm50;
					fb = TPlusClientForPerformance.fileBuf50;
				}else if(message.contains("100k")){
					in = TPlusClientForPerformance.inptstrm100;
					fb = TPlusClientForPerformance.fileBuf100;
				}else if(message.contains("200k")){
					in = TPlusClientForPerformance.inptstrm200;
					fb = TPlusClientForPerformance.fileBuf200;
				}else if(message.contains("500k")){
					in = TPlusClientForPerformance.inptstrm500;
					fb = TPlusClientForPerformance.fileBuf500;
				}//else if(message.contains("800k")){
//					in = TPlusClientForPerformance.inptstrm800;
//					fb = TPlusClientForPerformance.fileBuf800;
//				}

				//生成前半段byte[]
				byte[] bFront = ("0610" + msgId).getBytes();
				
				//合并byte[]
				byte[] bResponse =new byte[bFront.length + fb.length];
				System.arraycopy(bFront, 0, bResponse, 0, bFront.length);
				System.arraycopy(fb, 0, bResponse, bFront.length, fb.length);
				
				//byte[]转成ByteBuf
				ByteBuf resp = Unpooled.copiedBuffer(bResponse);
				//TPlusClientForPerformance.logger.info("\r\n#################发送带文件http应答 response.length:"+resp.readableBytes()+"###############");
				
				//send
				ctx.channel().writeAndFlush(resp);//写.sync()发送比较大的文件，会报warn
//				System.out.println(msgId+":"+(++TPlusClientForPerformance.count));//计数
				
				resp.clear();	
				in.close();
				
			}else{
				String s1 = "HTTP/1.1 200 OK\r\n"
				+ "Cache-Control: private\r\n"
				+ "Content-Length: %s\r\n"
				+ "Content-Type: text/html; charset=utf-8\r\n"
				+ "Server: Microsoft-IIS/8.0\r\n"
				+ "X-AspNet-Version: 4.0.30319\r\n"
				+ "X-Powered-By: ASP.NET\r\n"
				+ "X-UA-Compatible: IE=EmulateIE8\r\n"
				+ "Date: Wed, 08 Oct 2014 07:32:24 GMT\r\n"
				+ "Connection: close\r\n\r\n\r\n\r\n"
				+ "<!DOCTYPE html>\r\n"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n"
				+ "<head>"
				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\r\n"
				+ "<title>\r\n"
				+ "</title>"
				+ "</head>\r\n";				
				String s2 = "<body>\r\n%s"
				+ "</body>\r\n"
				+ "</html>\r\n";
				String s = s1+s2;				
				StringBuffer test = new StringBuffer("T+ Monitor Message");//body内容
				String s3 = String.valueOf(192 + test.toString().getBytes().length);//content-Length
				s=String.format(s, s3, test.toString());
				String msgId = ((String) msg).substring(4, 40);
				String response = "0610" + msgId + s;//正常流程
//				String response ="";
//				TPlusClientForPerformance.logger.info("\r\n################# 发送http应答 #################\r\n"+response+"\r\n################### HTTP应答发送完毕 #############");
				ctx.channel().writeAndFlush(response);
				System.out.println(msgId+":"+(++TPlusClientForPerformance.count)+"非20,50,100,200,500");//计数
			}
		}
	}
}
