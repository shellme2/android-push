package com.eebbk.bfc.im.push.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeFormatUtil {

    //构造函数私有，防止恶意新建
    private TimeFormatUtil(){}

    public static String format(long milliseconds) {
        if (milliseconds < 1000) {
            milliseconds = 1000;
        }
        Date date = new Date(milliseconds - 8 * 3600 * 1000);
        return new SimpleDateFormat("HH:mm:ss", Locale.US).format(date);
    }
}
