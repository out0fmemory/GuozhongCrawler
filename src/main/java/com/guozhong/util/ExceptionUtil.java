package com.guozhong.util;

import org.openqa.selenium.WebDriverException;
import org.testng.xml.LaunchSuite.ExistingSuite;

import com.guozhong.exception.ProxyIpLoseException;
import com.guozhong.exception.ReadByteException;

public final class ExceptionUtil {
	
	public static final boolean existProxyException(Exception e){
		if(e instanceof ProxyIpLoseException){
			return true;
		}
		else if(e instanceof org.apache.http.conn.ConnectTimeoutException){
			return true;
		}
		else if(e instanceof java.net.SocketTimeoutException){
			return true;
		}
		else if(e instanceof org.openqa.selenium.TimeoutException){
			return true;
		}
		else if(e instanceof org.apache.http.TruncatedChunkException){
			return true;
		}
		else if(e instanceof java.net.SocketException){
			return true;
		}
		else if(e instanceof ReadByteException){
			return true;
		}
		else if(e instanceof java.net.ConnectException){
			return true;
		}
		else if(e instanceof org.apache.http.NoHttpResponseException){
			return true;
		} 
		else if(e instanceof WebDriverException){
			return true;
		} 
		
		return false;
	}

}
