package com.guozhong.page;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

import com.guozhong.CrawlTask;
import com.guozhong.Request;
import com.guozhong.Status;
import com.guozhong.downloader.driverpool.DriverPool;


/**
 * Default implementation for {@link PageFactory}.
 */
public class DefaultPageFactory implements PageFactory {
	private static Logger logger = Logger.getLogger(DefaultPageFactory.class);

	@Override
	public Page buildRejectedMimeTypePage(Request request, Status status,DriverPool driverPool,int driverIndex) {
		return new RejectedMimeTypePage(request, status, driverPool, driverIndex);
	}

	@Override
	public Page buildOkPage(Request request,Status status, String content, WebElement root, DriverPool driverPool,int driverIndex ) {
		return new OkPage(request, status, content,root , driverPool, driverIndex);
	}

	@Override
	public Page buildErrorPage(Request request, Status error, DriverPool drivePool,int driverIndex ) {
		return new ErrorPage(request, error,drivePool,driverIndex ); 
	}

	@Override
	public Page buildRetryPage(Request request,DriverPool drivePool,int driverIndex) {
		return new RetryPage(request,drivePool,driverIndex);
	}
}
