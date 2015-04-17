package com.guozhong.downloader.impl;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.guozhong.CrawlTask;
import com.guozhong.Request;
import com.guozhong.Status;
import com.guozhong.component.listener.ChromeDriverLifeListener;
import com.guozhong.downloader.PageDownloader;
import com.guozhong.downloader.driverpool.ChromeWebDriverPool;
import com.guozhong.page.DefaultPageFactory;
import com.guozhong.page.Page;
import com.guozhong.page.PageFactory;
import com.guozhong.proxy.ProxyIp;
import com.guozhong.proxy.ProxyIpPool;

/**
 * 谷歌下载器，如果需要登录可以用这个
 * @author Administrator
 *
 */
public final class ChromeDownloader extends PageDownloader {
	
	private volatile ChromeWebDriverPool webDriverPool;
	
	
	private final PageFactory pageFactory;

	public ChromeDownloader() {
    	pageFactory = new DefaultPageFactory();
    }
	
	@Override
	public void close() throws IOException {
		checkInit();
		webDriverPool.closeAll();
	}

	@Override
	public Page download(Request request, CrawlTask task) {
		System.out.println("请求谷歌下载:"+request.getUrl());
		checkInit();
		com.guozhong.downloader.impl.ChromeDriver webDriver = null;
		Page page = null ;
		try{
			webDriver = webDriverPool.get();
			//System.out.println("ChromeDriver 请求"+request.getUrl());
			webDriver.get(request.getUrl());
			//Thread.sleep(1000);//考虑浏览器渲染需要时间每个页面停留1秒
			Status status = Status.fromHttpCode(200);//暂时定为200,将来扩展ChromeDriver取得
			String pageSource =webDriver.getPageSource();
			if(status.getBegin() >= 400 || status.equals(Status.UNSPECIFIED_ERROR)){
				page = pageFactory.buildErrorPage(request, status,webDriverPool, webDriver.getIndex());
			}else{
				WebElement root = webDriver.findElement(By.xpath("//html"));
				page = pageFactory.buildOkPage(request, status, pageSource ,root,webDriverPool, webDriver.getIndex());
			}
		}catch(Exception e){
			System.out.println("ChromeDriver错误页面URL:"+request.getUrl());
			page = pageFactory.buildRetryPage(request,webDriverPool, webDriver.getIndex());
			e.printStackTrace();
		}finally{
			if(webDriver!=null){
				webDriverPool.returnToPool(webDriver);
			}
		}
		return page;
	}
	

	 private void checkInit() {
	        if (webDriverPool == null) {
	            synchronized (this){
	                webDriverPool = new ChromeWebDriverPool();
	            }
	        }
	 }
	 
	public void setMaxDriverCount(int drivercount) {
		checkInit();
		webDriverPool.setMaxDriverCount(drivercount);
	}
	
	public void setMinDriverCount(int drivercount) {
		checkInit();
		webDriverPool.setMinDriverCount(drivercount);
	}
	

	@Override
	public void setTimeout(int second) {
		checkInit();
		this.webDriverPool.setPageLoadTimeout(second);
	}

	public void setChromeDriverLifeListener(
			ChromeDriverLifeListener chromeDriverLifeListener) {
		checkInit();
		webDriverPool.setChromeDriverLifeListener(chromeDriverLifeListener);
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		checkInit();
		webDriverPool.open();
	}
	
	@Override
	public void setProxyIpPool(ProxyIpPool proxyIpPool) {
		throw new RuntimeException("谷歌浏览器暂时不支持设置动态代理IP,你可以在打开浏览器后设置");
	}

	@Override
	public void setMaxProxyRequestCount(int count) {
		throw new RuntimeException("谷歌浏览器暂时不支持设置动态代理IP,你可以在打开浏览器后设置");
	}

	
}
