package com.eebbk.bfc.demo.push;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.eebbk.bfc.demo.push.db.DbManager;
import com.eebbk.bfc.demo.push.report.BfcReport;
import com.eebbk.bfc.demo.push.util.BlockCanaryConfig;
import com.eebbk.bfc.demo.push.util.ExecutorsUtils;
import com.eebbk.bfc.http.config.BfcHttpConfigure;
import com.eebbk.bfc.im.push.BfcPush;
import com.eebbk.bfc.im.push.bean.SyncMessage;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.sdk.behavior.BehaviorCollector;
import com.github.moduth.blockcanary.BlockCanary;
import com.squareup.leakcanary.LeakCanary;

/**
 * @author liuyewu
 * Push—demo的application类，用于初始化等操作
 * 2016.09.20
 */
public class PushTestApplication extends Application {

    private static final String TAG = "PushTestApplication";
    private static final boolean DEVELOPER_MODE = true;

    public static boolean isInit=false;
    private static Context sContext;
    public static BfcPush sBfcPush;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        LogUtils.d(TAG, "PushTestApplication onCreate-->");
        ExecutorsUtils.execute(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    private void init(){
        if (DEVELOPER_MODE) {
            BfcHttpConfigure.openDebug();
            initStrictMode();
            initLeakCanary();
            initBlockCanary();
        }

        if(!new CheckPermission(this).permissionSet(MainActivity.PERMISSION)){

            LogUtils.i("liuyewu", "PushTestApplication entry init -->");

            DbManager.initializeInstance(this);

            BfcReport.reportToAppImPush(this);
//            TestHearbeatScheduler.HEART = 60000;
//            TestHearbeatScheduler.TIMEOUT = 20000;
            initPush();
        }

        BehaviorCollector.getInstance().init(new BehaviorCollector.Builder(this)
                .enableReport(false)
                .build());
    }

    private void initPush() {
        if (sBfcPush == null) {
            sBfcPush = new BfcPush.Builder().setDebug(true).setUrlMode(BfcPush.Settings.URL_MODE_RELEASE).build();
        }
        sBfcPush.init(getApplicationContext(), new OnInitSateListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "onSuccess: ");
            }

            @Override
            public void onFail(String errorMsg, String errorCode) {
                Log.e(TAG, "Error: " + errorMsg);
            }
        }, new OnPushStatusListener() {
            @Override
            public void onPushStatus(int status, Object... values) {
                LogUtils.i(TAG, "status:" + status);
                if(status == Status.RECEIVE){
                    // 收到推送消息
                    SyncMessage syncMessage = (SyncMessage)values[0];
                    LogUtils.i(TAG, "syncMessage:" + syncMessage.toString());
                }
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtils.d(TAG,"onTerminate");
    }
    private void initBlockCanary() {
        BlockCanary.install(this, new BlockCanaryConfig()).start();
    }

    private void initLeakCanary() {
        // leakcanary默认只监控Activity的内存泄露
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

    }


    private void initStrictMode() {
        //针对线程的监视策略
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        //针对vm
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
    public static Context getAppContext() {
        return sContext;
    }
}
