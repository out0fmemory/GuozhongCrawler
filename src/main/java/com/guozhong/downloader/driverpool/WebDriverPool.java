package com.guozhong.downloader.driverpool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriver;

import com.guozhong.downloader.impl.ExtendWebDriver;
import com.guozhong.downloader.impl.SimpleHttpClient;





/**
 * 目前只会缓存能不执行JS的Driver，因为能执行JS的Driver在多次执行后会出现报错
 * @author code4crafter@gmail.com <br>
 * Date: 13-7-26 <br>
 * Time: 下午1:41 <br>
 */
public final class WebDriverPool extends DriverPool{
	
	public final static int DEFAULT_TIMEOUT = 15;//默认加载网页超时8秒

    private final static int DEFAULT_NOTJSDRIVER = 10;

    
    /**
     * 不可执行JS的驱动
     */
    private int notjsdriver ;
    
    private final static int STAT_RUNNING = 1;

    private final static int STAT_CLODED = 2;
    
    private int pageLoadTimeout ;

    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);

    /**
     * 统计所有用过的webDriverList。好最后释放掉
     */
    private List<ExtendWebDriver> webDriverList = Collections.synchronizedList(new ArrayList<ExtendWebDriver>());
    /**
     * store webDrivers available
     */
    private LinkedBlockingQueue<ExtendWebDriver> queue = new LinkedBlockingQueue<ExtendWebDriver>();
    

    public WebDriverPool(int notjsdriver,int pageLoadTimeout) {
        this.notjsdriver = notjsdriver;
        this.pageLoadTimeout = pageLoadTimeout;
    }

    public WebDriverPool() {
        this(DEFAULT_NOTJSDRIVER,DEFAULT_TIMEOUT);
    }

    /**
     * 从池中取得一个WebDriver
     * @return
     * @throws InterruptedException
     */
    public final ExtendWebDriver get(boolean isExeJs) throws InterruptedException {
        checkRunning();
        ExtendWebDriver poll ;
        if(isExeJs){
        	poll = new ExtendWebDriver(true);
        }else{
        	if(webDriverList.size() < min_drivers){
        		synchronized (webDriverList) {
        			if(webDriverList.size() < min_drivers){
        				createExtendWebDriver();
        			}
        		}
        	}
        	poll = queue.poll();
        }
        
        if (poll != null) {
            return poll;
        }
        
        if (webDriverList.size() < max_drivers) {//如果webDriver使用的数量没有达到capacity则继续创建webDriver
            synchronized (webDriverList) {
                if (webDriverList.size() < max_drivers) {
                	createExtendWebDriver();
                }
            }
        }
        return queue.take();//此方法并不保证立即返回WebDriver，有可能等待之前的WebDriver执行完回到pool中
    }

	private void createExtendWebDriver() {
		ExtendWebDriver e = new ExtendWebDriver(false);
		int driverIndex = webDriverList.size();
		e.setIndex(driverIndex);
		e.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
		queue.add(e);
		webDriverList.add(e);
	}

    public final void returnToPool(ExtendWebDriver webDriver) {//将WebDriver添加到pool中
        checkRunning();
        if(webDriver.isJavascriptEnabled()){
        	webDriver.quit();
        }else{
        	webDriver.clearHeaders();
        	queue.add(webDriver);
        }
    }
    
    /**
     * 监测是否在运行
     */
    protected final void checkRunning() {
        if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
            throw new IllegalStateException("Already closed! please open");
        }
    }
    
    /**
     * 打开
     */
    public final void open(){
    	if (!stat.compareAndSet(STAT_CLODED, STAT_RUNNING)) {
            //throw new IllegalStateException("Already open!");
            System.out.println("WebDriverPool Already open!");
        }
    }

    /**
     * 关闭所有的WebDriver
     */
    public final void closeAll() {
        boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
        if (!b) {
            throw new IllegalStateException("Already closed!");
        }
        for (WebDriver webDriver : webDriverList) {
            webDriver.quit();
        }
        webDriverList.clear();
        queue.clear();
    }
    
    public final void setPageLoadTimeout(int timeout){
    	for (WebDriver driver : webDriverList) {
			driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
		}
    	this.pageLoadTimeout = timeout;
    }

	@Override
	public Object getDriver(int driverIndex) {
		if(getIndexs.contains(driverIndex)){
			return null;
		}else{
			System.out.println("拿出"+driverIndex);
			getIndexs.add(driverIndex);
		}
		for (ExtendWebDriver client : this.webDriverList) {
			if(client.getIndex() == driverIndex){
				queue.remove(client);//队列移除实例。防止处理未完成之前使用
				return client;
			}
		}
		return null;
	}

	@Override
	public void handleComplete(Object driver) {
		ExtendWebDriver extendWebDriver = (ExtendWebDriver) driver;
		getIndexs.remove(extendWebDriver.getIndex());//清除限制
    	System.out.println("加入"+extendWebDriver.getIndex());
    	queue.add(extendWebDriver);//回到队列
	}

}
