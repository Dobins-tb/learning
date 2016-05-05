package com.chanjet.ccs.tpuls_client.HttpClient;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class HttpClientHandler implements Runnable{
	private static Logger log = LoggerFactory.getLogger(HttpClientHandler.class);
	private String ip;
	private Integer port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop = false;
	
	public HttpClientHandler(String ip, Integer port) {
		this.ip = ip;
		this.port = port;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			log.info("open socket channel error", e);
			System.exit(1);
		}
	}
	@Override
	public void run() {
		try {
			doConnect();
			
			while(!stop) {
				selector.select(1000);
				Set<SelectionKey> selectKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectKeys.iterator();
				SelectionKey key = null;
				
				while (iter.hasNext()) {
					key = iter.next();
					iter.remove();
					try {
						handlerInput(key);
					} catch (Exception e) {
						log.error("handler input error", e);
						if (key != null) {
							key.cancel();
							if (key.channel() != null) {
								key.channel().close();
							}
						}
					}
				}

			}
		} catch (IOException e) {
			log.error("something happened", e);
		}
		
		
		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("close selector error", e);
			}
		}
		
	}
	
	private void handlerInput(SelectionKey key) throws ClosedChannelException, IOException {
		if (key.isValid()) {
			SocketChannel channel = (SocketChannel) key.channel();
			if (key.isConnectable()) {
				if (channel.finishConnect()) {
					doWrite(channel);
				} else {
					log.error("connect failed , over cycle");
					this.stop = true;
				}
			}
			
			if (key.isReadable()) {
				doRead(channel, key);
			}
		}
	}
	
	private void doRead(SocketChannel ch, SelectionKey key) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int readBytes = ch.read(buffer);
		if (readBytes > 0) {
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			log.info("read bytes===" + (new String(bytes, CharsetUtil.UTF_8)));
		} else if (readBytes < 0) {
			key.cancel();
			ch.close();
		} else {
			
		}
		
		doWrite(socketChannel);
	}
	
	private void doWrite(SocketChannel channel) {
		ByteBuffer buff = ByteBuffer.allocate(2048);
		buff.put(HttpClient.httpBody.getBytes());
		buff.flip();
		try {
			channel.write(buff);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void doConnect() throws IOException {
		if (socketChannel.connect(new InetSocketAddress(ip, port))) {
			log.info("connect to server success");
			socketChannel.register(selector, SelectionKey.OP_READ);
		} else {
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}

}
