package com.chanjet.ccs.tpuls_client.HttpClient;

public class HttpClient {
	public static String httpBody = buildHttpBody();

	public static void main(String[] args) {
		HttpClientHandler client = new HttpClientHandler("192.168.1.105", 4002);

		Thread thread = new Thread(client);
		thread.start();

		try {
			Thread.sleep(1000 * 60 * 1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String buildHttpBody() {
		StringBuilder sb = new StringBuilder("GET /hellopretty HTTP/1.1 \n\n").append("  Host: 192.168.1.105:4002 \n\n");
		sb.append("Connection: keep-alive \n\n")
		.append("Cache-Control: no-cache \n\n")
		.append("User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36 \n\n");
		sb.append("Accept: */* \n\n");
		sb.append("Accept-Encoding: gzip, deflate, sdch \n\n");
		sb.append("Accept-Language: zh-CN,zh;q=0.8 \n\n");
		sb.append("Content-Length: 0 \n\n");
		
		StringBuilder mutilHttpRquest =  new StringBuilder();
		for(int i = 0; i < 1; i++) {
			mutilHttpRquest.append(sb.toString());
		}
		
		return mutilHttpRquest.toString();
	}
}
