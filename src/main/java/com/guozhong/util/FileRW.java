package com.guozhong.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public final class FileRW {
	
	public static final String readFile(String path,String charsetName){
		StringBuffer sb  = new StringBuffer();
		FileInputStream in = null;
		InputStreamReader reader = null;
		BufferedReader br = null;
		String temp = null;
		try{
			in = new FileInputStream(path);
			reader = new InputStreamReader(in, charsetName);
			br = new BufferedReader(reader);
			while((temp = br.readLine())!=null){
				sb.append(temp).append("\n");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	public static final void writeFile(String content, String  file){
		writeFile(content, file, false);
	}
	
	public static final void writeFile(String content, String  file,boolean append){
		BufferedWriter bw = null;
		FileOutputStream out = null;
		OutputStreamWriter write = null;
		try{
			out = new FileOutputStream(file,append);
			write = new OutputStreamWriter(out);
			bw = new BufferedWriter(write);
			bw.write(content);
			bw.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(bw!=null){
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(write!=null){
				try {
					write.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static final String readFile(String path){
		return readFile(path, "utf-8");
	}
	
	
}
