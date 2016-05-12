package com.chanjet.ccs.tpuls_client;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;

/**
 * 消息处理
* <p>Title: InvokeMessage</p>
* <p>Description: </p>
* @author Administrator 
* @date 2016年5月9日
 */
public class InvokeMessage {
	private static Logger logger = Logger.getLogger(InvokeMessage.class);
	private LoadResponseFile fileHandler;
	private Map<String, byte[]> urlMapping = new HashMap<String, byte[]>();
	
	public InvokeMessage() {
		fileHandler = new LoadResponseFile();
		urlMapping.put("/20k", fileHandler.getFileSize20K());
		urlMapping.put("/50k", fileHandler.getFileSize50K());
		urlMapping.put("/100k", fileHandler.getFileSize100k());
		urlMapping.put("/200k", fileHandler.getFileSize200k());
		urlMapping.put("/500k", fileHandler.getFileSize500k());
		
	}
	public String invokeProxyServerMessage(ChannelHandlerContext ctx, String message) {
		if (message.length() < 4) {
			logger.error("invaild message header");
		}
		String msgType = message.substring(0, 4);
		
		if ("0210".equals(msgType)) {
			if(TPlusClientForPerformance.startThread){
				TPlusClientForPerformance.startThread = false;
				new Thread(new HeartbeatHandler()).start();//心跳
			}
		} else if ("0510".equals(msgType)) {
			String msgId = message.substring(4, 40);
			String response = "0610" + msgId + invokeHttpRequest(message.substring(40, message.length()));
			return response;
		} else {
			
		}
		return null;
		
	}
	
	private String invokeHttpRequest(String httpRequestMessage) {
		if (httpRequestMessage == null) {
			return null;
		}
		String[] httpHeaders = httpRequestMessage.split("/\r/\n");
		for (String str : httpHeaders) {
			if (str.contains("GET") || str.contains("POST")) {
				String[] requestHeads = str.split(" ");
				String urlPath = requestHeads[1].trim();
				byte[] response = urlMapping.get(urlPath);
				if (response != null) {
					return new String(response);
				} 
			} else {
				continue;
			}
		}
		return defaultResponse();
	}
	
	private String defaultResponse() {
		
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
				+ "<title>\r\n" + "</title>" + "</head>\r\n";
		
		String s2 = "<body>\r\n%s" + "</body>\r\n" + "</html>\r\n";
		String s = s1 + s2;
		StringBuffer test = new StringBuffer("T+ Monitor Message");// body内容
		String s3 = String.valueOf(192 + test.toString().getBytes().length);// content-Length
		s = String.format(s, s3, test.toString());
		return s;
	}
	
	
}
