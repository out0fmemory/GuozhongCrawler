package com.guozhong;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.guozhong.downloader.impl.DefaultFileDownloader;
import com.guozhong.model.Proccessable;


public class Request implements Proccessable,Comparable<Request>{
	

	private String url;

    private Method method;
    
    private String pageCharset = "GBK";
    
    /**
     * 是否是种子
     */
    private boolean seed = false;
    /**
     * request属性
     */
    private HashMap<String,Object> attributes = null;
    
    /**
     * request的参数
     */
    private HashMap<String, String> requestParams = null;
    
    /**
     * 请求头
     */
    private HashMap<String,String> headers = null;
    
    /**
     * 标记一类请求
     */
    private String tag;
    
    private int priority = 0;  
    
    
    /**
     * 是否使用DefaultDownload请求
     */
    private boolean isDefaultDownload;
    
    /**
     * 是否是二进制下载
     */
    private boolean isBinary = false;
    
    /**
     * 记录Request被发送的次数
     */
    private int requestCount = 0;
    
    public Request(){
    }
    
    public Request(boolean seed){
    	this.seed = seed;
    }
    
    public void recodeRequest(){
    	requestCount++;
    }
    
    public int getHistoryCount(){
    	return requestCount;
    }
    
    public final Request setAttribute(String attribute,Object value){
    	if(attributes == null){
    		attributes = new HashMap<String,Object>();
    	}
    	attributes.put(attribute, value);
    	return this;
    }
    
    public final Object getAttribute(String attribute){
    	if(attributes == null){
    		return null;
    	}
    	return attributes.get(attribute);
    }
    
    
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * 是否是种子URL
	 * @param seed
	 */
	protected void setSeed(boolean seed) {
		this.seed = seed;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}


	public void putParams(String name,String value){
		iniParmaContainer();
    	if(name != null){
    		if(!requestParams.containsKey(name)&&(value!=null && !value.trim().equals(""))){
    			requestParams.put(name, value);
    		}else if(requestParams.containsKey(name)){
    			throw new IllegalArgumentException("参数非法 name '"+name+"' 已经存在");
    		}else{
    			throw new IllegalArgumentException("参数非法 name = "+name+" value = "+value);
    		}
    	}
    }

	private void iniParmaContainer() {
		if(requestParams == null){
			requestParams = new HashMap<String, String>();
		}
	}
    
    public Set<Entry<String, String>> getParams(){
    	iniParmaContainer();
    	return this.requestParams.entrySet();
    }
    
    public Object getParamsByName(String name){
    	iniParmaContainer();
    	return this.requestParams.get(name);
    }
    
    private void iniHeadersContainer() {
		if(headers == null){
			headers = new HashMap<String, String>();
		}
	}
    
    public void putHeader(String name,String value){
    	iniHeadersContainer();
    	headers.put(name, value);
    }
    
    public Map<String, String> getHedaers(){
    	iniHeadersContainer();
    	return this.headers;
    }
    
    
    public int getPriority() {
		return 1000 - priority;
	}
    
	public void setPriority(int priority) {
		this.priority =1000 - priority;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	

	public boolean isDefaultDownload() {
		return isDefaultDownload;
	}

	public void setDefaultDownload(boolean defaultDown) {
		this.isDefaultDownload = defaultDown;
	}
	
	public final boolean isStartURL(){
		return seed;
	}



	public boolean isBinary() {
		return isBinary;
	}

	public void setBinary(String savaPath) {
		this.isBinary = true;
		setAttribute(DefaultFileDownloader.SAVE_FILE_NAME, savaPath);
	}

	public String getPageCharset() {
		return pageCharset;
	}

	public void setPageCharset(String pageCharset) {
		this.pageCharset = pageCharset;
	}

	public enum Method{
    	GET,
    	POST;
    }



	/**
	 * request排序
	 * getPriority越小  优先级越高   但是对于上层调用无需关心  底层会做反转
	 */
	@Override
	public int compareTo(Request o) {
		if(this.getPriority() < o.getPriority()){
    		return 1;
    	}else if(this.getPriority() == o.getPriority()){
    		return 0;
    	}else{
    		return -1;
    	}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (method != other.method)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
