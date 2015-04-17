package com.guozhong.page;

import org.openqa.selenium.WebElement;

import com.guozhong.Request;
import com.guozhong.Status;
import com.guozhong.downloader.driverpool.DriverPool;


/**
 * Contract for {@link com.funhigh.page.WePage.crawler.Page}s factory.
 */
public interface PageFactory {
	
	Page buildOkPage(Request request,Status status, String content, WebElement root, DriverPool driverPool,int driverIndex );

	Page buildErrorPage(Request request, Status error, DriverPool drivePool,int driverIndex );

	Page buildRejectedMimeTypePage(Request request, Status status,DriverPool driverPool,int driverIndex );
	
	Page buildRetryPage(Request request,DriverPool drivePool,int driverIndex);
}
