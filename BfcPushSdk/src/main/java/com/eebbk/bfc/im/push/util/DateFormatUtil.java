package com.eebbk.bfc.im.push.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间日期工具类
 */
public class DateFormatUtil {

	//构造函数私有，防止恶意新建
	private DateFormatUtil(){}

	public static final String FORMAT_1 = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT_2 = "yyyy-MM-dd-HH-mm-ss";

	public static String format() {
		return format(FORMAT_1);
	}

	public static String format(String format, Date date) {
		return new SimpleDateFormat(format, Locale.US).format(date);
	}

	public static String format(String format, long ms) {
		return format(format, new Date(ms));
	}

	public static String format(String format) {
		return format(format, new Date());
	}
}
