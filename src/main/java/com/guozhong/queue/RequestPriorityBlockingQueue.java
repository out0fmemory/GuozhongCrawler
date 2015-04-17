package com.guozhong.queue;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

import com.guozhong.Request;
import com.guozhong.StartContext;

/**
 * 优先级队列
 * @author Administrator
 *
 */
public final class RequestPriorityBlockingQueue extends
		PriorityBlockingQueue<Request>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public RequestPriorityBlockingQueue(){
		super();
	}
	
}
