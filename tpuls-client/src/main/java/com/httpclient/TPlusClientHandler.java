package com.httpclient;

import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.chanjet.ccs.tpuls_client.ChannelManager;
import com.chanjet.ccs.tpuls_client.InvokeMessage;
import com.chanjet.ccs.tpuls_client.TPlusClient;
import com.chanjet.ccs.tpuls_client.VerifyMessage;
import com.task.ReConnectTask;
import com.task.ReConnectToServerExecutor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
* <p>Title: TPlusClientHandler</p>
* <p>Description: </p>
* @author Administrator 
* @date 2016年5月9日
 */
public class TPlusClientHandler extends ChannelInboundHandlerAdapter {
	private static Logger logger = Logger.getLogger(TPlusClientHandler.class);
	private InvokeMessage invokeMessage; 
	
	public TPlusClientHandler() {
		invokeMessage = new InvokeMessage();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		
		String verifyMsg = VerifyMessage.chooseAvalibaleDomain();
		logger.info(" connected to server, domain: " + verifyMsg + "channel : " + ctx.channel().toString());
		ChannelManager.addChannel(ctx.channel(), verifyMsg);
		ctx.writeAndFlush(VerifyMessage.createVerifyMsg(verifyMsg));
	}
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      
		if ("0210TRUE".equals(msg.toString())) {
			logger.info(msg.toString());
		}
		String response = invokeMessage.invokeProxyServerMessage(ctx, msg.toString());
		if (response != null) {
			ByteBuf resp = Unpooled.copiedBuffer(response.getBytes());
			logger.info("write message to proxy server##############################################################" + response.length());
			ctx.writeAndFlush(resp);
		}

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
       ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.error("channel exception ", cause);
    	ctx.close();
    }
   
}
