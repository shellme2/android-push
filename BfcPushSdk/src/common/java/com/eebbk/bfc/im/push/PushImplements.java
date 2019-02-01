package com.eebbk.bfc.im.push;


import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.eebbk.bfc.im.push.config.UrlConfig;
import com.eebbk.bfc.im.push.debug.DebugBasicInfo;
import com.eebbk.bfc.im.push.entity.PushType;
import com.eebbk.bfc.im.push.error.ErrorCode;
import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnResultListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.AsyncExecutorUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NetUtil;
import com.eebbk.bfc.im.push.util.ParameterCheckUtils;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;
import com.huawei.android.pushagent.PushBootReceiver;
import com.huawei.android.pushagent.PushEventReceiver;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.api.PushManager;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.push.service.XMJobService;
import com.xiaomi.push.service.XMPushService;
import com.xiaomi.push.service.receivers.PingReceiver;

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
    static void initRun(Context context, OnInitSateListener onInitSateListener, OnPushStatusListener onPushStatusListener) {
        if (isNeedInit(context)) {
            LogUtils.i(TAG, LOG_TAG_FLOW_PUSH_INIT, "Step01:  run init next === >>>");
            initApp(context);

            signalAll();

            app.init(onInitSateListener, onPushStatusListener);

            startThirdPush(context);

            LogSDKInfo();
        }else {
            LogUtils.e(TAG, LOG_TAG_FLOW_PUSH_INIT, "Error: is not need init ,init was stop this time ,please check !!! ");
        }
    }

    private static void LogSDKInfo() {
        LogUtils.i(TAG, LOG_TAG_FLOW_PUSH_INIT," " + SDKVersion.getLibraryName() + " init, version: " +
                SDKVersion.getVersionName() + "  code: " + SDKVersion.getSDKInt() +
                " build: " + SDKVersion.getBuildName());
    }

    private static void initApp(Context context) {
        if (app == null) {
            PushApplication.initInstance(context.getApplicationContext());
            app = PushApplication.getInstance();
        } else {
            if (!app.isClosed()) {
                app.exit();
            }
        }
    }

    private static boolean isNeedInit(Context context) {
        if (context == null) {
            throw new RuntimeException(ErrorCode.EC_INIT_CONTEXT_NULL + ":: init context can't be empty, please check the init parameter！");
        }
        Context appContext = context.getApplicationContext();

        //multi process check  多个进程会让application多次初始化，防止推送多次初始化
        String curProcessName = AppUtil.getCurProcessName(appContext);
        if (curProcessName == null) {
            LogUtils.ec(TAG,  LOG_TAG_FLOW_PUSH_INIT,"current process is null", ErrorCode.EC_PROCESS_NULL);
            return false;
        }
        if (!curProcessName.equals(appContext.getPackageName())) {
            //如果当前进程不再主进程上，则不初始化,返回false
            LogUtils.ec(TAG,  LOG_TAG_FLOW_PUSH_INIT,"multi process init ,curProcessName:" + curProcessName + ",pkgName:" + appContext.getPackageName(), ErrorCode
                    .EC_PROCESS_MULTI);
            return false;
        }
        LogUtils.i(TAG, LOG_TAG_FLOW_PUSH_INIT, "init process:" + curProcessName + ",pkgName:" + appContext.getPackageName());

        if (!NetUtil.isConnectToNet(context)) {
            LogUtils.e(TAG, "the net is not connect !!!");
            return false;
        }

        if (isIniting) {
            LogUtils.w(TAG, "is init now,please waiting for finish to re init!");
            return false;
        }
        return true;
    }

    private static void waiting() throws InterruptedException {
        appLock.lock();
        isWaiting = true;
        LogUtils.d("waiting for push app init...");
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
        LogUtils.d(TAG, LOG_TAG_FLOW_PUSH_INIT,"push app init successfully,signalAll...");
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

        // TODO: 2016/10/10 这里得斟酌一下，会影响应用没启动时接收数据

        if (onGetCallBack == null) {
            throw new NullPointerException(ErrorCode.EC_GET_PUSH_APPLICATION_SAFE_NO_CALLBACK + " ::getPushApplicationSafely onGetCallBack is null.");
        }
        if (app == null) {
            if (context != null) {
                EebbkPush.init(context, null);
            }

            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    try {
                        waiting();
                        onGetCallBack.onGet(app);
                    } catch (InterruptedException e) {
                        LogUtils.ec(TAG, "getPushApplicationSafely::" + e.getMessage(), ErrorCode.EC_INIT_WAITING);
                    }
                }
            });
        } else {
            onGetCallBack.onGet(app);
        }
    }


    /**
     * 启动第三方推送，目前是华为，小米，可根据手机厂商来选择对应的推送
     */
    public static void startThirdPush(Context context) {
        String curProcessName = AppUtil.getCurProcessName(context);
        if (!curProcessName.equals(context.getPackageName())) {
            return;
        }
        LogUtils.i("init process:" + curProcessName + ",pkgName:" + context.getPackageName());
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equalsIgnoreCase(PushType.HUAWEI_PUSH_TAG)) {
            LogUtils.d("INIT huawei PUSH...");
            disableXiaoMiPush(context);
            PushManager.requestToken(context); // 华为推送获取token
            PushManager.enableReceiveNormalMsg(context, true);
        } else if (manufacturer.equalsIgnoreCase(PushType.XIAOMI_PUSH_TAG)) {
            LogUtils.d("INIT xiaomi PUSH...");
            disableHuaWeiPush(context);

            String appId = AppUtil.getMiAppId(context);
            String appKey = AppUtil.getMiAppKey(context);
//            String appId = "2882303761517533076";
//            String appKey = "5881753311076";

            MiPushClient.registerPush(context, appId, appKey);
            LogUtils.i("xiaomi appId:" + appId);
            LogUtils.i("xiaomi appKey:" + appKey);
            LogUtils.i("xiaomi registerId:" + MiPushClient.getRegId(context));
        }
    }

    /**
     * 禁止使用第三方推送服务
     *
     * @param context
     */
    public static void stopThirdPush(Context context) {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equalsIgnoreCase(PushType.HUAWEI_PUSH_TAG)) {
            LogUtils.d("STOP HUAWEI PUSH...");
            PushManager.enableReceiveNormalMsg(context, false);
        } else if (manufacturer.equalsIgnoreCase(PushType.XIAOMI_PUSH_TAG)) {
            LogUtils.d("STOP XIAOMI PUSH...");
            MiPushClient.unregisterPush(context);
        }
    }

    private static void disableHuaWeiPush(Context context) {
        ComponentName pushEventReceiver = new ComponentName(context, PushEventReceiver.class.getName());
        ComponentName pushBootReceiver = new ComponentName(context, PushBootReceiver.class.getName());
        ComponentName huaweiPushService = new ComponentName(context, PushService.class.getName());
        PackageManager pm = context.getPackageManager();
        // 禁用华为推送的 manifest 配置
        pm.setComponentEnabledSetting(pushEventReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(pushBootReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(huaweiPushService, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private static void disableXiaoMiPush(Context context) {
        ComponentName xmPushService = new ComponentName(context, XMPushService.class.getName());
        ComponentName xmJobService = new ComponentName(context, XMJobService.class.getName());
        ComponentName pingReceiver = new ComponentName(context, PingReceiver.class.getName());
        PackageManager pm = context.getPackageManager();
        // 禁用华为推送的 manifest 配置
        pm.setComponentEnabledSetting(xmPushService, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(xmJobService, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(pingReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static String analyzePush() {
        return app.analyzePush();
    }

    public static void release() {
        if (app != null) {
            app.release();
            app = null;
        }
    }
    public static DebugBasicInfo getDebugBasicInfo() {
        return app.getBasicInfo();
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
