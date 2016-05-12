package com.chanjet.ccs.tpuls_client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;


/**
 * 加载资源文件
* <p>Title: LoadResponseFile</p>
* <p>Description: </p>
* @author Administrator 
* @date 2016年5月9日
 */
public class LoadResponseFile {
	public static Logger logger = Logger.getLogger(LoadResponseFile.class);
	
	private byte[] fileSize20K;
	private byte[] fileSize50K;
	private byte[] fileSize100k;
	private byte[] fileSize200k;
	private byte[] fileSize500k;
	
	public LoadResponseFile() {
		fileSize20K = initByteBuffer("/data/20k.html");
		fileSize50K = initByteBuffer("/data/50k.html");
		fileSize100k = initByteBuffer("/data/100k.html");
		fileSize200k = initByteBuffer("/data/200k.html");
		fileSize500k = initByteBuffer("/data/500k.html");
	}
	
	
	@SuppressWarnings("resource")
	public byte[] initByteBuffer(String filePath) {
		URL url = this.getClass().getResource(filePath);
		File file = new File(url.getFile());
		if(file.isFile()&&file.exists()&&!file.isHidden()&&file.canRead()){
			
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(file);
				byte[] bytebuffer = new byte[inputStream.available()];
				inputStream.read(bytebuffer);
				return bytebuffer;
			} catch (IOException e) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e1) {
						logger.error(e1);
					}
				}
				return null;
			}
		}else{
			logger.error("500k.html文件不存在或不可操作");
			return null;
		}
	}


	public byte[] getFileSize20K() {
		return fileSize20K;
	}


	public void setFileSize20K(byte[] fileSize20K) {
		this.fileSize20K = fileSize20K;
	}


	public byte[] getFileSize50K() {
		return fileSize50K;
	}


	public void setFileSize50K(byte[] fileSize50K) {
		this.fileSize50K = fileSize50K;
	}


	public byte[] getFileSize100k() {
		return fileSize100k;
	}


	public void setFileSize100k(byte[] fileSize100k) {
		this.fileSize100k = fileSize100k;
	}


	public byte[] getFileSize200k() {
		return fileSize200k;
	}


	public void setFileSize200k(byte[] fileSize200k) {
		this.fileSize200k = fileSize200k;
	}


	public byte[] getFileSize500k() {
		return fileSize500k;
	}


	public void setFileSize500k(byte[] fileSize500k) {
		this.fileSize500k = fileSize500k;
	}
	
}
