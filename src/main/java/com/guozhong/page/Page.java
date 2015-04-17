package com.guozhong.page;

import org.openqa.selenium.WebElement;

import com.guozhong.Request;
import com.guozhong.Status;
import com.guozhong.downloader.driverpool.DriverPool;


/**
 * @author jonasabreu
 * 
 */
public abstract class Page {

	
	 protected DriverPool driverPool ;
	 protected int driverIndex ;
	 protected Object driver;
	
    public abstract String getContent();

    public abstract Status getStatusCode();

    public abstract Request getRequest();
    
    public abstract Object getRequestAttribute(String attribute);
    
    


    /**
     * 是否需要将driver放回队列
     */
	public  boolean isNeedPost(){
		return driver!=null;
	};

	protected int getDriverIndex() {
		return driverIndex;
	}
	
	public Object getRequestDriver() {
		driver = driverPool.getDriver(driverIndex);
		return driver;
	}

	/**
     * 将driver放回队列
     */
	public void handleComplete() {
		if(isNeedPost()){
			if(!handleComplete){
				driverPool.handleComplete(driver);
				handleComplete = true;
			}
		}
	}
	
	private boolean handleComplete = false;
}
