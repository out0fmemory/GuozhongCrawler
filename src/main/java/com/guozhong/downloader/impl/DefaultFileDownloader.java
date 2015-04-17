package com.guozhong.downloader.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.guozhong.Request;
import com.guozhong.Status;
import com.guozhong.downloader.FileDownloader;
import com.guozhong.exception.NoFilePathException;
import com.guozhong.thread.CountableThreadPool;

public final class DefaultFileDownloader implements FileDownloader, Runnable {
	private final Logger log = Logger.getLogger(DefaultFileDownloader.class);

	public static final String SAVE_FILE_NAME = "save_file_name";

	private final BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<Request>();

	private int delayTime;

	private long lastDownloadTime;

	private CountableThreadPool downloadThreadPool;

	public DefaultFileDownloader(int threadNum) {
		downloadThreadPool = new CountableThreadPool(threadNum + 1);
		downloadThreadPool.execute(this);
	}

	@Override
	public void downloadFile(Request req) {
		if (req.getAttribute(SAVE_FILE_NAME) == null) {
			try {
				throw new NoFilePathException(req.getUrl()
						+ " No SAVE_FILE_NAME Attribute");
			} catch (Exception e) {
				e.printStackTrace();
				log.error(" No SAVE_FILE_NAME Attribute",e);
			}
		} else {
			requestQueue.add(req);
		}
	}

	@Override
	public void setDelayTime(int time) {
		this.delayTime = time;
	}

	@Override
	public void saveRequestTask() {
	}

	@Override
	public void run() {
		while (true) {
			if (requestQueue.isEmpty()||System.currentTimeMillis() - lastDownloadTime < delayTime) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			Request req = requestQueue.poll();
			if (req != null) {
				downloadThreadPool.execute(new DowloadTask(req));
				lastDownloadTime = System.currentTimeMillis();
			}
		}

	}

	private final class DowloadTask implements Runnable {
		
		private final Request request;
		public DowloadTask(Request req){
			request = req;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet method = null;
			InputStream in =null;
			FileOutputStream out=null;
			try {
				String url = request.getUrl();
				method = new HttpGet(url);
				HttpResponse response = client.execute(method);
				Status status = Status.fromHttpCode(response.getStatusLine().getStatusCode());
				switch(status){
				case OK: 
					String pathname = (String) request.getAttribute(SAVE_FILE_NAME);
					in = response.getEntity().getContent();
					out = new FileOutputStream(new File(pathname));
					byte[] temp = new byte[1024*8];
					int len =0;
					while((len = in.read(temp))!=-1){
						out.write(temp, 0, len);
					}
					break;
				default:
					new RuntimeException("downloader file error status="+status);
				}
			}catch(Exception e){
				e.printStackTrace();
				log.error(e.getMessage()+":"+request.getUrl());
			}finally {
				if(method != null){
					method.abort();
				}
				if(in != null){
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(out!=null){
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
