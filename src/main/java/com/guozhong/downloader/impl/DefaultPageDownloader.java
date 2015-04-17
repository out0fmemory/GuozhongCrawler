package com.guozhong.downloader.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;



import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.guozhong.CrawlTask;
import com.guozhong.Request;
import com.guozhong.StartContext;
import com.guozhong.Status;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.listener.HttpClientLifeListener;
import com.guozhong.downloader.PageDownloader;
import com.guozhong.downloader.driverpool.HttpClientPool;
import com.guozhong.exception.ProxyIpLoseException;
import com.guozhong.exception.ReadByteException;
import com.guozhong.page.DefaultPageFactory;
import com.guozhong.page.Page;
import com.guozhong.page.PageFactory;
import com.guozhong.proxy.ProxyIp;
import com.guozhong.proxy.ProxyIpPool;
import com.guozhong.util.ExceptionUtil;
import com.guozhong.util.StringUtil;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;


public class DefaultPageDownloader extends PageDownloader{

	private final Logger log = Logger.getLogger(DefaultPageDownloader.class);
	private int timeout = 15;
	
	private final PageFactory pageFactory;
	
	private ProxyIpPool proxyIpPool;
	
	private volatile HttpClientPool httpClientPool;
	
	public DefaultPageDownloader(){
		pageFactory = new DefaultPageFactory();
	}
	
	@Override
	public void close() throws IOException {
		httpClientPool.closeAll();
	}

	@Override
	public Page download(Request request,CrawlTask task) {
		Page page = null;
		if(proxyIpPool == null){
			try {
				page = go(request, task, null);
			} catch (ProxyIpLoseException e) {}
			return page;
		}
		int proxyIpRequestCount = 0 ;
		ProxyIp ip = null;
		while(true){
			if(proxyIpRequestCount >= maxProxyRequestCount&&!request.isStartURL()){
				log.error(request.getUrl()+"下载次数超过"+maxProxyRequestCount+"被丢弃"); 
				break;
			}
			ip = proxyIpPool.pollProxyIp();//不断去拿最新的代理IP去下载
			try {
				page = go(request, task, ip);
				if(ip.incrementRequestCount() >= proxyIpPool.getMaxValidCount()){
					System.out.println(ip+"使用达到"+proxyIpPool.getMaxValidCount()+"次");
				}else{
					ip.markCache();//缓存IP
					System.out.println(ip+"使用成功");
				}
				return page;
			} catch (ProxyIpLoseException e) {
				proxyIpRequestCount++;
				log.info(request.getUrl()+" "+ip+">下载失败");
				continue;
			} 
		}
		return page;
	}
	
	/**
	 * 去下载
	 * @param request
	 * @param task
	 * @return
	 * @throws ProxyIpLoseException
	 */
	public Page go(Request request,CrawlTask task,ProxyIp ip )throws ProxyIpLoseException{
		ProxyIpLoseException exception  = null;
		Page  page = null;
		SimpleHttpClient client =  null;
		HttpRequestBase method = null;
		try {
			client = httpClientPool.get();
			method = buildHttpUriRequest(request);
			setProxyIpAndTimeOut(method,ip,timeout);
			HttpResponse response = client.execute(method);
			Status status = Status.fromHttpCode(response.getStatusLine().getStatusCode());
			String content = EntityUtils.toString(response.getEntity(),request.getPageCharset());
			if(ip != null){
				//验证是否是正常网页
				PageProcessor pageProcessor = task.findPageProccess(request.getTag());
				Matcher matcher = pageProcessor.getNormalContain().matcher(content);
				boolean containalNormal = matcher.find();
				if(!containalNormal){ //网页内容是否正常含有特征字符串
					throw new ProxyIpLoseException(ip.toString());
				}
			}
			if(status.getBegin() >= 400 || status.equals(Status.UNSPECIFIED_ERROR)){
				page = pageFactory.buildErrorPage(request, status , httpClientPool , client.getIndex());
			}else{
				WebElement root = null;
				page= pageFactory.buildOkPage(request, status, content, root , httpClientPool , client.getIndex());
			}
		} catch (Exception e) {
			//e.printStackTrace();
			if(e instanceof NullPointerException){
				throw new RuntimeException(e); 
			}
			if(ip != null ){//判断异常是否是代理Ip所导致的
				if(ExceptionUtil.existProxyException(e)){
					exception = new ProxyIpLoseException(ip.toString());
				}else{
					page = pageFactory.buildRetryPage(request , httpClientPool , client.getIndex());
					e.printStackTrace();
				}
			}else{
				page = pageFactory.buildRetryPage(request , httpClientPool , client.getIndex());
			}
		}finally {
			if(method != null){
				method.abort();
				method.releaseConnection();
				httpClientPool.returnToPool(client);
			}
			if(exception != null){//抛出ip失效异常
				throw exception;
			}
		}
		return page;
	}
	
	private final String readContent(HttpResponse response,String charset) throws Exception{
		StringBuffer buf = new StringBuffer();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),charset));
			String temp  = null;
			while((temp=br.readLine())!=null){
				buf.append(temp).append("\n");
			}
		}catch(Exception e){
			//e.printStackTrace();
		}finally{
			if(br != null){
				br.close();
			}
		}
		return buf.toString();
	}
	
	
	private final void setProxyIpAndTimeOut(HttpRequestBase method,ProxyIp ip,int timeout){
		Builder builder = RequestConfig.custom().setSocketTimeout(timeout*1000).setConnectTimeout(timeout*1000);//设置请求和传输超时时间;
		if(ip != null){
			HttpHost proxy = new HttpHost(ip.getIp(),ip.getPort());    
			builder.setProxy(proxy);
		}
		method.setConfig(builder.build());
	}

	@Override
	public void setTimeout(int second) {
		this.timeout = second;
	}
	
	public void setMaxDriverCount(int drivercount) {
		checkInit();
		httpClientPool.setMaxDriverCount(drivercount);
	}
	
	public void setMinDriverCount(int drivercount){
		checkInit();
		httpClientPool.setMinDriverCount(drivercount);
	}
	
	@Override
	public void open() {
		checkInit();
		httpClientPool.open();
	}
	
	 private void checkInit() {
	        if (httpClientPool == null) {
	            synchronized (this){
	            	httpClientPool = new HttpClientPool();
	            }
	        }
	    }
	 
	@Override
	public void setProxyIpPool(ProxyIpPool proxyIpPool) {
		this.proxyIpPool = proxyIpPool;
	}
	
	public void setHttpClientLifeListener(HttpClientLifeListener httpClientLifeListener) {
		checkInit();
		this.httpClientPool.setHttpClientLifeListener(httpClientLifeListener);
	}
	
	/**
	 * 根据request构建get或者post请求
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private final HttpRequestBase buildHttpUriRequest(Request request) throws UnsupportedEncodingException{
		Map<String,String> custom_headers = request.getHedaers();
		Map<String,String> headers = getFirefoxHeaders();
		headers.putAll(custom_headers);//覆盖自定义请求头
		Set<Entry<String, String>> keyValues = headers.entrySet();
		switch(request.getMethod()){
			case GET:
				HttpGet get = new HttpGet(request.getUrl());
				//设置请求头
				for (Entry<String, String> entry : keyValues) {
					get.setHeader(entry.getKey(), entry.getValue());
				}
				
				return get;
			case POST:
				HttpPost post = new HttpPost();
				//设置请求头
				for (Entry<String, String> entry : keyValues) {
					post.setHeader(entry.getKey(), entry.getValue());
				}
				//设置请求参数
				Set<Entry<String, String>> params = request.getParams();
				if(!params.isEmpty()){
					List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
					for (Entry<String, String> entry : params) {
						BasicNameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
						nameValuePairs.add(pair);
					}
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs)); 
				}
				return post;
		}
		return null;
	}
	
	private static final Map<String,String> getFirefoxHeaders(){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0");
		return headers;
	}
	
	
}
