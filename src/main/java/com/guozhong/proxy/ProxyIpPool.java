package com.guozhong.proxy;

import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.guozhong.CrawlTask;
import com.guozhong.exception.ProxyIpPoolException;


/**
 * @author Administrator
 *
 */
public abstract class ProxyIpPool {
	private static Logger logger = Logger.getLogger(CrawlTask.class);
	/**
	 */
	public static final int DEFAULT_CACHE_COUNT = 10;
	
	public static final int DEFAULT_USE_VALID_COUNT = 5;
	
	private final LinkedBlockingQueue<ProxyIp> cache = new LinkedBlockingQueue<ProxyIp>();
	
	private final Stack<ProxyIp> netWorkQueue = new Stack<ProxyIp>();
	
	/**
	 * 是否开启缓存
	 */
	private boolean enableCache = false;
	
	private int initSize = 5*5;
	
	/**
	 * 每个IP默认最大有效请求次数。当一个IP有效请求次数达到该值后，为确保ip不会因为请求次数过多而被封会从缓存中移除
	 * 
	 */
	private int max_use_count  = DEFAULT_USE_VALID_COUNT;
	
	/**
	 * Ip过期时间
	 */
	private long pastTime = 1000 * 30;
	
	
	/**
	 * 建议是线程数的5倍
	 * @param initSize
	 */
	public ProxyIpPool(int initSize,long pastTime,int max_use_count){
		this.initSize = initSize;
		this.pastTime = pastTime;
		this.max_use_count = max_use_count;
	}
	
	public int getMaxUseCount() {
		return max_use_count;
	}

	public void setMaxValidCount(int max_valid_count) {
		this.max_use_count = max_valid_count;
	}

	public ProxyIp pollProxyIp(){
		//拿缓存
		ProxyIp ip = null;
		if(enableCache){
			int size = cache.size();
			if(size >= DEFAULT_CACHE_COUNT){
				ip = cache.poll();
				ip.setOwner(this);
				return ip;
			}
		}
		//拿网络
		while(true){
			try{
				ip = netWorkQueue.pop();
			}catch(EmptyStackException e){}
			if(ip!= null){
				if(isPast(ip)){
					logger.info("清除过期的IP"+netWorkQueue.size()+"个\n");
					netWorkQueue.clear();
				}else{
					ip.setOwner(this);
					return ip;
				}
			}
			synchronized (netWorkQueue) {
				if(netWorkQueue.isEmpty()){
					logger.info("加载新的IP"+initSize+"个\n");
					List<ProxyIp> ips = null;
					try {
						ips = initProxyIp(initSize);
					} catch (Exception e) {
						e.printStackTrace();
						logger.error(e);
					}
					if(ips == null || ips.size() != initSize){
						logger.warn("", new ProxyIpPoolException("加载代理ip小于要求"+initSize));
					}
					for (ProxyIp item : ips) {
						netWorkQueue.push(item); 
					}
				}
			}
		}
	}
	
	
	/**
	 * @return
	 */
	protected abstract List<ProxyIp> initProxyIp(int size)throws Exception;
	
	
	
	public void cache(ProxyIp ip){
		if(enableCache){
			cache.add(ip);
		}
	}
	
	public int getCacheSize(){
		return cache.size();
	}

	public boolean isEnableCache() {
		return enableCache;
	}

	public ProxyIpPool setEnableCache(boolean enableCache) {
		if(!enableCache){
			cache.clear();
		}
		this.enableCache = enableCache;
		return this;
	}
	
	/**
	 * 验证是否过期
	 * @return
	 */
	private final boolean isPast(ProxyIp ip){
		return System.currentTimeMillis() - ip.getFetchTime() > pastTime;
	}
}
