/**
 * 
 */
package com.guozhong.page;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;

import com.guozhong.Request;
import com.guozhong.Status;
import com.guozhong.downloader.driverpool.DriverPool;
import com.guozhong.downloader.impl.SimpleHttpClient;


/**
 * @author jonasabreu
 * 
 */
public class OkPage extends com.guozhong.page.Page {

	    private final Request request;
	    private final Status status;
	    private final String content ; 
	    private final WebElement root;
	   
	   

	    public OkPage(final Request request, final Status status, String content, WebElement root,final DriverPool driverPool,int driverIndex ) {
	        this.request = request;
	        this.status = status;
	        this.content = content;
	        this.root = root;
	        this.driverPool = driverPool;
	        this.driverIndex = driverIndex;
	    }


	    public String getContent() {
	        return this.content;
	    }


	    public Status getStatusCode() {
	        return status;
	    }
	    
		@Override
		public Request getRequest() {
			return this.request;
		}
		
		public WebElement getRoot(){
			return this.root;
		}
		
		@Override
		public String toString() {
			return "OkPage [status=" + status.name() +",url:"+request.getUrl()+ ", content=" + content + "]";
		}
		
		@Override
		public Object getRequestAttribute(String attribute) {
			return request.getAttribute(attribute);
		}
		
		
		
}
