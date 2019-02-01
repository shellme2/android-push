package com.eebbk.bfc.im.push.debug;

import android.os.Environment;

import com.eebbk.bfc.bfclog.BfcLog;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/20 20:14
 * Email:  zengjingfang@foxmail.com
 */
public class DLog {
    public static  boolean debugMode = true;
    //构造函数私有，防止恶意新建
    private DLog() {
    }

    private static BfcLog bfcLog;


    private static final String TAG = "DebugTool >>>";

    private static final String LOG_SAVE_PATH = Environment.getExternalStorageDirectory() + "/bfclog/push.log";

    static {
        bfcLog = new BfcLog.Builder().tag(TAG).showLog(true).showThreadInfo(true).methodCount(1).methodOffset(1).build();
    }

    public static void setDebugMode(boolean isDebug) {
        debugMode = isDebug;
        if (isDebug) {
            bfcLog = new BfcLog.Builder().tag(TAG).showLog(true).showThreadInfo(true).methodCount(1).methodOffset(1).build();
        }else{
            bfcLog = new BfcLog.Builder().tag(TAG).showLog(true).showThreadInfo(true).logLevel(BfcLog.ERROR).methodCount(1).methodOffset(1).build();
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
    public static void e(String text) {
        bfcLog.e(text);
    }

    public static void v(String tag, String msg) {
        bfcLog.tag(tag).v(msg);
    }

    public static void d(String tag, String msg) {
        bfcLog.tag(tag).d(msg);
    }

    public static void i(String tag, String msg) {
        bfcLog.tag(tag).i(msg);
    }

    public static void e(String tag, String msg) {
        bfcLog.tag(tag).e(msg);
    }

}
