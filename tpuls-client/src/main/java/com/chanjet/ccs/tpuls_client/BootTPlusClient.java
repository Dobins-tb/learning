package com.chanjet.ccs.tpuls_client;

public class BootTPlusClient {
	public static void main(String[] args) throws Exception {
//		TPlusClientForPerformance tPerformance = new TPlusClientForPerformance();
//		tPerformance.init();
		
		int tPlusClientNumber = 5;
		initClientDomain(tPlusClientNumber);
		
		TPlusClient.init("172.18.21.217", 8992, 5);
		TPlusClient.connectToProxy(tPlusClientNumber);
	}
	
	private static void initClientDomain(int clientNumber) {
		for (int i = 0; i < clientNumber; i++) {
			VerifyMessage.initDomain(i + VerifyMessage.domain1, true);
		}
	}
}
