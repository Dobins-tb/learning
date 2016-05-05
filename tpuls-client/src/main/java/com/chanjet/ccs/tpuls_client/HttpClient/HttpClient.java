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
		StringBuilder sb = new StringBuilder("GET /hellopretty HTTP/1.1\r\n").append("  Host: 192.168.1.105:4002\r\n");
		sb.append("Connection: keep-alive")
		.append("Cache-Control: no-cache")
		.append("User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36");
		sb.append("Accept: */*");
		sb.append("Accept-Encoding: gzip, deflate, sdch");
		sb.append("Accept-Language: zh-CN,zh;q=0.8");
		sb.append("Content-Length: 0");
		
		StringBuilder mutilHttpRquest =  new StringBuilder();
		for(int i = 0; i < 5; i++) {
			mutilHttpRquest.append(sb.toString());
		}
		
		return mutilHttpRquest.toString();
	}
}
