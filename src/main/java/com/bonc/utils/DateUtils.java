package com.bonc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	public static void main(String args[]){
		System.out.println(getDate(new Date()));
	}
	public static String getDate(Date date) {
		String time = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		time = sdf.format(date);
		return time;
	}
}
