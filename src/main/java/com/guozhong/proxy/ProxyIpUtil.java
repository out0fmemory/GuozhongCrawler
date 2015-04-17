package com.guozhong.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.WebDriverException;

import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.guozhong.downloader.impl.ExtendWebDriver;
import com.guozhong.downloader.impl.HttpClientFactory;
import com.guozhong.util.FileRW;

public final class ProxyIpUtil {
	
	public static final void TestProxyIp(List<ProxyIp> xx,String outFile){
		StringBuffer buf = new StringBuffer();
		ExtendWebDriver wd  = new ExtendWebDriver();
		wd.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		WebClient wc = wd.getClient();
		WebClientOptions o = wc.getOptions();
		for (ProxyIp ip : xx) {
			ProxyConfig proxyConfig = new ProxyConfig(ip.getIp(),ip.getPort());
			o.setProxyConfig(proxyConfig);
			wd.get("http://www.baidu.com/");
			if(wd.getPageSource().contains("关于百度")){
				buf.append(ip).append("\n");
			}
			System.out.println("代理ip（"+ip.getIp()+"："+ip.getPort()+"）请求状态码："+wd.getResponseCode());
		}
		wd.quit();
		FileRW.writeFile(buf.toString(), outFile);
	}
	
	public static final List<ProxyIp> filterProxyIp(List<ProxyIp> xx){
		List<ProxyIp> array = new ArrayList<ProxyIp>();
		ExtendWebDriver wd  = new ExtendWebDriver();
		wd.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		WebClient wc = wd.getClient();
		WebClientOptions o = wc.getOptions();
		for (ProxyIp ip : xx) {
			ProxyConfig proxyConfig = new ProxyConfig(ip.getIp(),ip.getPort());
			o.setProxyConfig(proxyConfig);
			wd.get("http://www.baidu.com/");
			System.out.println("代理ip（"+ip.getIp()+"："+ip.getPort()+"）请求状态码："+wd.getResponseCode());
			if(wd.getPageSource().contains("关于百度")){
				array.add(ip);
			}else{
				System.out.println("剔除IP："+ip);
			}
		}
		wd.quit();
		return array;
	}
	
	public static void httpClientTestProxyIp(String ip,int port) throws Exception{
		System.out.println("-----------------------------");
		HttpClientFactory factory = HttpClientFactory.getInstance(); 
		BasicCookieStore cookieStore = new BasicCookieStore();
		HttpHost proxy = new HttpHost(ip, 3128,"HTTP");
		HttpClient client = factory.buildDefaultHttpClient(new BasicCookieStore());
       // client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy); 
        HttpGet get = new HttpGet("http://www.kanzhun.com/gs.html?ka=head-com");
        get.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		get.setHeader("Accept-Encoding", "gzip, deflate");
		get.setHeader("Connection", "keep-alive");
		get.setHeader("Accept-Language", "zh-cn");
		get.setHeader("Host", "www.kanzhun.com");
		get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0");
        HttpResponse response = client.execute(get);
        String content = EntityUtils.toString(response.getEntity(),"utf-8");
        System.out.println(content);
    	if(!content.contains("看准")){
    		System.out.println("代理无效");
		}else{
			System.out.println(true);
		}
        System.out.println("代理ip（"+ip+"="+port+"）请求状态码："+response.getStatusLine().getStatusCode());
	}
	
	private static ExtendWebDriver wd  = new ExtendWebDriver();
	public static final boolean TestProxyIp(String ip,int port){
		boolean testProxyIp = false;
		//wd.clearHeaders();
		wd.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		WebClient wc = wd.getClient();
//		wd.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//		wd.addRequestHeader("Accept-Encoding", "gzip, deflate");
//		wd.addRequestHeader("Connection", "keep-alive");
//		wd.addRequestHeader("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
//		wd.addRequestHeader("Host", "www.kanzhun.com");
//		wd.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0");
		WebClientOptions o = wc.getOptions();
		ProxyConfig proxyConfig = new ProxyConfig(ip,port);
		o.setProxyConfig(proxyConfig);
		try{
			wd.get("http://www.kanzhun.com/gs.html?ka=head-com");
			System.out.println(wd.getPageSource());
			if(wd.getPageSource().contains("allindustry clearfix")){
				testProxyIp = true;
			}
			System.out.println("代理ip（"+ip+"="+port+"）请求状态码："+wd.getResponseCode());
		}catch(Exception e){
			if(e instanceof  org.apache.http.conn.ConnectTimeoutException){
				System.out.println("是");	
			}else{
				System.out.println("不是");
			}
			e.printStackTrace();
			System.out.println(e.getMessage());
			String eMessage = e.getMessage();
			if(eMessage.contains(ip)||eMessage.contains("timed out")){
				System.out.println("代理无效");
				testProxyIp = false;
			}
		}
		//wd.quit();
		return testProxyIp;
	}
	
	private static final String getUTF8(HttpResponse response) throws Exception{
		StringBuffer buf = new StringBuffer();
		BufferedReader br = null;
		try{
			br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"utf-8"));
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
	


	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(TestProxyIp("125.39.68.130",80));
		//System.out.println(TestProxyIp("122.225.106.36",80));
		//Set<Cookie> s = wd.getClient().getCookies(new URL("http://www.kanzhun.com/gs.html?ka=head-com"));
		//System.out.println(s);
		//System.out.println(TestProxyIp("183.62.255.58", 9797));
		//System.out.println(TestProxyIp("183.220.245.251", 8123));
		//httpClientTestProxyIp("123.125.19.44", 80);
	}
}
