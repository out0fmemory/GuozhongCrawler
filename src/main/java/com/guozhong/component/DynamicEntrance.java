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
	 * 加载入口URL
	 * @return
	 */
	public List<String> load();
	
	/**
	 * 加载入口URL  带附加参数的方法
	 * @return
	 */
	public Map<String,Map<String,Object>> load2();
	
	public List<StartContext> getStartContext();
	
	/**
	 * 加载完成之后回调
	 */
	public void onLoadComplete();
	
	
	/**
	 * 入口URL页面的编码格式不设置默认gbk
	 * @return
	 */
	public String getEntranceCharSet();
}
