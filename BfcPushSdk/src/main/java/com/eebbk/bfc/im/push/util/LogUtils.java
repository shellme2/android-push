package com.eebbk.bfc.im.push.util;

import android.os.Environment;

import com.eebbk.bfc.bfclog.BfcLog;

public class LogUtils {

    public static  boolean debugMode = false;
    //构造函数私有，防止恶意新建
    private LogUtils() {
    }

    private static BfcLog bfcLog;


    private static final String TAG = "com.eebbk.bfc.im.push";

    private static final String LOG_SAVE_PATH = Environment.getExternalStorageDirectory() + "/bfclog/push.log";

    static {
        bfcLog = new BfcLog.Builder().tag(TAG).showLog(true).logLevel(BfcLog.ERROR).methodCount(0).build();
    }

    public static void setDebugMode(boolean isDebug) {
        debugMode = isDebug;
        if (isDebug) {
            bfcLog = new BfcLog.Builder().tag(TAG).showLog(true).methodCount(0).build();
        }else{
            bfcLog = new BfcLog.Builder().tag(TAG).showLog(true).logLevel(BfcLog.ERROR).methodCount(0).build();
        }
    }

    public static void v(String text) {
        bfcLog.v(text);
    }

    public static void d(String text) {
        bfcLog.d(text);
    }

    public static void i(String text) {
        bfcLog.i(text);
    }

    public static void w(String text) {
        bfcLog.w(text);
    }



    public static void e(Throwable throwable) {
        bfcLog.e(throwable);
    }

    public static void ec(String text, String errorCode) {
        bfcLog.e("ErrorCode: " + errorCode + "\nErrorMsg: " + text);
    }

    public static void e(String tag, Throwable throwable) {
        bfcLog.tag(tag).e(throwable);
    }

    public static void test(String text) {
        bfcLog.e(text);
    }

    public static void v(String tag, String msg) {
        bfcLog.tag(tag).v(msg);
    }
    public static void v(String tag,String tag2,String msg) {
        bfcLog.tag(tag).v(tag2+"===>>> "+msg);
    }
    public static void v(String tag,String tag2,String tag3,String msg) {
        bfcLog.tag(tag).v(tag2+"==="+tag3+"===>>> "+msg);
    }
    public static void d(String tag, String msg) {
        bfcLog.tag(tag).d(msg);
    }
    public static void d(String tag,String tag2,String msg) {
        bfcLog.tag(tag).d(tag2+"===>>> "+msg);
    }
    public static void d(String tag,String tag2,String tag3,String msg) {
        bfcLog.tag(tag).d(tag2+"==="+tag3+"===>>> "+msg);
    }
    public static void i(String tag, String msg) {
        bfcLog.tag(tag).i(msg);
    }
    public static void i(String tag,String tag2,String msg) {
        bfcLog.tag(tag).i(tag2+"===>>> "+msg);
    }
    public static void i(String tag,String tag2,String tag3,String msg) {
        bfcLog.tag(tag).i(tag2+"==="+tag3+"===>>> "+msg);
    }
    public static void w(String tag, String msg) {
        bfcLog.tag(tag).w(msg);
    }

    public static void w(String tag,String tag2,String msg) {
        bfcLog.tag(tag).w(tag2+"===>>> "+msg);
    }

    public static void w(String tag,String tag2,String tag3,String msg) {
        bfcLog.tag(tag).w(tag2+"==="+tag3+"===>>> "+msg);
    }

    public static void e(String tag, String msg) {
        bfcLog.tag(tag).e(msg);
    }

    public static void e(String tag,String tag2,String msg) {
        bfcLog.tag(tag).e(tag2+"===>>> "+msg);
    }
    public static void e(String tag,String tag2,String tag3,String msg) {
        bfcLog.tag(tag).e(tag2+"==="+tag3+"===>>> "+msg);
    }
    public static void e(String tag, String msg, Throwable tr) {
        bfcLog.tag(tag).e(tr, msg);
    }
    public static void e(String tag, String tag2,String msg, Throwable tr) {
        bfcLog.tag(tag).e(tr, tag2+"===>>> "+msg);
    }
    public static void e(String tag, String tag2,String tag3,String msg, Throwable tr) {
        bfcLog.tag(tag).e(tr,tag2+"==="+tag3+"===>>> "+msg);
    }

    /**
     * error log with error code
     *
     * @param tag       suggest use class path
     * @param msg       the message of error
     * @param errorCode the corresponding error code
     */
    public static void ec(String tag, String msg, String errorCode) {
        bfcLog.tag(tag).e("ErrorCode: " + errorCode + "\nErrorMsg: " + msg);
    }
    public static void ec(String tag,String tag2, String msg, String errorCode) {
        bfcLog.tag(tag).e("TAG2: "+tag2+"\n ErrorCode: " + errorCode + "\nErrorMsg: " + msg);
    }
    public static void ec(String tag,String tag2,String tag3, String msg, String errorCode) {
        bfcLog.tag(tag).e("TAG2: "+tag2+"==="+tag3+"===\n"+"ErrorCode: " + errorCode + "\nErrorMsg: " + msg);
    }

}
