package com.eebbk.bfc.im.push.service.dispatcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.eebbk.bfc.im.push.bean.AppPushInfo;
import com.eebbk.bfc.im.push.bean.PushCollectInfo;
import com.eebbk.bfc.im.push.communication.MessageHandleService;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.heartbeat.SyncHandleService;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.service.heartbeat.PushConnStatusChangedHandleService;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.IntentUtil;

import java.util.List;
import java.util.Map;

public class HandleServiceNotify {

    public static void notifyMessageHandleService(Context hostContext, ResponseEntity responseEntity, List<Context> contextList) {
        if (contextList.size() == 0) {
            LogUtils.w("no find the right context:" + responseEntity);
            return;
        }
        for (Context targetContext : contextList) {
            if (targetContext == null) {
                continue;
            }
            byte[] data = responseEntity.toByteArray();
            Intent intent = IntentUtil.createIntent(hostContext, SyncAction.READ_DATA_ACTION);
            intent.setComponent(new ComponentName(targetContext, MessageHandleService.class.getName()));
            intent.putExtra("data", data);
            if (hostContext.startService(intent) == null) {
                LogUtils.e("notify service " + MessageHandleService.class.getName() + " fail,pkgName:" + hostContext.getPackageName());
            } else {
                LogUtils.i("receive data and notify " + MessageHandleService.class.getSimpleName() + ",pkgName:" + hostContext.getPackageName()
                        + ",action:" + intent.getAction() + ",data.length:" + (data == null ? 0 : data.length));
            }
        }
    }

    public static void notifyConnectedHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap
            , String hostname, int port) {
        Intent connectedHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.SYNC_CONNECTED_ACTION);
        for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMap.entrySet()) {
            String pkgName = entry.getKey();
            if (AppUtil.isAppActive(context, pkgName)) {
                connectedHandleServiceIntent.setComponent(new ComponentName(pkgName, PushConnStatusChangedHandleService.class.getName()));
                connectedHandleServiceIntent.putExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG, context.getPackageName());
                connectedHandleServiceIntent.putExtra(ConnectionService.HOSTNAME_TAG, hostname);
                connectedHandleServiceIntent.putExtra(ConnectionService.PORT_TAG, port);
                if (context.startService(connectedHandleServiceIntent) == null) {
                    LogUtils.e("notify connected " + PushConnStatusChangedHandleService.class.getName() + " failed,pkgName:" + pkgName);
                } else {
                    LogUtils.i("notify connected " + PushConnStatusChangedHandleService.class.getSimpleName() + "successfully,pkgName:" + pkgName);
                }
            } else {
                LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when connected on im push.");
            }
        }
    }

    public static void notifyStartConnectHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap
            , String hostname, int port) {
        Intent startConnectHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.SYNC_CONNECTING_ACTION);
        for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMap.entrySet()) {
            String pkgName = entry.getKey();
            if (AppUtil.isAppActive(context, pkgName)) {
                startConnectHandleServiceIntent.setComponent(new ComponentName(pkgName, PushConnStatusChangedHandleService.class.getName()));
                startConnectHandleServiceIntent.putExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG, context.getPackageName());
                startConnectHandleServiceIntent.putExtra(ConnectionService.HOSTNAME_TAG, hostname);
                startConnectHandleServiceIntent.putExtra(ConnectionService.PORT_TAG, port);
                if (context.startService(startConnectHandleServiceIntent) == null) {
                    LogUtils.e("notify connected " + PushConnStatusChangedHandleService.class.getName() + " failed,pkgName:" + pkgName);
                } else {
                    LogUtils.i("notify connected " + PushConnStatusChangedHandleService.class.getSimpleName() + "successfully,pkgName:" + pkgName);
                }
            } else {
                LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when connected on im push.");
            }
        }
    }

    public static void notifyDisconnectedHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap) {
        Intent dicconnectedHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.SYNC_DISCONNECTED_ACTION);
        for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMap.entrySet()) {
            String pkgName = entry.getKey();
            if (AppUtil.isAppActive(context, pkgName)) {
                dicconnectedHandleServiceIntent.setComponent(new ComponentName(pkgName, PushConnStatusChangedHandleService.class.getName()));
                dicconnectedHandleServiceIntent.putExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG, context.getPackageName());
                if (context.startService(dicconnectedHandleServiceIntent) == null) {
                    LogUtils.e("notify disconnected " + PushConnStatusChangedHandleService.class.getSimpleName() + " failed,pkgName:" + pkgName);
                } else {
                    LogUtils.i("notify disconnected " + PushConnStatusChangedHandleService.class.getSimpleName() + " successfully,pkgName:" + pkgName);
                }
            } else {
                LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when disconnected on im push.");
            }
        }
    }

    public static void notifyConnectFailedHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap
            , String errorMsg, String hostname, int port) {
        Intent connectFailedHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.SYNC_CONNECT_FAIL_ACTION);
        for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMap.entrySet()) {
            String pkgName = entry.getKey();
            if (AppUtil.isAppActive(context, pkgName)) {
                connectFailedHandleServiceIntent.setComponent(new ComponentName(pkgName, PushConnStatusChangedHandleService.class.getName()));
                connectFailedHandleServiceIntent.putExtra(ConnectionService.CONNECT_ERROR_MSG_TAG, errorMsg);
                connectFailedHandleServiceIntent.putExtra(ConnectionService.HOSTNAME_TAG, hostname);
                connectFailedHandleServiceIntent.putExtra(ConnectionService.PORT_TAG, port);
                if (context.startService(connectFailedHandleServiceIntent) == null) {
                    LogUtils.e("notify sync push connect failed " + PushConnStatusChangedHandleService.class.getSimpleName() + " failed,pkgName:" + pkgName);
                } else {
                    LogUtils.i("notify sync push connect failed " + PushConnStatusChangedHandleService.class.getSimpleName() + " successfully,pkgName:" + pkgName);
                }
            } else {
                LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when connect failed on im push.");
            }
        }
    }

    public static void notifyOnLoginHandleService(Context context, Map<String, AppPushInfo> bindPkgNameMap, long registerId) {
        Intent onLoginHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.PUSH_LOGIN_ACTION);
        for (Map.Entry<String, AppPushInfo> entry : bindPkgNameMap.entrySet()) {
            String pkgName = entry.getKey();
            if (AppUtil.isAppActive(context, pkgName)) {
                onLoginHandleServiceIntent.setComponent(new ComponentName(pkgName, SyncHandleService.class.getName()));
                onLoginHandleServiceIntent.putExtra(ConnectionService.REGISTER_TAG, registerId);
                if (context.startService(onLoginHandleServiceIntent) == null) {
                    LogUtils.e("notify on login " + SyncHandleService.class.getSimpleName() + " failed,pkgName:" + pkgName);
                } else {
                    LogUtils.i("notify on login " + SyncHandleService.class.getSimpleName() + " successfully,pkgName:" + pkgName);
                }
            } else {
                LogUtils.d("pkgName:" + pkgName + " is stopped,do not wakeup when login on im push.");
            }
        }
    }

    public static void notifyOnPushCollectHandleService(Context context, PushCollectInfo pushCollectInfo) {
        String pkgName = context.getPackageName();
        Intent onPushCollectHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.PUSH_INFO_COLLECTION);
        onPushCollectHandleServiceIntent.setComponent(new ComponentName(pkgName, SyncHandleService.class.getName()));
        onPushCollectHandleServiceIntent.putExtra(ConnectionService.PUSH_INFO_COLLECTION_TAG, pushCollectInfo.toString());
        if (context.startService(onPushCollectHandleServiceIntent) == null) {
            LogUtils.e("notify on push collect " + SyncHandleService.class.getSimpleName() + " failed,pkgName:" + pkgName);
        } else {
            LogUtils.i("notify on push collect " + SyncHandleService.class.getSimpleName() + " successfully,pkgName:" + pkgName);
        }
    }

    public static void notifyKillPushHandleService(Context context, int pid) {
        String pkgName = context.getPackageName();
        Intent killPushHandleServiceIntent = IntentUtil.createIntent(context, SyncAction.KILL_PUSH_PROCESS);
        killPushHandleServiceIntent.setComponent(new ComponentName(pkgName, SyncHandleService.class.getName()));
        killPushHandleServiceIntent.putExtra(ConnectionService.KILL_PUSH_PROCESS_ID_TAG, pid);
        if (context.startService(killPushHandleServiceIntent) == null) {
            LogUtils.e("notify on push collect " + SyncHandleService.class.getSimpleName() + " failed,pkgName:" + pkgName);
        } else {
            LogUtils.i("notify on push collect " + SyncHandleService.class.getSimpleName() + " successfully,pkgName:" + pkgName);
        }
    }
}
