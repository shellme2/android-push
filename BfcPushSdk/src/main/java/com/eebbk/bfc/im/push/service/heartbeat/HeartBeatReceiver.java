package com.eebbk.bfc.im.push.service.heartbeat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.heartbeat.heartpackage.HeartbeatManager;
import com.eebbk.bfc.im.push.service.heartbeat.heartpackage.HeartbeatScheduler;
import com.eebbk.bfc.im.push.service.host.HostServiceInfo;
import com.eebbk.bfc.im.push.service.task.TaskType;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NetUtil;

/**
 * 心跳包接收广播
 */
public class HeartBeatReceiver extends BroadcastReceiver {

    public static final String REDUNDANCY = "redundancy";
    private static final String TAG = "HeartBeatReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        HostServiceInfo hostServiceInfo = new HostServiceInfo();
        ConnectionService.getHostServiceInfo(context, hostServiceInfo);
        String servicePkgName = hostServiceInfo.getServicePkgName();
        if (TextUtils.isEmpty(servicePkgName)) {
            return;
        }
        LogUtils.i("servicePkgName:" + servicePkgName);
        if (intent == null) {
            LogUtils.e(TAG,"intent is null , then do nothing !!!");
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e(TAG,"action is null , then do nothing !!!");
            return;
        }

        if (action.equals(SyncAction.HEARTBEAT_REQUEST)) {
            if (NetUtil.isConnectToNet(context) && NetUtil.isDianXinAnd2GNet(context)) {
                LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT,TAG,"The net is DianXin and 2G this time ,so we can not response the heartbeat");
                return;
            }
            LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT,"receive a heartbeat request action:" + action);
            int heartType = intent.getIntExtra(HeartbeatScheduler.HEART_TYPE_TAG, 0);
            HeartbeatManager heartbeatManager = HeartbeatManager.getInstance();
            if (!heartbeatManager.isStarted()) {
                LogUtils.w("heartbeat is canceled!");
            } else {
                if (heartType != HeartbeatScheduler.REDUNDANCY_HEART) {
                    heartbeatManager.startNextHeartbeat(context, heartType);
                }
            }
            startHeartbeatTask(context, servicePkgName, heartType);
        }
    }

    public static void startHeartbeatTask(Context context, String servicePkgName, int heartType) {
        Intent serviceIntent = ConnectionService.createServiceIntent(context, servicePkgName);
        serviceIntent.putExtra(TaskType.TAG, TaskType.HEART_BEAT);
        serviceIntent.putExtra(HeartbeatScheduler.HEART_TYPE_TAG, heartType);
        ComponentName componentName = context.startService(serviceIntent);
        if (componentName != null) {
            LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT,"start heartbeat service successfully,pkgName:" + componentName.getPackageName());
        } else {
            LogUtils.e( TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT,TAG, "start heartbeat service failed,pkgName:" + servicePkgName);
        }
    }

}
