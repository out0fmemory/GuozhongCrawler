package com.guozhong.component;

import java.util.List;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.guozhong.model.Proccessable;

public  interface  PageScript {
	/**
	 * 在这里执行你的JS代码。在执行JS代码的过程中你可以返回一些新的Proccessable处理
	 * @param driver
	 * @return
	 * @throws Exception
	 */
	public  List<Proccessable> executeJS(HtmlUnitDriver driver)throws Exception;
}
