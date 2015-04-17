package com.guozhong;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.guozhong.component.DynamicEntrance;
import com.guozhong.component.PageProcessor;
import com.guozhong.component.Pipeline;
import com.guozhong.component.listener.ChromeDriverLifeListener;
import com.guozhong.component.listener.HttpClientLifeListener;
import com.guozhong.component.listener.TaskLifeListener;
import com.guozhong.downloader.PageDownloader;
import com.guozhong.downloader.impl.ChromeDownloader;
import com.guozhong.downloader.impl.DefaultPageDownloader;
import com.guozhong.downloader.impl.SeleniumDownloader;
import com.guozhong.proxy.ProxyIpPool;
import com.guozhong.queue.DelayedPriorityBlockingQueue;
import com.guozhong.queue.RequestPriorityBlockingQueue;
import com.guozhong.thread.CountableThreadPool;
import com.guozhong.util.URLUtil;


/**
 * @author Administrator
 *
 */

public class CrawlManager{
	
	/**
	 * 工程目录
	 */
	public static final String PROJECT_DIR = "c:\\";
	
	private static Logger logger = Logger.getLogger(CrawlManager.class);
	
	/**
	 * 默认的线程数
	 */
	public static final int DEFAULT_TASK_THREADPOOL = 5;
	
	private static final CrawlManager spider = new CrawlManager();
	
	private static final Timer timer = new Timer();
	
	protected PageDownloader downloader;
	
	protected ExecutorService executorService;
	
	protected Map<String,CountableThreadPool> allThreadPool = new HashMap<String, CountableThreadPool>();
	
	protected Map<String,CrawlTask> allTask = new HashMap<String, CrawlTask>();
	
	private String currentTask ;
	
	private CrawlTimerTask timerTask;
	
	private CrawlManager(){}
	
	public final static CrawlManager getInstance(){
		return spider;
	}
	
	public static CrawlManager prepareCrawlTask(CrawlTask crawltask){
		if(spider.allTask.containsKey(crawltask.getTaskName()) && !spider.allTask.get(crawltask.getTaskName()).isSingleStartFinished()){//如果任务存在并且还在运行
			throw new IllegalArgumentException("任务已经存在");
		}else{
			spider.allTask.put(crawltask.getTaskName(), crawltask);
			spider.currentTask = crawltask.getTaskName();
			crawltask.ownerSpider(spider);
		}
		return spider;
	}
	
	public CrawlManager withThread(int threadNum){
		checkTakName();
		CountableThreadPool threadPool = allThreadPool.get(currentTask);
		if (threadPool == null || threadPool.isShutdown()) {
            if (executorService != null && !executorService.isShutdown()) {
                threadPool = new CountableThreadPool(threadNum, executorService);
            } else {
                threadPool = new CountableThreadPool(threadNum);
            }
        }
		allThreadPool.put(currentTask, threadPool);
		allTask.get(currentTask).setThreadPool(threadPool);
		return this;
	}
	
	public CrawlManager withDowloader(PageDownloader downloader){
		checkTakName();
		if(downloader != null){
			CrawlTask task = allTask.get(currentTask);
			task.setDownloader(downloader);
		}
		return this;
	}
	
	
	public CrawlManager addPageProccess(PageProcessor pageProccess){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.addPageProccess(pageProccess);
		return this;
	}
	
	public CrawlManager withPipeline(Pipeline p){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setPipeline(p);
		return this;
	}
	
	public CrawlManager setPageRetryCount(int retryCount){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setPageRetryCount(retryCount);
		return this;
	}
	
	/**
	 * 添加种子URL设置附加参数和页面编码格式
	 * @param url
	 * @param extra
	 * @param charSet
	 * @return
	 */
	public CrawlManager withStartUrl(String url,Map<String,Object> extra,String charSet){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.addStartUrl(url, extra, charSet);
		return this;
	}
	
	/**
	 * 添加种子URL并设置附加参数
	 * @param url
	 * @param extra
	 * @return
	 */
	public CrawlManager withStartUrl(String url,Map<String,Object> extra){
		checkTakName();
		withStartUrl(url, extra , null);
		return this;
	}
	
	/**
	 * 添加种子URL
	 * @param url
	 * @return
	 */
	public CrawlManager withStartUrl(String url){
		checkTakName();
		withStartUrl(url, null , null);
		return this;
	}
	
	/**
	 * 添加种子URL并设置页面编码格式
	 * @param url
	 * @param charSet
	 * @return
	 */
	public CrawlManager withStartUrl(String url,String charSet){
		checkTakName();
		withStartUrl(url, null , charSet);
		return this;
	}
	
	public CrawlManager withDynamicEntrance(DynamicEntrance dynamicEntrance){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setDynamicEntrance(dynamicEntrance);
		return this;
	}
	
	
	public CrawlManager withPriorityRequestQueue(){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setRequestQueue(new RequestPriorityBlockingQueue());
		return this;
	}
	
	/**
	 * 使用延迟优先级队列
	 * @param delayInMilliseconds
	 * @return
	 */
	public CrawlManager withDelayedPriorityRequestQueue(int delayInMilliseconds){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setRequestQueue(new DelayedPriorityBlockingQueue(delayInMilliseconds));
		return this;
	}
	
	/**
	 * 设置监听器，监听生命周期
	 * @param listener
	 * @return
	 */
	public CrawlManager withTaskLifeListener(TaskLifeListener listener){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setTaskLifeListener(listener);
		return this;
	}
	
	public CrawlManager withChromeDriverLifeListener(ChromeDriverLifeListener listener){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setChromeDriverLifeListener(listener);
		return this;
	}
	
	public CrawlManager withHttpClientLifeListener(HttpClientLifeListener listener){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setHttpClientLifeListener(listener);
		return this;
	}
	
	public CrawlManager withProxyIpPool(ProxyIpPool proxyIpPool){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.getDownloader().setProxyIpPool(proxyIpPool);
		return this;
	}
	
	/**
	 * 备份请求队列
	 * @param taskIdentification
	 * @return
	 */
	public CrawlManager enableRequestBackup(long backupPeriod){
		checkTakName();
		Matcher matcher = Pattern.compile("[/\\\\*\\?<>|]+").matcher(currentTask); 
		if(matcher.find()){
			throw new RuntimeException("任务名称不能含有非法字符");
		}
		CrawlTask task = allTask.get(currentTask);
		return this;
	}
	

	/**
	 * 启动任务
	 */
	public final void start() {
		checkTakName();
		initComponent(currentTask);
		if(timerTask != null){
			timer.schedule(timerTask, 0);
		}else{
			new Thread(allTask.get(currentTask)).start();
		}
		currentTask = null;
		timerTask = null;
		
	}
	
	private final void initComponent(String taskName){
		CountableThreadPool threadPool = allThreadPool.get(taskName);
		if (threadPool == null || threadPool.isShutdown()) {
            if (executorService != null && !executorService.isShutdown()) {
                threadPool = new CountableThreadPool(DEFAULT_TASK_THREADPOOL, executorService);
            } else {
                threadPool = new CountableThreadPool(DEFAULT_TASK_THREADPOOL);
            }
            allThreadPool.put(taskName, threadPool);
            allTask.get(taskName).setThreadPool(allThreadPool.get(currentTask));
        }
		
		PageDownloader downloader = allTask.get(taskName).getDownloader();
		if(downloader == null){
			downloader = new DefaultPageDownloader();
			allTask.get(taskName).setDownloader(downloader);
		}
		
		if(downloader instanceof ChromeDownloader){
			if(threadPool.getThreadNum() > 30){
				((ChromeDownloader)downloader).setMaxDriverCount(30);//谷歌浏览器最多driver数30个因为耗内存巨大
			}else{
				((ChromeDownloader)downloader).setMaxDriverCount(threadPool.getThreadNum());
			}
		}
	}

	private final void checkTakName(){
		if(currentTask == null){
			throw new IllegalArgumentException("操作错误，应该从方法 executeCrawlTask开始");
		}
	}
	
	public void stopTask(String name){
		/**
		 * 待实现
		 */
	}
	
	
	public void setShareExecutorService(ExecutorService sheardThreadpool){
		if(this.executorService != null && !this.executorService.isShutdown()){
			this.executorService.shutdown();
		}
		this.executorService = sheardThreadpool;
	}
	
	public final CrawlManager withTimer(int hour,long period,int endHour){
		checkTakName();
		timerTask = new CrawlTimerTask(hour, period, endHour,  currentTask);
		allTask.get(currentTask).setRepetitive(timerTask);
		int periodMinute = (int) (period/1000/60);
		String timerInfo = hour+"点开始到"+endHour+"点结束"+",每"+periodMinute+"分钟运行一次";
		allTask.get(currentTask).setTimerInfo(timerInfo);
		return this;
	}
	
	public final boolean setNewTimer(String taskName , int hour,long period,int endHour){
		CrawlTask task = allTask.get(currentTask);
		CrawlTimerTask newTimer = new CrawlTimerTask(hour, period, endHour, taskName);
		task.setNewRepetitive(newTimer);
		return true;
	}
	
	public final CrawlManager withDownloadFileThread(int thread){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setDownloadFileThread(thread);
		return this;
	}
	
	public final CrawlManager setDownloadFileDelayTime(int millisecond){
		checkTakName();
		CrawlTask task = allTask.get(currentTask);
		task.setDownloadFileDelayTime(millisecond);
		return this;
	}
	
	
	public final class CrawlTimerTask extends TimerTask{

		/**
		 * 每天开始爬行的时间
		 */
		protected int hour ;
		
		/**
		 * 执行各后续任务之间的时间间隔，单位是毫秒。
		 */
		protected long period ;
		
		protected int endHour;
		
		protected String taskName ;
		

		private CrawlTimerTask(int satrthour,  long period,
				int endHour,String taskName) {
			this.hour = satrthour;
			this.period = period;
			this.endHour = endHour;
			this.taskName = taskName;
		}
		
		public CrawlTimerTask getNextStepTask(){
			return new CrawlTimerTask(hour,  period, endHour, taskName); 
		}

		@Override
		public void run() {
			if(allTask.get(taskName) == null){
				this.cancel();
				timer.purge();
				return ;
			}
			
			while(!runable()){
				System.out.println("未达到程序运行时间");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			new Thread(allTask.get(taskName)).start();
			this.cancel();
			timer.purge();
		}
		
		private final boolean runable(){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			if((calendar.get(Calendar.HOUR_OF_DAY) >= hour && calendar.get(Calendar.HOUR_OF_DAY) <= endHour)){
				return true;
			}
			return false;
		}

		public int getHour() {
			return hour;
		}

		public long getPeriod() {
			return period;
		}


		public int getEndHour() {
			return endHour;
		}


		public String getTaskName() {
			return taskName;
		}
	}
	
	/**
	 * 重新调度定时
	 * @param oldTimerTask
	 */
	public final void redeployTimerTask(CrawlTimerTask oldTimerTask){
		CrawlTimerTask newTimerTask = oldTimerTask.getNextStepTask();
		allTask.get(oldTimerTask.taskName).setRepetitive(newTimerTask);
		timer.schedule(newTimerTask, newTimerTask.period);
		logger.info("重新定时任务:"+oldTimerTask.taskName);
	}
	
	/**
	 * 销毁任务
	 */
	public void destoryCrawTask(String taskName){
		allTask.remove(taskName);
		allThreadPool.remove(taskName);
	}
	
	/**
	 * 取得所有任务的名字
	 * @return
	 */
	public List<CrawlTask> getAllTask(){
		List<CrawlTask> allName = new ArrayList<CrawlTask>();
		allName.addAll(allTask.values());
		return allName;
	}
	
	/**
	 * 取得一个任务的状态
	 * @param name
	 * @return
	 */
	public String getTaskStatus(String name){
		CrawlTask task = allTask.get(name);
		return task.getStatus();
	}
	
	public CrawlTask getCrawlTask(String name){
		return allTask.get(name);
	}
	
}
