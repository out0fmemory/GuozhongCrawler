package com.guozhong.util;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public final class JavaScriptUtil {
	
	public static final Object click(HtmlUnitDriver driver,WebElement ele){
		Object o = driver.executeScript("arguments[0].click();", ele);
		try {
			Thread.sleep(1000);//执行js后是默认等1S 保证html源码重新被加载
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return o;
	}

}
