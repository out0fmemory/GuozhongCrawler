package com.guozhong.component.listener;

import org.openqa.selenium.chrome.ChromeDriver;

/**
 * 监听谷歌浏览器的创建和销毁
 * @author Administrator
 *
 */
public interface ChromeDriverLifeListener {

	/**
	 * 监听创建谷歌浏览器
	 * 每创建好一个则回调如下方法
	 * 比如:在这里你可以先登录
	 * @param chromeDriver
	 */
	public void onCreated(int index,com.guozhong.downloader.impl.ChromeDriver chromeDriver);
	
	
	/**
	 * 监听谷歌浏览器销毁
	 * 在销毁之前做你的操作比如退出登录
	 * @param chromeDriver
	 */
	public void onQuit(int index,com.guozhong.downloader.impl.ChromeDriver chromeDriver);
}
