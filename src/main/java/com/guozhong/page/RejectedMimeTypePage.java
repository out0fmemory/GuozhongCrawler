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
final public class RejectedMimeTypePage extends Page {

    private final Request request;
    private final Status status;
    private final DriverPool driverPool ;
    private  int driverIndex ;
    public RejectedMimeTypePage(final Request request, final Status status,final DriverPool driverPool,int driverIndex ) {
        this.request = request;
        this.status = status;
        this.driverPool = driverPool;
        this.driverIndex = driverIndex;
    }


    public String getContent() {
        return "";
    }


    public Status getStatusCode() {
        return status;
    }

	@Override
	public Request getRequest() {
		return this.request;
	}
	
	@Override
	public Object getRequestAttribute(String attribute) {
		return request.getAttribute(attribute);
	}




	@Override
	public int getDriverIndex() {
		return driverIndex;
	}

}
