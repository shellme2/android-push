package com.eebbk.bfc.im.push.util;

import android.util.Log;

public class LogUtils {

    //构造函数私有，防止恶意新建
    private LogUtils(){}

    private static boolean isDebug;

    private static boolean hasV=true;
    private static boolean hasD=true;
    private static boolean hasI=true;
    private static boolean hasW=true;
    private static boolean hasWtf=true;
    private static boolean hasE=true;

    private static final String TAG="com.eebbk.bfc.im.push";

    public static void setDebugMode(boolean isDebug) {
        LogUtils.isDebug = isDebug;
    }

    public static void setSaveLevel(boolean v, boolean d, boolean i, boolean w, boolean e) {

    }

    public static void v(String text) {
        v(TAG,text);
    }

    public static void d(String text) {
        d(TAG,text);
    }

    public static void i(String text) {
        i(TAG,text);
    }

    public static void w(String text) {
        w(TAG,text);
    }

    public static void e(String text) {
        e(TAG,text);
    }

    public static void e(Throwable throwable) {
        e(TAG,throwable);
    }

    public static void e(String tag, Throwable throwable) {
        e(TAG,tag,throwable);
    }

    public static void test(String text) {
        e(text);
    }

    public static boolean isDebug() {
        return isDebug;
    }


    public static void v(String tag,String msg){
        if(hasV){
            Log.v(tag,msg);
        }
    }
    public static void v(String tag,String msg,Throwable tr){
        if(hasV){
            Log.v(tag, msg,tr);
        }
    }

    public static void d(String tag,String msg){
        if(hasD){
            Log.d(tag,msg);
        }
    }
    public static void d(String tag,String msg,Throwable tr){
        if(hasD){
            Log.d(tag, msg,tr);
        }
    }

    public static void i(String tag,String msg){
        if(hasI){
            Log.i(tag,msg);
        }
    }
    public static void i(String tag,String msg,Throwable tr){
        if(hasI){
            Log.i(tag, msg,tr);
        }
    }

    public static void w(String tag,String msg){
        if(hasW){
            Log.w(tag,msg);
        }
    }
    public static void w(String tag,Throwable tr){
        if(hasW){
            Log.w(tag, tr);
        }
    }
    public static void w(String tag,String msg,Throwable tr){
        if(hasW){
            Log.w(tag, msg,tr);
        }
    }

    public static void e(String tag,String msg){
        if(hasE){
            Log.e(tag,msg);
        }
    }
    public static void e(String tag,String msg,Throwable tr){
        if(hasE){
            Log.e(tag, msg,tr);
        }
    }

    public static void wtf(String tag,String msg){
        if(hasWtf){
            Log.wtf(tag, msg);
        }
    }
    public static void wtf(String tag,Throwable tr){
        if(hasWtf){
            Log.wtf(tag, tr);
        }
    }
    public static void wtf(String tag,String msg,Throwable tr){
        if(hasWtf){
            Log.wtf(tag, msg, tr);
        }
    }
}
