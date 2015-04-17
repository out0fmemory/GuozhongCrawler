package com.guozhong.component;

import java.util.List;
import java.util.regex.Pattern;


import com.guozhong.StartContext;
import com.guozhong.model.Proccessable;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;

/**
 * 网页解析接口
 * @author Administrator
 *
 */
public interface PageProcessor {
	/**
	 * 标记这个PageProcessor，应该处理哪种Request请求的页面   初始URL可以返回null
	 * @return
	 */
	public String getTag();
	
	/**
	 * 如果需要页面动态交互JS，定义一个PageScript返回
	 * @return
	 */
	public PageScript getJavaScript();
	
	/**
	 * 当启动代理Ip访问时需要重写此方法，返回正常网页应该带有的字符串标识。比如www.baidu.com带有“百度”
	 * @return
	 */
	public Pattern getNormalContain();
	
	/**
	 * 处理一个页面
	 * @param page
	 * @param context
	 * @return
	 */
	public void process(OkPage page,StartContext context,List<Proccessable> result)throws Exception; 
	
	/**
	 * 处理错误页面
	 * @param page
	 * @param context
	 */
	public void proccessErrorPage(Page page,StartContext context)throws Exception;
}
