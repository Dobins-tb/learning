package com.chanjet.ccs.tpuls_client;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {
	private static Map<Channel, String> channelContainer = new ConcurrentHashMap<Channel, String>();
	
	public static void addChannel(Channel channel, String domain) {
		channelContainer.put(channel, domain);
	}
	
	public static void removeChannel(Channel channel) {
		channelContainer.remove(channel);
	}
	
	public static String getDomainByChannel(Channel channel) {
		return channelContainer.get(channel);
	}
	
	public static int getContainerSize() {
		return channelContainer.size();
	}
	
	public static Map<Channel, String> getChannelContainer() {
		return channelContainer;
	}
}
