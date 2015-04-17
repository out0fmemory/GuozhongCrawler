package com.guozhong.downloader.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;

public final class ExtendWebDriver extends HtmlUnitDriver {
	
	private int index ;
	
	

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	private Set<String> headerNames ;


	public int getResponseCode() {
		Page page = lastPage();
		if (page == null) {
		      return -1;
		 }
		WebResponse response = page.getWebResponse();
		return response.getStatusCode();
    }
	
	public ExtendWebDriver() {
		super();
		headerNames = new HashSet<String>();
	}

	public ExtendWebDriver(boolean enableJavascript) {
		this(BrowserVersion.CHROME);
		headerNames = new HashSet<String>();
		if(enableJavascript){
			setJavascriptEnabled(enableJavascript);
			WebClient webClient = getWebClient();
			webClient.getOptions().setCssEnabled(false);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			//webClient.getOptions().setTimeout(50000);
			webClient.getOptions().setThrowExceptionOnScriptError(false);
		}
	}

	public ExtendWebDriver(BrowserVersion version) {
		super(version);
		// TODO Auto-generated constructor stub
	}

	public ExtendWebDriver(Capabilities capabilities) {
		super(capabilities);
		// TODO Auto-generated constructor stub
	}

	public void addRequestHeader(String name,String value){
		WebClient webClient = getWebClient();
		webClient.addRequestHeader(name, value);
		headerNames.add(name);
	}

	public void clearHeaders(){
		WebClient webClient = getWebClient();
		for (String name:headerNames) {
			webClient.removeRequestHeader(name);
		}
		headerNames.clear();
	}
	
	public WebClient getClient(){
		return  getWebClient();
	}
}
