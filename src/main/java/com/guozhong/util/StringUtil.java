package com.guozhong.util;

public final class StringUtil {
	
	public static final boolean  existStr(String string , String ... parmes){
		for (int i = 0; i < parmes.length; i++) {
			if(string.contains(parmes[i])){
				return true;
			}
		}
		return false;
	}

}
