package com.eebbk.bfc.demo.push_1;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.eebbk.bfc.im.push.EebbkPush;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.util.LogUtils;

/**
 * @author liuyewu
 * Push—demo的application类，用于初始化等操作
 * 2016.09.20
 */
public class PushTestApplication extends Application {

    private static final String TAG=PushTestApplication.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();

        EebbkPush.init(this, new OnInitSateListener() {
            @Override
            public void onSuccess() {
                LogUtils.i(TAG, "sync push init success!!!");
            }

            @Override
            public void onFail(String errorMsg) {
                LogUtils.i(TAG, "sync push init fail msg-->"+errorMsg);
            }
        });
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

}
