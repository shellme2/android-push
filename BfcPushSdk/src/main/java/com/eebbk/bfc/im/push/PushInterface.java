package com.eebbk.bfc.im.push;


import android.content.Context;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.bean.SyncRegistInfo;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.listener.OnStopResumeListener;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.AsyncExecutorUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PushInterface {
    private static volatile SyncApplication app;

    private static boolean debug = true;

    private static ReentrantLock appLock = new ReentrantLock();

    private static Condition appCondition = appLock.newCondition();

    private static volatile boolean isWaiting = false;

    /**
     * sdk初始化
     */
    static void initRun(Context context, OnInitSateListener onInitSateListener) {
        if(context==null){
            throw new RuntimeException("初始化中上下文对象不能为空，请检查初始化参数！！！");
        }
        Context appContext = context.getApplicationContext();

        String curProcessName = AppUtil.getCurProcessName(appContext);
        if (!curProcessName.equals(appContext.getPackageName())) {
            LogUtils.e("multi process init return!curProcessName:" + curProcessName + ",pkgName:" + appContext.getPackageName());
            return;
        }
        LogUtils.i("init process:" + curProcessName + ",pkgName:" + appContext.getPackageName());

        if (app == null) {
            SyncApplication.initInstance(appContext);
            app = SyncApplication.getInstance();
        } else {
            if (!app.isClosed()) {
                app.exit();
            }
        }
        doInit(app, onInitSateListener);
    }

    private static void doInit(SyncApplication app,OnInitSateListener onInitSateListener) {
        signalAll();
        app.init(onInitSateListener);
    }

    private static void waiting() throws InterruptedException {
        appLock.lock();
        isWaiting = true;
        LogUtils.d("waiting for sync app init...");
        try {
            appCondition.await();
            LogUtils.d("sync app waiting was signal...");
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
        LogUtils.d("sync app init successfully,signalAll...");
    }


    private static void send(RequestEntity entity, OnReceiveListener onReceiveListener, boolean isNeedResp, boolean isNeedRetry) {
        final Request request = Request.createRequest(app, entity);
        request.setNeedResponse(isNeedResp);
        request.setNeedRetry(isNeedRetry);
        request.setOnReceiveListener(onReceiveListener);
        request.send();
    }

    private static void getRegistIdSafely(final OnGetCallBack<Long> onGetCallBack) {
        app.getSyncRegistInfoSafely(new OnGetCallBack<SyncRegistInfo>() {
            @Override
            public void onGet(SyncRegistInfo syncRegistInfo) {
                if (onGetCallBack == null) {
                    throw new NullPointerException("OnGetCallBack is null!");
                }
                onGetCallBack.onGet(syncRegistInfo.getRegistId());
            }
        });
    }

    /**
     * 推送同步触发
     */
    static void sendPushSyncTriggerRun(final OnReceiveListener onReceiveListener) {
        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        app.sendPushSyncTrigger(onReceiveListener);
                    } catch (InterruptedException e) {
                        LogUtils.e(e);
                    }
                }
            });
        } else {
            app.sendPushSyncTrigger(onReceiveListener);
        }
    }

    /**
     * sdk设置debug模式，会打log信息
     */
    static void setDebugModeRun(boolean debug) {
        PushInterface.debug = debug;
        LogUtils.setDebugMode(debug); // 设置app进程的debug模式
    }

    static void setDebugModeRun(boolean debug,PushConfig config) {
        PushInterface.debug = debug;
        LogUtils.setDebugMode(debug); // 设置app进程的debug模式
        app.setPushConfig(config);
    }

    static String getSDKVersionRun(){
        return app.getSyncSDKVersionName();
    }

    /**
     * 推送别名和标签设置
     */
    static void setAliasAndTagsRun(final String alias, final List<String> tags, final OnAliasAndTagsListener onAliasAndTagsListener) {
        if(!checkAliasAndTags(alias,tags,onAliasAndTagsListener)){
            return;
        }

        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        app.setAliasAndTagRequest(alias,tags,onAliasAndTagsListener);
                    } catch (InterruptedException e) {
                        LogUtils.e(e);
                    }
                }
            });
        } else {
            app.setAliasAndTagRequest(alias,tags,onAliasAndTagsListener);
        }
    }

    static void setAliasRun(String alias, OnAliasAndTagsListener onAliasAndTagsListener){
        if(TextUtils.isEmpty(alias)){
            throw new RuntimeException("设置别名不能为空！请检查参数设置！");
        }
        setAliasAndTagsRun(alias,null,onAliasAndTagsListener);
    }

    static void setTagsRun(List<String> tags, OnAliasAndTagsListener onAliasAndTagsListener){
        if(tags==null||tags.isEmpty()){
            onAliasAndTagsListener.onFail(null,null,"设置别名不能为空！请检查参数设置！");
//            throw new RuntimeException("设置别名不能为空！请检查参数设置！");
        }
        setAliasAndTagsRun(null,tags,onAliasAndTagsListener);
    }

    /**
     *检查别名标签是否合法
     */
    private static boolean checkAliasAndTags(String alias, List<String> tags,OnAliasAndTagsListener onAliasAndTagsListener){
        if(!TextUtils.isEmpty(alias)){
            if(alias.length()>40){
                throw new RuntimeException("别名设置不合法，字符长度不能超过40，请检查别名设置！！！");
            }
            if(!alias.matches("^[a-zA_Z0-9_]+$")){
                throw new RuntimeException("别名设置不合法，别名由数字、26个英文字母或者下划线组成，请检查别名设置！！！");
            }
        }

        if(tags!=null&&!tags.isEmpty()){
            for (String tag:tags) {
                if(tag.length()>40){
                    onAliasAndTagsListener.onFail(null,null,"标签设置不合法，字符长度不能超过40，请检查别名设置！！！");
                    return false;
//                    throw new RuntimeException("标签设置不合法，字符长度不能超过45，请检查别名设置！！！");
                }
                if(!tag.matches("^[a-zA_Z0-9_]+$")){
                    onAliasAndTagsListener.onFail(null,null,"标签设置不合法，标签由数字、26个英文字母或者下划线组成，请检查别名设置！！！");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取sdk全局环境，预留接口此接口在sdk还没初始化好的时候会开一个线程进行等待
     */
    public static void getSyncApplicationSafely(final OnGetCallBack<SyncApplication> onGetCallBack) {

        // TODO: 2016/10/10 这里得斟酌一下，会影响应用没启动时接收数据 
        
        if (onGetCallBack == null) {
            throw new NullPointerException("onGetCallBack is null.");
        }
        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        onGetCallBack.onGet(app);
                    } catch (InterruptedException e) {
                        LogUtils.e(e);
                    }
                }
            });
        } else {
            onGetCallBack.onGet(app);
        }
    }

    static void stopPushRun(final OnStopResumeListener onStopResumeListener){
        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        app.setStopPush(onStopResumeListener);
                    } catch (InterruptedException e) {
                        LogUtils.e(e);
                    }
                }
            });
        } else {
            app.setStopPush(onStopResumeListener);
        }
    }

    static void resumePushRun(final OnStopResumeListener onStopResumeListener){
        if (app == null) {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        app.setResumePush(onStopResumeListener);
                    } catch (InterruptedException e) {
                        LogUtils.e(e);
                    }
                }
            });
        } else {
            app.setResumePush(onStopResumeListener);
        }
    }

    static boolean isStopPushRun(){
        return StoreUtil.readIsStopPush(new PhoneStore(app.getContext()));
    }
}
