package com.guozhong.page;

import java.util.ArrayList;
import java.util.List;

import com.guozhong.Request;
import com.guozhong.Status;
import com.guozhong.downloader.driverpool.DriverPool;
import com.guozhong.downloader.impl.SimpleHttpClient;


/**
 * @author jonasabreu
 * 
 */
final public class ErrorPage extends Page {

	    private final Request request;
	    private final Status error;
	    public ErrorPage(final Request request, final Status error,final DriverPool driverPool,int driverIndex ) {
	        this.request = request;
	        this.error = error;
	        this.driverPool = driverPool;
	        this.driverIndex = driverIndex;
	    }


	    public String getContent() {
	        return "";
	    }


	    public Status getStatusCode() {
	        return this.error;
	    }

		@Override
		public Request getRequest() {
			return this.request;
		}


		@Override
		public Object getRequestAttribute(String attribute) {
			return request.getAttribute(attribute);
		}



}
