package com.eebbk.bfc.im.push;


import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.eebbk.bfc.common.file.FileUtils;
import com.eebbk.bfc.im.push.config.UrlConfig;
import com.eebbk.bfc.im.push.debug.DebugBasicInfo;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.error.ErrorCode;
import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.listener.OnResultListener;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.AsyncExecutorUtil;
import com.eebbk.bfc.im.push.util.DeviceUtils;
import com.eebbk.bfc.im.push.util.ExecutorsUtils;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.ParameterCheckUtils;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.eebbk.bfc.im.push.config.LogTagConfig.LOG_TAG_FLOW_PUSH_INIT;

public class PushImplements {

    private static final String TAG = "PushImplements";

    private static volatile PushApplication app;

    private static ReentrantLock appLock = new ReentrantLock();

    private static Condition appCondition = appLock.newCondition();

    private static volatile boolean isWaiting = false;

    public static volatile boolean isIniting = false;


    static void setUrlDebug(boolean isDebug) {
        UrlConfig.setDebugMode(isDebug);
    }

    /**
     * sdk初始化
     */
    static void initRun(Context context, final OnInitSateListener onInitSateListener, final OnPushStatusListener onPushStatusListener) {
        LogSDKInfo();
        Da.record(context, new DaInfo().setFunctionName(Da.functionName.INIT).setExtendSdkVersion());
        if(!canInit(context, onInitSateListener, onPushStatusListener)){
            return;
        }
        final Context cxt = context.getApplicationContext();
        ExecutorsUtils.execute(new Runnable() {
            @Override
            public void run() {
                if (isNeedInit(cxt)) {
                    LogUtils.e(LOG_TAG_FLOW_PUSH_INIT, "Step01:  run init next === >>> "+cxt.getPackageName());
                    Da.record(cxt, new DaInfo().setFunctionName(Da.functionName.INIT)
                            .setTrigValue("Step01:  run init next === >>> "+cxt.getPackageName())
                            .setExtendSdkVersion());
                    initApp(cxt);
                    signalAll();
                    app.init(onInitSateListener, onPushStatusListener);
                } else {
                    LogUtils.e(TAG, LOG_TAG_FLOW_PUSH_INIT, "Error: is not need init ,init was stop this time ,please check !!! ");
                }
            }
        });
    }

    private static boolean canInit(Context context, OnInitSateListener onInitSateListener, OnPushStatusListener onPushStatusListener){
        if(TextUtils.equals(DeviceUtils.getMachineId(context), DeviceUtils.INVALID_MACHINE_ID)){
            if(onInitSateListener != null){
                onInitSateListener.onFail("init fail, bfc-push do not support machine id number of " + DeviceUtils.INVALID_MACHINE_ID + "!", ErrorCode.EC_MACHINE_ID_ILLEGAL);
            }
            if(onPushStatusListener != null){
                onPushStatusListener.onPushStatus(OnPushStatusListener.Status.ERROR, ErrorCode.EC_MACHINE_ID_ILLEGAL);
            }
            LogUtils.e(TAG, "init fail, bfc-push do not support machine id number of " + DeviceUtils.INVALID_MACHINE_ID + "!");
            return false;
        }
        try{
            if(!FileUtils.isSpaceEnough(Environment.getExternalStorageDirectory().getAbsolutePath(), 10)){
                if(onInitSateListener != null){
                    onInitSateListener.onFail("bfc-push init fail, sdCard space is not enough!!!", ErrorCode.EC_SDCARD_SPACE_IS_NOT_ENOUGH);
                }
                if(onPushStatusListener != null){
                    onPushStatusListener.onPushStatus(OnPushStatusListener.Status.ERROR, ErrorCode.EC_SDCARD_SPACE_IS_NOT_ENOUGH);
                }
                LogUtils.e(TAG, "bfc-push init fail, sdCard space is not enough!!!");
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    private static void LogSDKInfo() {
        LogUtils.e(TAG, LOG_TAG_FLOW_PUSH_INIT, "SDK: " + SDKVersion.getLibraryName() + " init, version: " +
                SDKVersion.getVersionName() + "  code: " + SDKVersion.getSDKInt() +
                " build: " + SDKVersion.getBuildName());
    }

    private static boolean isNeedInit(Context context) {
        if (context == null) {
            throw new RuntimeException(ErrorCode.EC_INIT_CONTEXT_NULL + ":: init context can't be empty, please check the init parameter！");
        }
        Context appContext = context.getApplicationContext();

        //multi process check
        String curProcessName = AppUtil.getCurProcessName(appContext);
        if (curProcessName == null) {
            LogUtils.ec(TAG, LOG_TAG_FLOW_PUSH_INIT,"current process is null", ErrorCode.EC_PROCESS_NULL);
            return false;
        }
        if (!curProcessName.equals(appContext.getPackageName())) {
            LogUtils.ec(TAG, LOG_TAG_FLOW_PUSH_INIT, "multi process init ,curProcessName:" + curProcessName + ",pkgName:" + appContext.getPackageName(), ErrorCode
                    .EC_PROCESS_MULTI);
            return false;
        }
        LogUtils.d(TAG, "init process:" + curProcessName + ",pkgName:" + appContext.getPackageName());

        if (isIniting) {
            LogUtils.e(TAG, LOG_TAG_FLOW_PUSH_INIT, "is init now,please waiting for finish to re init!");
            return false;
        }

        return true;
    }

    private static void initApp(Context context) {
        if (app == null) {
            PushApplication.initInstance(context.getApplicationContext());
            app = PushApplication.getInstance();
            LogUtils.w(TAG, " push analyze ,debug mode is " + LogUtils.debugMode);
        } else {
            LogUtils.i(TAG, LOG_TAG_FLOW_PUSH_INIT," app is not null ，isIniting"+isIniting);
            LogUtils.i(TAG, LOG_TAG_FLOW_PUSH_INIT," app is not not closed ,the we exit app !!! ");
            app.exit();
        }

    }

    private static void waiting() throws InterruptedException {
        appLock.lock();
        isWaiting = true;
        LogUtils.d(TAG,"waiting for push app init...");
        try {
            appCondition.await();
            LogUtils.d("push app waiting was signal...");
        } finally {
            appLock.unlock();
        }
    }

    private static void signalAll() {
        if (!isWaiting) {
            return;
        }
        appLock.lock();
        try {
            appCondition.signalAll();
            isWaiting = false;
        } finally {
            appLock.unlock();
        }
        LogUtils.d(TAG, LOG_TAG_FLOW_PUSH_INIT,"Step03: push app init successfully,signalAll...");
    }

    /**
     * 推送同步触发
     */
    static void sendPushSyncTriggerRun(final OnResultListener onResultListener) {
        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        app.sendPushSyncTrigger(onResultListener);
                    } catch (InterruptedException e) {
                        onResultListener.onFail(e.getMessage(), ErrorCode.EC_INIT_WAITING);
                        LogUtils.ec(TAG, "sendPushSyncTriggerRun::" + e.getMessage(), ErrorCode.EC_INIT_WAITING);
                    }
                }
            });
        } else {
            app.sendPushSyncTrigger(onResultListener);
        }
    }

    /**
     * sdk设置debug模式，会打log信息
     */
    static void setDebugModeRun(boolean debug) {
        LogUtils.i(TAG, "set debug mode success,mode is ::" + debug);
        LogUtils.setDebugMode(debug); // 设置app进程的debug模式
    }

    static void setTagsRun(final List<String> tags, final OnAliasAndTagsListener onAliasAndTagsListener) {

        ParameterCheckUtils.checkTags(tags);

        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        app.setTagsRequest(tags, onAliasAndTagsListener);
                    } catch (InterruptedException e) {
                        onAliasAndTagsListener.onFail("", tags, e.getMessage(), ErrorCode.EC_INIT_WAITING);
                        LogUtils.ec(TAG, "setTagsRun::" + e.getMessage(), ErrorCode.EC_INIT_WAITING);
                    }
                }
            });
        } else {
            app.setTagsRequest(tags, onAliasAndTagsListener);
        }
    }

    static void stopPushRun(final OnResultListener onResultListener) {
        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        app.setStopPush(onResultListener);
                    } catch (InterruptedException e) {
                        onResultListener.onFail(e.getMessage(), ErrorCode.EC_INIT_WAITING);
                        LogUtils.ec(TAG, "stopPushRun::" + e.getMessage(), ErrorCode.EC_INIT_WAITING);
                    }
                }
            });
        } else {
            app.setStopPush(onResultListener);
        }
    }

    static void resumePushRun(final OnResultListener onResultListener) {
        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        app.setResumePush(onResultListener);
                    } catch (InterruptedException e) {
                        onResultListener.onFail(e.getMessage(), ErrorCode.EC_INIT_WAITING);
                        LogUtils.ec(TAG, "resumePushRun::" + e.getMessage(), ErrorCode.EC_INIT_WAITING);
                    }
                }
            });
        } else {
            app.setResumePush(onResultListener);
        }
    }

    static boolean isStopPushRun() {
        return StoreUtil.readIsStopPush(new PhoneStore(app.getContext()));
    }

    /**
     * 获取sdk全局环境，预留接口此接口在sdk还没初始化好的时候会开一个线程进行等待
     */
    public static void getPushApplicationSafely(Context context, final OnGetCallBack<PushApplication> onGetCallBack) {

        if (onGetCallBack == null) {
            LogUtils.e(TAG," onGetCallBack is null !!! ");
            throw new NullPointerException(ErrorCode.EC_GET_PUSH_APPLICATION_SAFE_NO_CALLBACK + " ::getPushApplicationSafely onGetCallBack is null.");
        }
        LogUtils.d(TAG, " =====>>>> " + System.currentTimeMillis());
        if (app == null) {
            LogUtils.e(TAG," app is null !!! ");//add at 0524
            if (context != null) {
                LogUtils.d(TAG," context is not null ,go to init push");
                EebbkPush.init(context, null);
            }

            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogUtils.d(TAG,"thread waiting !!! ");
                        waiting();
                        LogUtils.d(TAG,"thread waiting !!! ");
                        onGetCallBack.onGet(app);
                    } catch (InterruptedException e) {
                        LogUtils.ec(TAG, "getPushApplicationSafely::" + e.getMessage(), ErrorCode.EC_INIT_WAITING);
                    }
                }
            });
        } else {
            LogUtils.e(TAG," app is not null !!! ");
            onGetCallBack.onGet(app);
        }
    }

    public static String analyzePush() {
        return app.analyzePush();
    }

    public static DebugBasicInfo getDebugBasicInfo() {
        return app.getBasicInfo();
    }

    public static void release() {
        if (app != null) {
            app.release();
        }
    }

    static void setOnPushStatusListener(final OnPushStatusListener onPushStatusListener) {
        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        app.setOnPushStatusListener(onPushStatusListener);
                    } catch (InterruptedException e) {
                        LogUtils.ec(TAG, "setOnPushStatusListener::" + e.getMessage(), ErrorCode.EC_INIT_WAITING);
                    }
                }
            });
        } else {
            app.setOnPushStatusListener(onPushStatusListener);
        }
    }
}
