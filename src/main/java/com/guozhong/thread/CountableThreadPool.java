package com.guozhong.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CountableThreadPool {
	    private int threadNum;

	    private AtomicInteger threadAlive = new AtomicInteger();

	    private ReentrantLock reentrantLock = new ReentrantLock();

	    private Condition condition = reentrantLock.newCondition();
	    
	    private ExecutorService executorService;

	    public CountableThreadPool(int threadNum) {
	        this.threadNum = threadNum;
	        this.executorService = Executors.newFixedThreadPool(threadNum);
	    }

	    public CountableThreadPool(int threadNum, ExecutorService executorService) {
	        this.threadNum = threadNum;
	        this.executorService = executorService;
	    }

	    public void setExecutorService(ExecutorService executorService) {
	        this.executorService = executorService;
	    }

	    public int getThreadAlive() {
	        return threadAlive.get();
	    }

	    public int getThreadNum() {
	        return threadNum;
	    }

	    

	    public void execute(final Runnable runnable) {


	        if (threadAlive.get() >= threadNum) {
	            try {
	                reentrantLock.lock();
	                while (threadAlive.get() >= threadNum) {
	                    try {
	                        condition.await();
	                    } catch (InterruptedException e) {
	                    }
	                }
	            } finally {
	                reentrantLock.unlock();
	            }
	        }
	        threadAlive.incrementAndGet();
	        executorService.execute(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    runnable.run();
	                } finally {
	                    try {
	                        reentrantLock.lock();
	                        threadAlive.decrementAndGet();
	                        condition.signal();
	                    } finally {
	                        reentrantLock.unlock();
	                    }
	                }
	            }
	        });
	    }

	    public boolean isShutdown() {
	        return executorService.isShutdown();
	    }

	    public void shutdown() {
	        executorService.shutdown();
	    }
	    
	    public int getIdleThreadCount(){
	    	return threadNum - getThreadAlive();
	    }
}
