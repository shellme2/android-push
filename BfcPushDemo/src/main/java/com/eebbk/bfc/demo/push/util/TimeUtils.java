package com.eebbk.bfc.demo.push.util;

import com.eebbk.bfc.im.push.util.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
    private static final String TAG = "TimeUtils";

    private TimeUtils(){}

    public static String getNowTime(){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return format.format(new Date());
    }

    public static String formatTime(Long time){
        LogUtils.d(TAG, "time: "+time);
        SimpleDateFormat format=new SimpleDateFormat("mm分ss秒SSS毫秒", Locale.CHINA);
        return format.format(new Date(time));
    }
}
