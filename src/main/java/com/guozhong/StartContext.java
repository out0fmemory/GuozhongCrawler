package com.guozhong;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.bcel.generic.NEW;
import org.apache.commons.collections.SynchronizedPriorityQueue;
import org.apache.commons.collections.list.SynchronizedList;
import org.apache.xalan.xsltc.runtime.Hashtable;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.gargoylesoftware.htmlunit.javascript.host.arrays.ArrayBufferViewBase;
import com.guozhong.Request.Method;
import com.guozhong.component.Pipeline;
import com.guozhong.model.Proccessable;
/**
 * 解析时候的上下文信息和操作句柄
 * @author Administrator
 *
 */
public final class StartContext implements Serializable{
	
	/**
	 * 全局属性不会被清除
	 */
	private final HashMap<String, Object> globalAttribute = new HashMap<String, Object>();
	
	/**
	 * 临时属性   每个StartContext完成后会被清除
	 */
	private final HashMap<String, Object> tempAttribute = new HashMap<String, Object>();
	
	/**
	 * 定义根据url
	 */
	private List<Request> subrequest = new ArrayList<Request>();
	
	private Request startRequest;
	private Pipeline pipeline ;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StartContext(String url,String tag,int priority,String charSet) {
		startRequest = createRequest(url, tag, priority,charSet);
		startRequest.setSeed(true);
	}
	 
	public StartContext(String url,String tag,int priority) {
		startRequest = createRequest(url, tag, priority);
		startRequest.setSeed(true);
	}
	
	public StartContext(Request startRequest) {
		this.startRequest = startRequest;
	}
	
	public StartContext(){}
	
	
	
	
	 /**
     * 
     * @param url
     * @param tag 初始URL可以为null
     * @return
     */
    public  Request createRequest(String url,String tag){
    	Request req = new Request();
    	req.setUrl(url);
    	req.setMethod(Method.GET);
    	req.setTag(tag);
    	return req;
    }
    
    /**
     * 
     * @param url
     * @param tag
     * @param priority
     * @param charSet  页面的编码格式 默认gbk
     * @return
     */
    public  Request createRequest(String url,String tag,int priority,String charSet){
    	if(priority >=0 && priority<=1000){
    		Request req = new Request();
        	req.setUrl(url);
        	req.setMethod(Method.GET);
        	req.setPriority(priority) ;
        	req.setTag(tag);
        	req.setPageCharset(charSet);
        	return req;
    	}else{
    		throw new IllegalArgumentException("priority的值必须在0-1000之间");
    	}
    }
    
    /**
     * 
     * @param url
     * @param tag
     * @param priority
     * @return
     */
    public  Request createRequest(String url,String tag,int priority){
    	if(priority >=0 && priority<=1000){
    		Request req = new Request();
    		req.setUrl(url);
    		req.setMethod(Method.GET);
    		req.setPriority(priority) ;
    		req.setTag(tag);
    		return req;
    	}else{
    		throw new IllegalArgumentException("priority的值必须在0-1000之间");
    	}
    }
    
    /**
     * 创建一个二进制下载请求
     * @param url
     * @param savePath  文件保存的路径
     * @return
     */
    public Request createBinary(String url,String savePath){
    	Request req = new Request();
    	req.setUrl(url);
    	req.setBinary(savePath);
    	return req;
    }

	public Request getStartRequest() {
		return startRequest;
	}

	public void setStartRequest(Request startRequest) {
		this.startRequest = startRequest;
	}

	
	
	public void addSubRequest(Request request){
		if(startRequest != null){
			this.subrequest.add(request);
		}else{
			startRequest = request;
			startRequest.setSeed(true);
		}
	}
	
	public List<Request> getSubRequest(){
		return this.subrequest;
	}

	/**
	 * 取得信息
	 * @param key
	 * @return
	 */
	public  Object getGlobalAttribute(String attribute){
		Object value;
		synchronized (globalAttribute) {
			value = globalAttribute.get(attribute);
		}
		return value;
	}
	
	
	/**
	 * @param attribute
	 * @param value
	 * @return
	 */
	public Object putGlobalAttribute(String attribute, Object value) {
		synchronized (globalAttribute) {
			globalAttribute.put(attribute, value);
		}
		return value;
	}
	
	public  Object getTempAttribute(String attribute){
		Object value;
		synchronized (tempAttribute) {
			value = tempAttribute.get(attribute);
		}
		return value;
	}
	
	public Object putTempAttribute(String attribute, Object value) {
		synchronized (tempAttribute) {
			tempAttribute.put(attribute, value);
		}
		return value;
	}

	/**
	 * 清除临时全局信息
	 */
	public void clearTempAttribute(){
		synchronized (tempAttribute) {
			tempAttribute.clear();
		}
	}

	public Pipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
	}
	

	/**
	 * 直接发送到离线存储类
	 * @param proccessable
	 */
	public final void sendToPipeline(Proccessable proccessable){
		sendToPipeline(Arrays.asList(proccessable));
	}
	
	public final void sendToPipeline(List<Proccessable> pro){
		if(pipeline != null){
			pipeline.proccessData(pro);
		}
	}
	
	public boolean isEmpty(){
		if(startRequest == null || subrequest.isEmpty()){
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "StartContext [globalAttribute=" + globalAttribute
				+ ", tempAttribute=" + tempAttribute + ", subrequest="
				+ subrequest + ", startRequest=" + startRequest + ", pipeline="
				+ pipeline + "]";
	}


}
