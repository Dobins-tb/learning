package com.task;

import com.chanjet.ccs.tpuls_client.TPlusClient;

/**
 * 重连task
* <p>Title: ReConnectTask</p>
* <p>Description: </p>
* @author Administrator 
* @date 2016年5月11日
 */
public class ReConnectTask implements Runnable {
	private String verifyMsg;
	
	public ReConnectTask(String verifyMsg) {
		this.verifyMsg = verifyMsg;
	}
	@Override
	public void run() {
		TPlusClient.reConnectToProxy(verifyMsg);
	}

}
