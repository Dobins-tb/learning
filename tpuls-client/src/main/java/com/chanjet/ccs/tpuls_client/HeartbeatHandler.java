package com.chanjet.ccs.tpuls_client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.task.ReConnectTask;
import com.task.ReConnectToServerExecutor;

public class HeartbeatHandler implements Runnable{
	public static Logger logger = Logger.getLogger(HeartbeatHandler.class);
		
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			List<Channel> inactiveChannelList = new ArrayList<Channel>();
			
			Set<Channel> tmpChannelContainer = ChannelManager.getChannelContainer().keySet();
			logger.info(" 开始检测心跳， 当前Client 数量 : " + tmpChannelContainer.size());
			Iterator<Channel> iter = tmpChannelContainer.iterator();
			if (tmpChannelContainer.size() == 0) {
				try {
					TPlusClient.connectToProxy(5);
				} catch (Exception e) {
					logger.error("连接server", e);;
				}
			}
			
			while(iter.hasNext()) {
				Channel channel =  iter.next();
				if (channel.isActive()) {
					String heartbeatMessage = "0310PING";
					ByteBuf bufHeartbeatMsg = Unpooled.copiedBuffer(heartbeatMessage.getBytes());
					channel.writeAndFlush(bufHeartbeatMsg);
				} else {
					
					String verifyMsg = ChannelManager.getDomainByChannel(channel);
					logger.info("channel not active" + channel.toString() + "   " + verifyMsg);
					inactiveChannelList.add(channel);
					if (verifyMsg != null)  {
						VerifyMessage.releaseDomain(verifyMsg);
						ReConnectToServerExecutor.commitTask(new ReConnectTask(verifyMsg));
					}
				}

			}
			
			for (Channel ch : inactiveChannelList) {
				ch.close();
				ChannelManager.removeChannel(ch);
			}
			
			try {
				Thread.sleep(1000 * 10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
