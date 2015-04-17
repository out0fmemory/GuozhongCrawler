package com.guozhong.page;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.guozhong.Request;
import com.guozhong.Status;
import com.guozhong.downloader.driverpool.DriverPool;
import com.guozhong.downloader.impl.SimpleHttpClient;

public class RetryPage extends Page {

    private final Request request;
    
    private final int retryCount ;
    
    private static final Map<String, Integer> retryCounts = new ConcurrentHashMap<String,Integer>();
    
    private final DriverPool driverPool ;
    private  int driverIndex ;

    public RetryPage(final Request request,final  DriverPool driverPool,int driverIndex ) {
        this.request = request;
        this.driverPool = driverPool;
        this.driverIndex = driverIndex;
        Integer oldCount = retryCounts.get(request.getUrl());
        if(oldCount == null){
        	oldCount = 0;
        }
        this.retryCount=oldCount;
        retryCounts.put(request.getUrl(), retryCount);
    }



	@Override
	public Request getRequest() {
		return this.request;
	}



	@Override
	public String getContent() {
		return null;
	}



	@Override
	public Status getStatusCode() {
		return null;
	}


	public void record(){
		retryCounts.put(request.getUrl(), retryCount+1);
	}

	public int getRetryCount() {
		return retryCounts.get(request.getUrl());
	}

	public static void clearCount(String url){
		retryCounts.remove(url);
	}
	
	@Override
	public Object getRequestAttribute(String attribute) {
		return request.getAttribute(attribute);
	}



}
