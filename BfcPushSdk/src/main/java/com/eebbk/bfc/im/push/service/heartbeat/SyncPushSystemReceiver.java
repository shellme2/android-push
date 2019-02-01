package com.eebbk.bfc.im.push.service.heartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.task.TaskType;
import com.eebbk.bfc.im.push.service.tcp.ConnectTask;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NetUtil;

import static com.eebbk.bfc.im.push.service.heartbeat.ConnectSwitchService.getHostServicePackageName;
import static com.eebbk.bfc.im.push.service.heartbeat.ConnectSwitchService.turnOffConnectService;
import static com.eebbk.bfc.im.push.service.heartbeat.ConnectSwitchService.turnOnConnectService;

public class SyncPushSystemReceiver extends BroadcastReceiver {

    private static final String TAG = "SyncPushSystemReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e(TAG, "action is null.");
            return;
        }
        LogUtils.i(TAG,LogTagConfig.LOG_TAG_POINT_RECEIVER, " ==== >>>>> SyncPushSystemReceiver action:" + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(SyncAction.RESTART_SERVICE_ACTION)) { // android系统启动广播
            turnOnConnectService(context, 0);
            Da.record(context, new DaInfo().setFunctionName(Da.functionName.BOOT)
                    .setTrigValue(Da.trigValue.BOOT));
            return;
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) { //网络状态变化广播
            onNetStatusChanged(context);
            return;
        }

        String servicePkgName = getHostServicePackageName(context);
        if (servicePkgName == null) {
            LogUtils.e(TAG, "service package name is null !!!");
            return;
        }

        LogUtils.i(TAG, "SyncPushSystemReceiver servicePkgName:" + servicePkgName);

        if (action.equals(Intent.ACTION_SHUTDOWN)) {
            turnOffConnectService(context, 0);
            Da.record(context, new DaInfo().setFunctionName(Da.functionName.BOOT)
                    .setTrigValue(Da.trigValue.SHUTDOWN));
        } else if (action.equals(Intent.ACTION_USER_PRESENT)) { // 屏幕解锁广播
            triggerHeartbeat(context, servicePkgName);
        } else if (TextUtils.equals(intent.getAction(), Intent.ACTION_PACKAGE_REMOVED)) { // APP卸载广播
            String packageName = intent.getData().getSchemeSpecificPart();
            startConnectionService(context, TaskType.APP_REMOVED, packageName);
        }
    }

    private void triggerHeartbeat(Context context, String servicePkgName) {
        if (NetUtil.isConnectToNet(context)) {
            Intent intent = new Intent(SyncAction.HEARTBEAT_REQUEST);
            intent.setClassName(servicePkgName, HeartBeatReceiver.class.getName());
            intent.putExtra(HeartBeatReceiver.REDUNDANCY, true);
            context.sendBroadcast(intent);
        }
    }

    private void onNetStatusChanged(Context context) {
        if (NetUtil.isConnectToNet(context) && !NetUtil.isDianXinAnd2GNet(context)) {
            ConnectTask.resetRetryTime();
            turnOnConnectService(context, 0);
            Da.record(context, new DaInfo().setFunctionName(Da.functionName.NET_STATUS_CHANGED)
                    .setTrigValue(Da.trigValue.CONNECTED));
        } else {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_ERROR_NET,"network is unreachable! maybe is 2g,isDianXinAnd2GNet="+NetUtil.isDianXinAnd2GNet(context));
            turnOffConnectService(context, 0);
            Da.record(context, new DaInfo().setFunctionName(Da.functionName.NET_STATUS_CHANGED)
                    .setTrigValue(Da.trigValue.DISCONNECT));
        }
    }

    private void startConnectionService(Context context, int taskType, String pkgName) {

        Intent serviceIntent = ConnectionService.createServiceIntent(context, context.getPackageName());
        serviceIntent.putExtra(TaskType.TAG, taskType);
        serviceIntent.putExtra("package_name", pkgName);
        context.startService(serviceIntent);
    }
}
