package com.guozhong.downloader.driverpool;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public abstract class DriverPool {
	
	/**
	 * 保存取得出去处理的index
	 */
	 protected Set<Integer> getIndexs = Collections.synchronizedSet(new HashSet<Integer>());
	
	 protected int max_drivers = Integer.MAX_VALUE;
	 
	 protected int min_drivers = 1;
	 
	 
	 public final void setMaxDriverCount(int count){
	    	this.max_drivers = count;
	 }
	 
	 public final int getMaxDriverCount(){
		 return max_drivers;
	 }
	 
	 public final void setMinDriverCount(int count){
		 this.min_drivers = count;
	 }
	 
	 public final int getMinDriverCount(){
		 return min_drivers;
	 }
	 
	 public abstract void open();
	 
	 /**
	  * 通过index编号取得一个driver去，且被取得出去的driver不能回到队列中
	  * @param driverIndex
	  * @return
	  */
	 public abstract Object getDriver(int driverIndex);
	 
	 public abstract void handleComplete(Object driver);
	 
	 public abstract void closeAll();
}
