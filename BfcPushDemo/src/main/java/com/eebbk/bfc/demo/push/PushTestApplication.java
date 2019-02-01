package com.eebbk.bfc.demo.push;

import android.app.Application;
import android.os.StrictMode;

import com.eebbk.bfc.demo.push.db.DbManager;
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
    private static final boolean DEVELOPER_MODE=false;

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }


        DbManager.initializeInstance(this);

        //默认初始化设置
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


}
