package com.guozhong.component.listener;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import com.guozhong.downloader.impl.SimpleHttpClient;
import com.guozhong.exception.DriverCreateException;

public interface HttpClientLifeListener {

	/**
	 * 监听创建httpClient实例
	 * 每创建好一个则回调如下方法
	 * 比如:在这里你可以先登录
	 */
	public void onCreated(int index,SimpleHttpClient httpClient);
	
	
	/**
	 * 监听谷歌httpClient实例
	 * 在销毁之前做你的操作比如退出登录
	 */
	public void onQuit(int index,SimpleHttpClient httpClient);
}
