package com.eebbk.bfc.im.push.service.heartbeat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.host.HostServiceInfo;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.service.task.TaskType;
import com.eebbk.bfc.im.push.util.NetUtil;

import java.util.List;

public class SyncPushSystemReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e("action is null.");
            return;
        }

        LogUtils.i("SyncPushSystemReceiver RootReceiver action:" + action);

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)||
                action.equals(SyncAction.RESTART_SERVICE_ACTION)) { // android系统启动广播
            pullUpApp(context);
            return;
        }

        HostServiceInfo hostServiceInfo = new HostServiceInfo();
        ConnectionService.getHostServiceInfo(context, hostServiceInfo);
        String servicePkgName = hostServiceInfo.getServicePkgName();
        if (TextUtils.isEmpty(servicePkgName)) {
            return;
        }

        LogUtils.i("SyncPushSystemReceiver servicePkgName:" + servicePkgName);

        if (action.equals(Intent.ACTION_SHUTDOWN)) {
            onDeviceShutdown(context, servicePkgName);
        } else if (action.equals(Intent.ACTION_USER_PRESENT)) { // 屏幕解锁广播
            triggerHeartbeat(context, servicePkgName);
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) { //网络状态变化广播
            onNetStatusChanged(context, servicePkgName);
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

    private void onDeviceShutdown(Context context, String servicePkgName) {
        Intent onDeviceShutdownIntent = new Intent(SyncAction.DEVICE_SHUTDOWN_ACTION);
        onDeviceShutdownIntent.setClassName(servicePkgName, SyncHandleService.class.getName());
        context.startService(onDeviceShutdownIntent);
    }

    private void onNetStatusChanged(Context context, String servicePkgName) {
        if (NetUtil.isConnectToNet(context)) {
            Intent onNetConnectedIntent = new Intent(SyncAction.NETWORK_CONNECTED_ACTION);
            onNetConnectedIntent.setClassName(servicePkgName, SyncHandleService.class.getName());
            ComponentName componentName = context.startService(onNetConnectedIntent);
            if (componentName != null) {
                LogUtils.i("start service successfully,servicePkgName:" + servicePkgName + ",name:" + SyncHandleService.class.getSimpleName());
            } else {
                LogUtils.e("start service failed,servicePkgName:" + servicePkgName + ",name:" + SyncHandleService.class.getSimpleName());
            }
        } else {
            LogUtils.e("network is unreachable!");
        }
    }

    /**
     * 拉起宿主app
     */
    private void pullUpApp(Context context) {
        // TODO: 2016/10/7 处理拉取操作
        Intent pullUpAppIntent = new Intent(SyncAction.START_ACTION);
        pullUpAppIntent.setPackage(context.getPackageName());
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentServices(pullUpAppIntent, PackageManager.GET_INTENT_FILTERS);
        for (ResolveInfo resolveInfo : list) {
            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
            if (serviceInfo != null && serviceInfo.packageName.equals(context.getPackageName())) {
                pullUpAppIntent.setComponent(new ComponentName(context, serviceInfo.name));
                ComponentName componentName = context.startService(pullUpAppIntent);
                if (componentName != null) {
                    LogUtils.i("pull up app successfully,componentName pkgName:" + componentName.getPackageName());
                    break;
                } else {
                    LogUtils.e("pull up app fail,unknown error,maybe your device forbid pulling up other app!");
                }
            }
        }
    }

    private void startConnectionService(Context context, int taskType, String pkgName) {
        Intent serviceIntent = ConnectionService.createIntent(context, context.getPackageName());
        serviceIntent.putExtra(TaskType.TAG, taskType);
        serviceIntent.putExtra("package_name", pkgName);
        context.startService(serviceIntent);
    }
}
