package com.chanjet.ccs.tpuls_client;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class VerifyMessage {
	private static String appkey1 = "hahaha";
	private static String uuid1 = "b3df71d4-7b2b-4a86-a490-f7155b0a1a2b";//"c654e2cf-6132-4269-832d-babe1aacab60";
	
	private static String orgid1 = "60004164471";
	private static String token1 = "aca4ea39-24ab-4c89-8637-29878f31b155";//"9c6a92a4-1e6c-4c25-b295-2c40d1c32be1";	
	public static String domain1 = "tplusclient.free.tpluscloud.com";
	
	static String verifyMsg = String.format("0010{orgid:'%s',token:'%s','domain':'%s','uuid':'%s',appkey:'%s'}",orgid1,token1,domain1,uuid1,appkey1);
	
	public static String createVerifyMsg(String userDomain) {
		return String.format("0010{orgid:'%s',token:'%s','domain':'%s','uuid':'%s',appkey:'%s'}",orgid1,token1,userDomain,uuid1,appkey1);
	}
	
	/**
	 * Map value=true,表示当前 可用， false表示不可用
	 */
	private static Map<String, Boolean> domainPool = new HashMap<String, Boolean>();
	
	public static void initDomain(String domain, Boolean flag) {
		domainPool.put(domain, flag);
	}
	
    public static synchronized String chooseAvalibaleDomain() {
    	for(Entry<String, Boolean> entry : VerifyMessage.domainPool.entrySet()) {
    		if (entry.getValue() == true) {
    			VerifyMessage.domainPool.put(entry.getKey(), false);
    			return entry.getKey();
    		}
    	}
    	return null;
    }
    
    public static synchronized void releaseDomain(String domain) {
    	domainPool.put(domain, true);
    }
}
