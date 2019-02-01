package com.eebbk.bfc.im.push.debug.da;

import android.content.Context;

/**
 * @author hesn
 * 2018/8/30
 */
public class LogInfo {
    private String tag;
    private String msg;
    private String pid;
    private String packageName;
    private long time;

    LogInfo(){}

    LogInfo(Context context, DaInfo info){
        tag = info.getFunctionName();
        msg = info.getTrigValue();
        pid = String.valueOf(android.os.Process.myPid());
        packageName = context.getPackageName();
        time = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "LogInfo{" +
                "tag='" + tag + '\'' +
                ", msg='" + msg + '\'' +
                ", pid='" + pid + '\'' +
                ", packageName='" + packageName + '\'' +
                ", time=" + time +
                '}';
    }
}
