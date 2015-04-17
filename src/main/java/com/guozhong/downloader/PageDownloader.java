package com.guozhong.downloader;

import java.io.Closeable;

import com.guozhong.CrawlTask;
import com.guozhong.Request;
import com.guozhong.page.Page;
import com.guozhong.proxy.ProxyIpPool;




public abstract class PageDownloader implements Closeable{
	
	public final static int DEFAULT_MAX_PROXY_REQUEST_COUNT = Integer.MAX_VALUE;

    /**
     * Downloads web pages and store in Page object.
     *
     * @param request
     * @param task
     * @return page
     */
    public abstract Page download(Request request,CrawlTask task);
    
    public abstract void open();

    
    public abstract void setTimeout(int second);
    
    /**
     * 设置代理IP池
     * @param proxyIpPool
     */
    public abstract void setProxyIpPool(ProxyIpPool proxyIpPool);
    
    /**
     * 设置最大代理IP请求次数  根据代理IP有效率  自行设定
     * @param count  0不限制
     */
    public void setMaxProxyRequestCount(int count) {
    	if(count == 0){
    		this.maxProxyRequestCount = Integer.MAX_VALUE;
    	}else{
    		this.maxProxyRequestCount = count;	
    	}
	}
    
    protected int maxProxyRequestCount = DEFAULT_MAX_PROXY_REQUEST_COUNT;
}
