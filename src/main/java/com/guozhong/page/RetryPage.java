package com.guozhong.page;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.guozhong.Request;
import com.guozhong.Status;
import com.guozhong.downloader.driverpool.DriverPool;
import com.guozhong.downloader.impl.SimpleHttpClient;

public class RetryPage extends Page {

    private final Request request;
    
    private final DriverPool driverPool ;
    
    private  int driverIndex ;

    public RetryPage(final Request request,final  DriverPool driverPool,int driverIndex ) {
        this.request = request;
        this.driverPool = driverPool;
        this.driverIndex = driverIndex;
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

	public int getRetryCount() {
		return request.getHistoryCount();
	}

	
	@Override
	public Object getRequestAttribute(String attribute) {
		return request.getAttribute(attribute);
	}



}
