package com.guozhong.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.guozhong.model.Proccessable;

public final  class ProccessableUtil {

	/**
	 * 判断一个集合的数据类型是否符合cls
	 * @param dataList
	 * @param cls
	 * @return
	 */
	public static final boolean instanceOfClass
	(List<Proccessable> dataList , Class<? extends Proccessable> cls){
		if(dataList.isEmpty()){
			return false;
		}
		String dataName = dataList.get(0).getClass().getName();
		return dataName.equals(cls.getName());
	}
	
	public static final List<Proccessable> buildProcceableList(){
		return new ArrayList<Proccessable>();
	}
	
	public static <T extends Proccessable> List<T> convert(List<Proccessable> prodata , Class<T> type){
		List<T> data = new ArrayList<T>();
		data.addAll( (Collection<? extends T>) prodata);
		return  data;
	}
}
