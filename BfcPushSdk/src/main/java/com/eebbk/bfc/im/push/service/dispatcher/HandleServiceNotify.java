package com.eebbk.bfc.im.push.service.dispatcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.eebbk.bfc.im.push.bean.AppPushInfo;
import com.eebbk.bfc.im.push.bean.PandaAppInfo;
import com.eebbk.bfc.im.push.bean.PushCollectInfo;
import com.eebbk.bfc.im.push.communication.MessageHandleService;
import com.eebbk.bfc.im.push.communication.PushConnStatusChangedHandleService;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.panda.PandaAppManager;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.heartbeat.SyncHandleService;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.IntentUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandleServiceNotify {

    private static final String TAG = "HandleServiceNotify";


    /**
     * Class: HandleServiceNotify
     * Tag: 宿主选择
     * Ref: ConnectionService.deliverResponseEntity()
     * Fun: 消息（非心跳响应）通知到各个App >>> MessageHandleService
     */
    public static void notifyMessageHandleService(Context hostContext, ResponseEntity responseEntity, List<Context> contextList) {
        if (contextList.size() == 0) {
            LogUtils.w(TAG,"no find the right context:" + responseEntity);
            return;
        }
        List<Context> contextListCopy = new ArrayList<>(contextList);
        for (Context targetContext : contextListCopy) {
            if (targetContext == null) {
                continue;
            }
            byte[] data = responseEntity.toByteArray();
            Intent intent = IntentUtil.createIntent(hostContext, SyncAction.READ_DATA_ACTION);
            intent.setComponent(new ComponentName(targetContext, MessageHandleService.class.getName()));
            intent.putExtra("data", data);
            if (startService(hostContext, intent) == null) {
                LogUtils.e( TAG, "notify service " + MessageHandleService.class.getName() + " fail,pkgName:" + hostContext.getPackageName());
            } else {
                LogUtils.i("receive data and notify " + MessageHandleService.class.getSimpleName() + ",pkgName:" + hostContext.getPackageName()
                        + ",action:" + intent.getAction() + ",data.length:" + (data == null ? 0 : data.length));
            }

        }
    }

    /**
     * Class: HandleServiceNotify
     * Tag: 宿主选择
     * Ref: ConnectionService.tcpConnection.setOnConnectListener.onConnected()
     * Fun: 当Tcp的Socket连接成功后通知到各个已绑定的App >>> PushConnStatusChangedHandleService
     */
    public static void notifyConnectedHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap
            , String hostname, int port) {
        Intent connectedHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.SYNC_CONNECTED_ACTION);
        Map<String, AppPushInfo> bindPkgNameMapCopy = new HashMap<>(bindPkgNameMap);
        for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMapCopy.entrySet()) {
            String pkgName = entry.getKey();
            if (AppUtil.isAppActive(context, pkgName)) {
                connectedHandleServiceIntent.setComponent(new ComponentName(pkgName, PushConnStatusChangedHandleService.class.getName()));
                connectedHandleServiceIntent.putExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG, context.getPackageName());
                connectedHandleServiceIntent.putExtra(ConnectionService.HOSTNAME_TAG, hostname);
                connectedHandleServiceIntent.putExtra(ConnectionService.PORT_TAG, port);
                if (startService(context, connectedHandleServiceIntent) == null) {
                    LogUtils.e( TAG, "notify connected " + PushConnStatusChangedHandleService.class.getName() + " failed,pkgName:" + pkgName);
                    Da.record(context, new DaInfo().setFunctionName(Da.trigValue.CONNECTED)
                            .setTrigValue("notify connected " + PushConnStatusChangedHandleService.class.getName() + " failed,pkgName:" + pkgName));
                } else {
                    LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"Notify Connected","notify connected to app " + PushConnStatusChangedHandleService.class.getSimpleName() + "successfully,pkgName:" + pkgName);
                    Da.record(context, new DaInfo().setFunctionName(Da.trigValue.CONNECTED)
                            .setTrigValue("Notify Connected===>>>" + "notify connected to app " + PushConnStatusChangedHandleService.class.getSimpleName() + "successfully,pkgName:" + pkgName));
                }
            } else {
                LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when connected on im push.");
                Da.record(context, new DaInfo().setFunctionName(Da.trigValue.CONNECTED)
                        .setTrigValue("pkgName:" + pkgName + " is stopped,do not wakeup when connected on im push."));
            }
        }

        List<PandaAppInfo> pandaList = PandaAppManager.getInstance().getPandaApps();
        for (PandaAppInfo pandaApp : pandaList) {
            String appName = pandaApp.getPackageName();
            if(!bindPkgNameMapCopy.containsKey(appName)){
                if (AppUtil.isAppActive(context, appName)) {
                    LogUtils.e( TAG, "pandaApp notify panda app " + appName + " sync connected");
                    connectedHandleServiceIntent.setComponent(new ComponentName(appName, PushConnStatusChangedHandleService.class.getName()));
                    connectedHandleServiceIntent.putExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG, context.getPackageName());
                    connectedHandleServiceIntent.putExtra(ConnectionService.HOSTNAME_TAG, hostname);
                    connectedHandleServiceIntent.putExtra(ConnectionService.PORT_TAG, port);
                    if (startService(context, connectedHandleServiceIntent) == null) {
                        LogUtils.e( TAG, "pandaApp notify connected " + PushConnStatusChangedHandleService.class.getName() + " failed,pkgName:" + appName);
                        Da.record(context, new DaInfo().setFunctionName(Da.trigValue.CONNECTED)
                                .setTrigValue("pandaApp notify connected " + PushConnStatusChangedHandleService.class.getName() + " failed,pkgName:" + appName));
                    } else {
                        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"pandaApp Notify Connected","notify connected to app " + PushConnStatusChangedHandleService.class.getSimpleName() + "successfully,pkgName:" + appName);
                        Da.record(context, new DaInfo().setFunctionName(Da.trigValue.CONNECTED)
                                .setTrigValue("pandaApp Notify Connected===>>>" + "notify connected to app " + PushConnStatusChangedHandleService.class.getSimpleName() + "successfully,pkgName:" + appName));
                    }
                } else {
                    LogUtils.d("pandaApp pkgName:" + appName + " is stopped,do not wakeup when connected on im push.");
                    Da.record(context, new DaInfo().setFunctionName(Da.trigValue.CONNECTED)
                            .setTrigValue("pandaApp pkgName:" + appName + " is stopped,do not wakeup when connected on im push."));
                }
            }
        }
    }
    /**
     * Class: HandleServiceNotify
     * Tag: 宿主选择
     * Ref: ConnectionService.tcpConnection.setOnConnectListener.onStartConnect()
     * Fun: 当Tcp的Socket开始连接时 通知到各个已绑定的App >>> PushConnStatusChangedHandleService
     */
    public static void notifyStartConnectHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap
            , String hostname, int port) {
        Intent startConnectHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.SYNC_CONNECTING_ACTION);
        Map<String, AppPushInfo> bindPkgNameMapCopy = new HashMap<>(bindPkgNameMap);
        for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMapCopy.entrySet()) {
            String pkgName = entry.getKey();
            if (AppUtil.isAppActive(context, pkgName)) {
                startConnectHandleServiceIntent.setComponent(new ComponentName(pkgName, PushConnStatusChangedHandleService.class.getName()));
                startConnectHandleServiceIntent.putExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG, context.getPackageName());
                startConnectHandleServiceIntent.putExtra(ConnectionService.HOSTNAME_TAG, hostname);
                startConnectHandleServiceIntent.putExtra(ConnectionService.PORT_TAG, port);
                if (startService(context, startConnectHandleServiceIntent) == null) {
                    LogUtils.e( TAG, "notify connected " + PushConnStatusChangedHandleService.class.getName() + " failed,pkgName:" + pkgName);
                } else {
                    LogUtils.i("notify connected " + PushConnStatusChangedHandleService.class.getSimpleName() + "successfully,pkgName:" + pkgName);
                }
            } else {
                LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when connected on im push.");
            }
        }
    }

    /**
     * Class: HandleServiceNotify
     * Tag: 宿主选择
     * Ref: ConnectionService.tcpConnection.setOnConnectListener.onDisconnected()
     * Fun: 当Tcp的Socket连接中断时 通知到各个已绑定的App >>> PushConnStatusChangedHandleService
     */
    public static void notifyDisconnectedHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap) {
        Intent dicConnectedHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.SYNC_DISCONNECTED_ACTION);
        Map<String, AppPushInfo> bindPkgNameMapCopy = new HashMap<>(bindPkgNameMap);
        try {
            // app 异常停止时，会报java.lang.SecurityException: Unable to start service Intent xxxx: process is bad
            // 没有找到好的解决方案，先try catch
            for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMapCopy.entrySet()) {
                String pkgName = entry.getKey();
                if (AppUtil.isAppActive(context, pkgName)) {
                    dicConnectedHandleServiceIntent.setComponent(new ComponentName(pkgName, PushConnStatusChangedHandleService.class.getName()));
                    dicConnectedHandleServiceIntent.putExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG, context.getPackageName());
                    if (startService(context, dicConnectedHandleServiceIntent) == null) {
                        LogUtils.e( TAG, "notify disconnected " + PushConnStatusChangedHandleService.class.getSimpleName() + " failed,pkgName:" + pkgName);
                    } else {
                        LogUtils.i("notify disconnected " + PushConnStatusChangedHandleService.class.getSimpleName() + " successfully,pkgName:" + pkgName);
                    }
                } else {
                    LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when disconnected on im push.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Class: HandleServiceNotify
     * Tag: 宿主选择
     * Ref: ConnectionService.tcpConnection.setOnConnectListener.onFailed()
     * Fun: 当Tcp的Socket连接失败时 通知到各个已绑定的App >>> PushConnStatusChangedHandleService
     */
    public static void notifyConnectFailedHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap
            , String errorMsg, String hostname, int port) {
        Intent connectFailedHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.SYNC_CONNECT_FAIL_ACTION);
        Map<String, AppPushInfo> bindPkgNameMapCopy = new HashMap<>(bindPkgNameMap);
        for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMapCopy.entrySet()) {
            String pkgName = entry.getKey();
            if (AppUtil.isAppActive(context, pkgName)) {
                connectFailedHandleServiceIntent.setComponent(new ComponentName(pkgName, PushConnStatusChangedHandleService.class.getName()));
                connectFailedHandleServiceIntent.putExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG, context.getPackageName());
                connectFailedHandleServiceIntent.putExtra(ConnectionService.CONNECT_ERROR_MSG_TAG, errorMsg);
                connectFailedHandleServiceIntent.putExtra(ConnectionService.HOSTNAME_TAG, hostname);
                connectFailedHandleServiceIntent.putExtra(ConnectionService.PORT_TAG, port);
                if (startService(context, connectFailedHandleServiceIntent) == null) {
                    LogUtils.e( TAG, "notify sync push connect failed " + PushConnStatusChangedHandleService.class.getSimpleName() + " failed,pkgName:" + pkgName);
                } else {
                    LogUtils.i("notify sync push connect failed " + PushConnStatusChangedHandleService.class.getSimpleName() + " successfully,pkgName:" + pkgName);
                }
            } else {
                LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when connect failed on im push.");
            }
        }
    }

    /**
     * Class: HandleServiceNotify
     * Tag: 宿主选择
     * Ref: ConnectionService.handleLoginResponse()
     * Fun: 当登录成功时 通知到各个已绑定的App >>> SyncHandleService
     */
    public static void notifyOnLoginHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap, long registerId) {
        Intent onLoginHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.PUSH_LOGIN_ACTION);
        Map<String, AppPushInfo> bindPkgNameMapCopy = new HashMap<>(bindPkgNameMap);
        for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMapCopy.entrySet()) {
            String pkgName = entry.getKey();
            if (AppUtil.isAppActive(context, pkgName)) {
                onLoginHandleServiceIntent.setComponent(new ComponentName(pkgName, SyncHandleService.class.getName()));
                onLoginHandleServiceIntent.putExtra(ConnectionService.REGISTER_TAG, registerId);
                if (startService(context, onLoginHandleServiceIntent) == null) {
                    LogUtils.e( TAG, "notify on login " + SyncHandleService.class.getSimpleName() + " failed,pkgName:" + pkgName);
                } else {
                    LogUtils.i("notify on login " + SyncHandleService.class.getSimpleName() + " successfully,pkgName:" + pkgName);
                }
            } else {
                LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when login on im push.");
            }
        }
    }

    /**
     * 通知后实际没有做什么 就先不看了
     */
    public static void notifyOnPushCollectHandleService(Context context, PushCollectInfo pushCollectInfo) {
        String pkgName = context.getPackageName();
        Intent onPushCollectHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.PUSH_INFO_COLLECTION);
        onPushCollectHandleServiceIntent.setComponent(new ComponentName(pkgName, SyncHandleService.class.getName()));
        onPushCollectHandleServiceIntent.putExtra(ConnectionService.PUSH_INFO_COLLECTION_TAG, pushCollectInfo.toString());
        if (startService(context, onPushCollectHandleServiceIntent) == null) {
            LogUtils.e( TAG, "notify on push collect " + SyncHandleService.class.getSimpleName() + " failed,pkgName:" + pkgName);
        } else {
            LogUtils.i("notify on push collect " + SyncHandleService.class.getSimpleName() + " successfully,pkgName:" + pkgName);
        }
    }

    private static ComponentName startService(Context context, Intent intent){
        try {
            return context.startService(intent);
        } catch (Exception e) {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE," startService error !!! ", e);
        }
        return null;
    }

}
