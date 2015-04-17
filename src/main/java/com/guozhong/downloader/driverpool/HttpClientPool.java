package com.guozhong.downloader.driverpool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.openqa.selenium.WebDriver;

import com.guozhong.component.listener.HttpClientLifeListener;
import com.guozhong.downloader.impl.HttpClientFactory;
import com.guozhong.downloader.impl.SimpleHttpClient;
import com.guozhong.exception.DriverCreateException;

/**
 * @author code4crafter@gmail.com <br>
 * Date: 13-7-26 <br>
 * Time: 下午1:41 <br>
 */
public final class HttpClientPool extends DriverPool{
	

	
	
    private HttpClientLifeListener httpClientLifeListener = null;

    /**
     * 统计用过的webDriverList。好释放
     *   
     * */
    private List<SimpleHttpClient> httpClientList = Collections.synchronizedList(new ArrayList<SimpleHttpClient>());
    /**
     * store webDrivers available
     */
    private LinkedBlockingQueue<SimpleHttpClient> queue = new LinkedBlockingQueue<SimpleHttpClient>();
    

    public HttpClientPool() {
    }


    /**
     * 从池中取得一个DefaultHttpClient
     * @return
     * @throws InterruptedException
     */
    public final SimpleHttpClient get() throws InterruptedException {
    	SimpleHttpClient poll = null;
    	if(httpClientList.size() < min_drivers){
    		synchronized (httpClientList) {
    			if(httpClientList.size() < min_drivers){
    				createSimpleHttpClient();
    			}
    		}
    	}
    	poll = queue.poll();
        if (poll != null && !getIndexs.contains(poll.getIndex())) {
            return poll;
        }
        if (httpClientList.size() < max_drivers) {//如果webDriver使用的数量美誉达到capacity则继续创建webDriver
            synchronized (httpClientList) {
                if (httpClientList.size() < max_drivers) {
                	createSimpleHttpClient();
                }
            }
        }
        return queue.take();//此方法并不保证立即返回WebDriver，有可能等待之前的WebDriver执行完回到poo
    }

    /**
     */
	private final void createSimpleHttpClient(){
		SimpleHttpClient poll;
		int driverIndex = httpClientList.size() ;
		poll = new SimpleHttpClient();
		poll.setIndex(driverIndex);
		if(httpClientLifeListener != null){
			httpClientLifeListener.onCreated(driverIndex,poll);
		}
		queue.add(poll);
		httpClientList.add(poll);
	}

    public final void returnToPool(SimpleHttpClient httpClient) {//将HttpClient添加到pool   	
    	if(!getIndexs.contains(httpClient.getIndex())){//被取得出去的driver不能回到队列   		 
    		queue.add(httpClient);
    	}
    }
    
    /**
     * 打开
     */
    public final void open(){
    }
    
    /**
     * 关闭DefaultHttpClient
     */
    @SuppressWarnings("deprecation")
	public final void closeAll() {
    	for (SimpleHttpClient client : httpClientList) {
    		if(httpClientLifeListener != null){
        		httpClientLifeListener.onQuit(client.getIndex(),client);
        	}
//    		try {
//				client.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
        //httpClientList.clear();
        //queue.clear();
    }
    
    public final void setPageLoadTimeout(int timeout){
    }
    
	public void setHttpClientLifeListener(HttpClientLifeListener httpClientLifeListener) {
		this.httpClientLifeListener = httpClientLifeListener;
	}


	@Override
	public Object getDriver(int driverIndex) {
		if(getIndexs.contains(driverIndex)){
			return null;
		}else{
			getIndexs.add(driverIndex);
		}
		for (SimpleHttpClient client : httpClientList) {
			if(client.getIndex() == driverIndex){
				queue.remove(client);//队列移除实例。防止处理未完成之前使用
				return client;
			}
		}
		return null;
	}


	@Override
	public void handleComplete(Object driver) {
		SimpleHttpClient httpClient = (SimpleHttpClient) driver;
		getIndexs.remove(httpClient.getIndex());//清除限制
    	queue.add(httpClient);//回到队列
	}
	
}
