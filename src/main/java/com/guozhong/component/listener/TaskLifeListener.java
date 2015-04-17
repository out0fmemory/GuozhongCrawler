package com.guozhong.component.listener;

import com.guozhong.CrawlTask;

/**
 * 如果要监听爬虫任务的生命周期就可以实现这个类
 * @author Administrator
 *
 */
public interface TaskLifeListener {
	
	/**
	 * 当任务开始抓取的时候回调
	 * @param task
	 */
	public void onStart(CrawlTask task);
	
	/**
	 * 任务结束的时候回调
	 * @param task
	 */
	public void onFinished(CrawlTask task);
	
	
	/**
	 * 任务恢复续抓时回调
	 * @param task
	 */
	public void onRecover(CrawlTask task);

}
