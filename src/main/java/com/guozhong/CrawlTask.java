package com.guozhong;


import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.guozhong.CrawlManager.CrawlTimerTask;
import com.guozhong.component.DynamicEntrance;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.PageScript;
import com.guozhong.component.Pipeline;
import com.guozhong.component.listener.ChromeDriverLifeListener;
import com.guozhong.component.listener.HttpClientLifeListener;
import com.guozhong.component.listener.TaskLifeListener;
import com.guozhong.downloader.PageDownloader;
import com.guozhong.downloader.impl.ChromeDownloader;
import com.guozhong.downloader.impl.DefaultFileDownloader;
import com.guozhong.downloader.impl.DefaultPageDownloader;
import com.guozhong.downloader.impl.SeleniumDownloader;
import com.guozhong.exception.EntranceException;
import com.guozhong.exception.PageProccessorException;
import com.guozhong.model.Proccessable;
import com.guozhong.page.ErrorPage;
import com.guozhong.page.OkPage;
import com.guozhong.page.Page;
import com.guozhong.page.RejectedMimeTypePage;
import com.guozhong.page.RetryPage;
import com.guozhong.queue.RequestPriorityBlockingQueue;
import com.guozhong.thread.CountableThreadPool;
import com.guozhong.util.ProccessableUtil;

/**
 * 爬虫任务类
 * @author Administrator
 *
 */

public class CrawlTask implements Runnable{
	
	private static Logger logger = Logger.getLogger(CrawlTask.class);
	
	
	private static final int DEFAULT_MAX_PAGE_RETRY_COUNT = 1;
	
	/**
	 * 默认文件下载线程数 
	 */
	private static final int DEFAULT_DOWNLOAD_FILE_THREAD = 3;
	
	private  String taskName ;
	
	
	private BlockingQueue<StartContext> startRequests = new LinkedBlockingQueue<StartContext>();
	
	/**
	 * 备份初始URL
	 */
	private List<StartContext> allStartBackups = new ArrayList<StartContext>();
	
	/**
	 * 默认用无延迟、优先级队列
	 */
	private BlockingQueue<Request> requestQueue = new RequestPriorityBlockingQueue();
	
	private PageDownloader downloader ;
	
	private DefaultFileDownloader defaultFileDownloader;
	
	private int download_file_thread = DEFAULT_DOWNLOAD_FILE_THREAD;
	
	private CountableThreadPool downloadThreadPool ;
	
	private CountableThreadPool offlineHandleThreadPool ; //离线处理线程
	
	private int maxPageRetryCount = DEFAULT_MAX_PAGE_RETRY_COUNT;
	
	private Map<String,PageProcessor> taskPageProccess = new HashMap<String,PageProcessor>();
	
	private static final String FIRST_KEY = "FIRST_PAGEPROCCESS";
	
	private Pipeline  pipeline ;
	
	/**
	 * 下次运行的定时任务
	 */
	private CrawlTimerTask repetitive ;
	
	/**
	 * 新的定时任务
	 */
	private CrawlTimerTask newRepetitive;
	
	private CrawlManager spider ;
	
	private StartContext context;
	
	/**
	 * 生命周期监听类
	 */
	private TaskLifeListener taskLifeListener;
	
	/**
	 * 最近一次任务开始的时间
	 */
	private long lastStartTime ;
	
	/*
	 * 当前运行的状态
	 */
	private String status ;
	
	private String timerInfo ; 
	
	/**
	 * 动态入口URL
	 */
	private DynamicEntrance dynamicEntrance ;
	
	
	public CrawlTask(String name){
		name = name.replaceAll("[/\\\\*\\?<>|]", "_");//  /\*?<>|  替换文件名非法字符
		this.taskName  =name;
	}
	
	
	public String getTaskName(){
		return taskName;
	}

	public TaskLifeListener getTaskLifeListener(){
		return taskLifeListener;
	}

	public void setTaskLifeListener(TaskLifeListener taskLifeListener) {
		this.taskLifeListener = taskLifeListener;
	}
	
	
	public DynamicEntrance getDynamicEntrance() {
		return dynamicEntrance;
	}

	
	public void setDynamicEntrance(DynamicEntrance dynamicEntrance) {
		this.dynamicEntrance = dynamicEntrance;
	}


	public void setDownloadFileThread(int download_file_thread){
		if(download_file_thread > 200){
			throw new RuntimeException("下载线程不能大于200个");
		}
		if(download_file_thread<3){
			this.download_file_thread = 3;
		}else{
			this.download_file_thread = download_file_thread;
		}
	}
	
	public void setDownloadFileDelayTime(int time){
		initDownloadFileThreadPool();
		defaultFileDownloader.setDelayTime(time);
	}

	public long getLastStartTime() {
		return lastStartTime;
	}

	public void addStartContxt(StartContext context)
	{
		context.setPipeline(pipeline);
		allStartBackups.add(context);
	}
	
	public void addStartUrl(String url,Map<String,Object> extra){
		StartContext context = new StartContext(url);
		if(extra != null){
			for (Map.Entry<String, Object> keyValuePair :  extra.entrySet()) {
				context.putContextAttribute(keyValuePair.getKey(), keyValuePair.getValue());
			}
		}
		addStartContxt(context);
	}
	
	public void addStartUrl(String url){
		StartContext context = new StartContext(url);
		addStartContxt(context);
	}
	
	/**
	 * 添加种子URL设置附加参数和页面编码格式
	 * @param url
	 * @param extra
	 * @param charSet
	 * @return
	 */
	public void addStartUrl(String url,Map<String,Object> extra,String charSet){
		StartContext context = null;
		if(charSet != null){
			context = new StartContext( url , charSet);
		}else{
			context = new StartContext(url);
		}
		if(extra != null){
			for (Map.Entry<String, Object> keyValuePair :  extra.entrySet()) {
				context.putContextAttribute(keyValuePair.getKey(), keyValuePair.getValue());
			}
		}
		addStartContxt(context);
	}
	
	/**
	 * 清除所有初始URL  
	 * 在监听任务里初始化入口URL前调用该方法释放之前的入口url
	 */
	private void clearStartRequest(){
		startRequests.clear();
		allStartBackups.clear();
	}
	
	public void pushRequest(Request request){
		if(request != null){
			request.recodeRequest();//记录请求
			this.requestQueue.add(request);
		}
	}
	
	public int getPageRetryCount() {
		return maxPageRetryCount;
	}
	
	public void setPageRetryCount(int pageRetryCount) {
		this.maxPageRetryCount = pageRetryCount;
	}
	
	public void setDownloader(PageDownloader downloader) {
		this.downloader = downloader;
	}
	
	public void setChromeDriverLifeListener(ChromeDriverLifeListener chromeDriverLifeListener) {
		if(downloader instanceof ChromeDownloader){
			ChromeDownloader d = (ChromeDownloader) downloader;
			d.setChromeDriverLifeListener(chromeDriverLifeListener);
		}else{
			throw new RuntimeException("setChromeDriverLifeListener()需要使用  ChromeDownloader");
		}
	}
	
	public void setHttpClientLifeListener(HttpClientLifeListener httpClientLifeListener) {
		if(downloader instanceof DefaultPageDownloader){
			DefaultPageDownloader d = (DefaultPageDownloader) downloader;
			d.setHttpClientLifeListener(httpClientLifeListener);
		}else{
			throw new RuntimeException("setHttpClientLifeListener()需要使用  DefaultPageDownloader");
		}
	}
	
	public Pipeline getPipeline() {
		return pipeline;
	}
	
	public void setPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
		for(StartContext context : allStartBackups){
			context.setPipeline(pipeline);
		}
	}
	
	public BlockingQueue<Request> getRequestQueue() {
		return requestQueue;
	}
	
	public void setRequestQueue(BlockingQueue<Request> q){
		this.requestQueue = q;
	}
	
	public void setThreadPool(CountableThreadPool threadPool) {
		if(this.downloadThreadPool != null && !this.downloadThreadPool.isShutdown()){
			this.downloadThreadPool.shutdown();
		}
		this.downloadThreadPool = threadPool;
		initOfflineThread();
	}

	/**
	 * 初始化离线处理线程池
	 */
	private final void initOfflineThread() {
		if(offlineHandleThreadPool!=null){
			return;
		}
		if(downloadThreadPool.getThreadNum()<=20){
			 offlineHandleThreadPool = new CountableThreadPool(5);
		}else{
			int offlineThreadNum = downloadThreadPool.getThreadNum()/4;
			offlineHandleThreadPool = new CountableThreadPool(offlineThreadNum);
		}
	}
	
	public final Request poolRequest() throws InterruptedException{
		Request req = null;
		while (true) {
			if(downloadThreadPool.getIdleThreadCount() == 0){
				Thread.sleep(100);
				continue;//等待有线程可以工作
			}
			if ((!requestQueue.isEmpty() || !isSingleStartFinished())) {
				req = requestQueue.poll();
				if (req != null)
					break;
				else 
					Thread.sleep(100);
			} else {
				break;
			}
		}
		return req;
	}
	
	/**
	 * 每个入口URL及子队列全部抓取完成则返回true
	 * @return
	 */
	public boolean isSingleStartFinished(){
		int alive = downloadThreadPool.getThreadAlive();
		int offline = offlineHandleThreadPool.getThreadAlive();
		if(alive == 0 && requestQueue.isEmpty() && offline == 0){
			return true;
		}
		return false;
	}
	
	public boolean isRuning(){
		if(downloadThreadPool.isShutdown()){
			return false;
		}
		int alive = downloadThreadPool.getThreadAlive();
		int offline = offlineHandleThreadPool.getThreadAlive();
		if(alive > 0 || !requestQueue.isEmpty() || offline > 0)
			return true;
		return false;
	}
	
	public PageDownloader getDownloader(){
		return this.downloader;
	}
	
	@Override
	public void run() {
		logger.info("开始抓取");
		initDynamicEntrance();
		try{
			initTask();
		}catch(EntranceException e){
			logger.warn("入口URL列表为空", e);
			spider.destoryCrawTask(taskName);//销毁任务
			downloadThreadPool.shutdown();
			offlineHandleThreadPool.shutdown();
			//任务生命周期回调
			if(taskLifeListener != null){
				taskLifeListener.onFinished(this);
			}
		}
		
		if(context==null){
			throw new NullPointerException("入口上下文为Null");
		}
		
		lastStartTime  = System.currentTimeMillis();
		status = "抓取中";
		downloader.open();//打开下载器
		
		while(!Thread.currentThread().isInterrupted()){
			Request request ;
			try{
				request = poolRequest();
				if(request == null){
					if(isSingleStartFinished() && !nextStartUrlQueue()){//如果当前入口URL抓完并且没有了下一个入口URL则完成任务
						destoryCrawlTask();
						break;
					}else{
						//seelp(0.2f);//每抓完一个入口URL  沉睡200ms
						continue;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				logger.error("轮询队列出错",e);
				break;
			}
			final Request finalRequest = request;
			final StartContext finalContext  = context;
			invokeDownload(finalRequest, finalContext);
		}
		
	}

	/**
	 * 初始化任务
	 */
	private void initTask() throws EntranceException{
		if(allStartBackups.isEmpty()){
			throw new EntranceException("种子URL数至少有1个");
		}
		if(startRequests.isEmpty()){
			for (StartContext context: allStartBackups) {//开始队列加载种子URL
				startRequests.add(context);
			}
		}
		if(taskLifeListener != null){
			taskLifeListener.onStart(this);
		}
		nextStartUrlQueue();
	}


	/**
	 * 动态加载入口URL
	 */
	private final void initDynamicEntrance() {
		if(dynamicEntrance != null){
			if(dynamicEntrance.isClearLast()){
				clearStartRequest();//清除之前的入口URL
				logger.info("清除上个任务入口URL");
			}
			
			dynamicEntrance.onStartLoad();
			
			List<String> urls = dynamicEntrance.load();
			if(urls != null){
				for (String url : urls) {
					addStartUrl(url);
				}
			}
			
			List<StartContext> startContexts = dynamicEntrance.loadStartContext();
			if(startContexts != null){
				for (StartContext sc : startContexts) {
					addStartContxt(sc);
				}
			}
			logger.info("共加载入口URL"+allStartBackups.size()+"个"); 
			dynamicEntrance.onLoadComplete();
		}
	}

	/**
	 * 调用下载
	 * @param finalRequest
	 * @param finalContext
	 */
	private final void invokeDownload(final Request finalRequest,
			final StartContext finalContext) {
		if(finalRequest.isBinary()){
			initDownloadFileThreadPool();
			defaultFileDownloader.downloadFile(finalRequest);
		}else{
			downloadThreadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					PageProcessor pageProccess = findPageProccess(finalRequest.getTag());
					if(pageProccess == null)return;
					Page page = downloader.download(finalRequest,CrawlTask.this);
					if(page == null) return;//取不到page则返回
					//System.out.println("抓取:"+finalRequest.getUrl()+"\tCode:"+page.getStatusCode());
					logger.info("抓取:"+finalRequest.getUrl()+"\tCode:"+page.getStatusCode());
					offlineHandle(pageProccess, page, finalContext);
				}
			});
		}
	}

	/**
	 * 初始化文件下载线程池
	 */
	private void initDownloadFileThreadPool() {
		if(defaultFileDownloader == null){
			synchronized (this){
				if(defaultFileDownloader == null){
					defaultFileDownloader = new DefaultFileDownloader(download_file_thread);
				}
			}
		}
	}
	
	/**
	 * 离线处理
	 * @param pageProccess
	 * @param page
	 * @return
	 */
	private final void offlineHandle(final PageProcessor pageProccess ,final Page page,final StartContext finalContext){
		offlineHandleThreadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				if(page instanceof RejectedMimeTypePage){
				}else if(page instanceof ErrorPage){
					try {
						pageProccess.proccessErrorPage(page,finalContext);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else if(page instanceof RetryPage){
					RetryPage retryPage = (RetryPage) page;
					if(retryPage.getRetryCount() < maxPageRetryCount){
						Request retryRequest = retryPage.getRequest();
						pushRequest(retryRequest);
						logger.warn("重新请求URL："+retryPage.getRequest().getUrl());
					}else{
						logger.error("下载次数超过"+maxPageRetryCount+":"+retryPage.getRequest().getUrl()+" 被丢弃");
					}
				}else if(page instanceof OkPage){
					List<Proccessable> proccessables = ProccessableUtil.buildProcceableList();
					try {
					    pageProccess.process((OkPage) page,finalContext,proccessables);
						handleProccessable(proccessables);
					} catch (Exception e) {
						if(page.isNeedPost()){
							page.handleComplete();
						}
						e.printStackTrace();
						logger.error("离线处理异常URL:"+page.getRequest().getUrl(),e);
					}
				}
			}
		});
	}
	
	/**
	 * 从入口URL队列取得一个URL
	 * @return
	 */
	private boolean nextStartUrlQueue() {
		context = startRequests.poll();
		if(context != null){
			logger.debug("startRequests : "+context.getStartRequest().getUrl());
			pushRequest(context.getStartRequest());
			List<Request> subRequest = context.getSubRequest();
			for (Request sub : subRequest) {
				pushRequest(sub);//添加跟进url
			}
		}
		return context!=null;
	}

	/**
	 * 找对应的解析器
	 * @param requestTag
	 * @return
	 */
	public final PageProcessor findPageProccess(String requestTag) {
		PageProcessor proccess = null;
		if(requestTag == null){
			proccess = taskPageProccess.get(FIRST_KEY);
		}else{
			proccess = taskPageProccess.get(requestTag);
		}
		return proccess;
	}
	
	public void handleProccessable(List<Proccessable> proccessables){
		Map<String,List<Proccessable>> data = new HashMap<String,List<Proccessable>>();
		List<Request> genjinRequest  = new ArrayList<Request>();
		for (Proccessable procdata : proccessables) {
			if(!(procdata instanceof Request)){
				String className = procdata.getClass().getName();
				if(!data.containsKey(className)){
					data.put(className, new ArrayList<Proccessable>());
				}
				List<Proccessable> list = data.get(className);
				list.add(procdata);
			}else{
				Request follow = (Request) procdata;
				if(!genjinRequest.contains(follow)){
					genjinRequest.add(follow);
				}
			}
		}
		if(pipeline != null){//为保证信息先存储
			for (List<Proccessable> datalist: data.values()) {
				pipeline.proccessData(datalist);
			}
		}
		//跟进URL加入队列
		for (Request req : genjinRequest) {
			pushRequest(req);
		}
	}
	
	public void addPageProccess(PageProcessor proccess){
		if(!taskPageProccess.containsValue(proccess)){
			if(proccess.getTag() == null){
				if(!taskPageProccess.containsKey(FIRST_KEY)){
					taskPageProccess.put(FIRST_KEY,proccess);
				}else{
					throw new PageProccessorException("已经存在一个FIRST_KEY PageProccessor");
				}
			}else{
				taskPageProccess.put(proccess.getTag(),proccess);
			}
			PageScript javaScript = proccess.getJavaScript();
			if(javaScript != null){
				if(downloader == null || !(downloader instanceof SeleniumDownloader)){
					throw new RuntimeException("请先实例化SeleniumDownloader");
				}
				SeleniumDownloader seleniumDownloader = (SeleniumDownloader) downloader;
				seleniumDownloader.addJavaScriptFunction(proccess.getTag(),javaScript);
			}
		}else{
			throw new RuntimeException("不能添加重复的PageProccesor");
		}
	}
	
	
	protected CrawlTimerTask getRepetitive() {
		return repetitive;
	}
	
	protected void setRepetitive(CrawlTimerTask repetitive) {
		this.repetitive = repetitive;
	}
	
	/**
	 * 设置新的定时
	 * @param newRepetitive
	 */
	public void setNewRepetitive(CrawlTimerTask newRepetitive){
		if(newRepetitive != null){
			this.newRepetitive = newRepetitive;
			int periodMinute = (int) (newRepetitive.getPeriod()/1000/60);
			this.timerInfo = newRepetitive.getHour()+"点开始到"+newRepetitive.getEndHour()+"点结束"+",每"+periodMinute+"分钟运行一次";
		}
	}
	
	protected void ownerSpider(CrawlManager spider){
		this.spider = spider;
	}
	
	public String getStatus() {
		return status;
	}

	public String getTimerInfo() {
		return timerInfo;
	}

	public void setTimerInfo(String timerInfo) {
		this.timerInfo = timerInfo;
	}

	/**
	 * 任务完成销毁任务
	 */
	private final void destoryCrawlTask(){
		logger.info(taskName+"任务完成销毁");
		//释放下载器
		try {
			downloader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//定时处理
		if(repetitive == null){
			spider.destoryCrawTask(taskName);//销毁任务
			downloadThreadPool.shutdown();
			offlineHandleThreadPool.shutdown();
		}else{
			if(newRepetitive != null){
				spider.redeployTimerTask(newRepetitive);//发布新任务
				newRepetitive = null;
			}else{
				spider.redeployTimerTask(repetitive);//发布任务
			}
			status = "休眠中";
		}
		//任务生命周期回调
		if(taskLifeListener != null){
			taskLifeListener.onFinished(this);
		}
	}
	
	
}
