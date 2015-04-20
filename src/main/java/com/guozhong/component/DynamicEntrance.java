package com.guozhong.component;

import java.util.List;
import java.util.Map;

import com.guozhong.StartContext;

/**
 * 动态生成入口URL。
 * 当你的任务的入口URL每次都不是固定的话，那么可以设置一个DynamicEntrance
 * @author Administrator
 *
 */
public interface DynamicEntrance {
	
	/**
	 * 是否清除之前的入口URL
	 * @return
	 */
	public boolean isClearLast();
	
	/**
	 * 开始加载之前回调onStartLoad
	 */
	public void onStartLoad();
	
	/**
	 * 简单加载所有入口URL。每个url是一个StartContext。并且页面编码等所有参数使用默认值。
	 * @return
	 */
	public List<String> load();
	
	
	/**
	 * 自定义所有StartContext。便于自定义参数和跟进url(SubRequest)、编码等
	 * @return
	 */
	public List<StartContext> loadStartContext();
	
	/**
	 * 加载完成之后回调
	 */
	public void onLoadComplete();
	
	
}
