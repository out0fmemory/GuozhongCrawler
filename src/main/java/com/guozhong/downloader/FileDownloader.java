package com.guozhong.downloader;

import com.guozhong.Request;

public  interface FileDownloader {
	
	/**
	 * 下载文件
	 * @param req
	 * @param saveFile
	 */
	public void downloadFile(Request req );
	
	/**
	 * 下载延迟时间
	 * @param time
	 */
	public void setDelayTime(int time);
	
	/**
	 * 保存请求队列
	 */
	public void saveRequestTask();

}
