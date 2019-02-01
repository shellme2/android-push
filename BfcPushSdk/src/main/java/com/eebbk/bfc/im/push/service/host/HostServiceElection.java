package com.eebbk.bfc.im.push.service.host;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.communication.ConnectionServiceManager;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.util.SettingStoreUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.heartbeat.HeartBeatReceiver;
import com.eebbk.bfc.im.push.service.heartbeat.SyncPushSystemReceiver;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.TimeFormatUtil;

import java.util.List;

public class HostServiceElection {

    private ConnectionServiceManager connectionServiceManager;
    private String hostServicePackageName ;
    private Context context;

    public HostServiceElection(ConnectionServiceManager connectionServiceManager,Context context) {
        this.connectionServiceManager = connectionServiceManager;
        this.context=context;
        hostServicePackageName= SettingStoreUtil.getHostPackgName(context);
    }

    private String getHostServicePackageName(){
        if(TextUtils.isEmpty(hostServicePackageName)){
            hostServicePackageName= SettingStoreUtil.getHostPackgName(context);
        }
        return hostServicePackageName;
    }

    public void electHostService(Context context) {
        Intent serviceIntent = connectionServiceManager.getServiceIntent();
        ComponentName componentName = serviceIntent.getComponent();
        if (componentName != null) {
            String pkgName = componentName.getPackageName();
            if (pkgName.equals(getHostServicePackageName())) {
                LogUtils.d("the app:" + context.getPackageName() + " has bind to the host app:" + pkgName);
                return;
            }
            connectionServiceManager.shutdown();
            Intent intent = ConnectionService.createIntent(context, getHostServicePackageName());
            connectionServiceManager.setServiceIntent(intent);
            setLocalReceiver(context);
            connectionServiceManager.startConnect(true);
            LogUtils.i("elect the host app:" + serviceIntent.getComponent().getPackageName());
        }
    }

    public void checkUpOwnService(Context context) {
        Intent serviceIntent = connectionServiceManager.getServiceIntent();
        ComponentName componentName = serviceIntent.getComponent();
        if (componentName != null) {
            String pkgName = componentName.getPackageName();
            if (!pkgName.equals(getHostServicePackageName())) {
                if (AppUtil.isAppActive(context, getHostServicePackageName())) {
                    Intent stopIntent = new Intent(SyncAction.PUSH_HOST_SERVICE_CHECK);
                    stopIntent.setClassName(getHostServicePackageName(), HostElectionHandleService.class.getName());
                    context.startService(stopIntent);
                    LogUtils.d("start handle service,action:" + SyncAction.PUSH_HOST_SERVICE_CHECK);
                }
            }
        }
    }

    public void checkUpHostService(Context context) {
        List<ActivityManager.RunningServiceInfo> list = AppUtil.getRunningServiceList(context, ConnectionService.class.getName());
        LogUtils.i("RunningServiceInfo size:" + list.size());
        if (list.size() > 0) {
            printServiceInfo(list);
            setLocalReceiver(context);
            checkUpRunningServiceInfo(context, list);
        } else {
            LogUtils.e("start connection service error!!!");
        }
    }

    /**
     * 检测本身的app是否启动了多余的推送共享service，如果启动了多余的service，并且在手机可成功启动
     * 远程推送共享service的情况下，会将本地的service干掉然后启动远程的service
     *
     * @param list service列表
     */
    public void checkUpRunningServiceInfo(Context context, List<ActivityManager.RunningServiceInfo> list) {
        String localPkgName = context.getPackageName();
        for (ActivityManager.RunningServiceInfo connectionServiceInfo : list) {
            String servicePkgName = connectionServiceInfo.service.getPackageName();
            if (localPkgName.equals(getHostServicePackageName())
                    && !servicePkgName.equals(getHostServicePackageName())) { // 只有宿主app才能发出停止其他寄生app的service的请求
                Intent stopIntent = new Intent(SyncAction.STOP_CONN_SERVICE_ACTION);
                stopIntent.setClassName(servicePkgName, HostElectionHandleService.class.getName());
                context.startService(stopIntent);
                LogUtils.d("start handle service,action:" + SyncAction.STOP_CONN_SERVICE_ACTION);
            }
        }
        LogUtils.d("checkUpRunningServiceInfo finished");
    }

    private void setLocalReceiver(Context context) {
        Intent serviceIntent = connectionServiceManager.getServiceIntent();
        String pkgName = context.getPackageName();
        ComponentName serviceIntentComponent = serviceIntent.getComponent();
        ComponentName heartbeatReceiver = new ComponentName(context, HeartBeatReceiver.class);
        ComponentName syncPushSysReceiver = new ComponentName(context, SyncPushSystemReceiver.class);
        PackageManager pm = context.getPackageManager();
        if (!pkgName.equals(serviceIntentComponent.getPackageName())) {
            // 禁用 manifest receiver
            pm.setComponentEnabledSetting(heartbeatReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(syncPushSysReceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            LogUtils.w("service pkgName:" + serviceIntentComponent.getPackageName() + ",local pkgName:" + pkgName
                    + "," + HeartBeatReceiver.class.getName() + " is disabled...");
            LogUtils.w("service pkgName:" + serviceIntentComponent.getPackageName() + ",local pkgName:" + pkgName
                    + "," + SyncPushSystemReceiver.class.getName() + " is disabled...");
        } else {
            // 启用 manifest receiver
            pm.setComponentEnabledSetting(heartbeatReceiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(syncPushSysReceiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            LogUtils.i("service pkgName:" + serviceIntentComponent.getPackageName() + ",local pkgName:" + pkgName
                    + "," + HeartBeatReceiver.class.getName() + " is enable...");
            LogUtils.i("service pkgName:" + serviceIntentComponent.getPackageName() + ",local pkgName:" + pkgName
                    + "," + SyncPushSystemReceiver.class.getName() + " is enable...");
        }
    }

    private void printServiceInfo(List<ActivityManager.RunningServiceInfo> list) {
        for (ActivityManager.RunningServiceInfo connectionServiceInfo : list) {
            LogUtils.i("printServiceInfo==================================================>>>");
            LogUtils.i("pid:" + connectionServiceInfo.pid);
            LogUtils.i("process:" + connectionServiceInfo.process);
            LogUtils.i("serviceName:" + connectionServiceInfo.service.getClassName());
            LogUtils.i("packageName:" + connectionServiceInfo.service.getPackageName());
            LogUtils.i("activeSince:" + TimeFormatUtil.format(connectionServiceInfo.activeSince));
            LogUtils.i("lastActivityTime:" + TimeFormatUtil.format(connectionServiceInfo.lastActivityTime));
            LogUtils.i("started:" + connectionServiceInfo.started);
            LogUtils.i("foreground:" + connectionServiceInfo.foreground);
            LogUtils.i("clientPackage:" + connectionServiceInfo.clientPackage);
            LogUtils.i("clientCount:" + connectionServiceInfo.clientCount);
            LogUtils.i("crashCount:" + connectionServiceInfo.crashCount);
        }
    }
}
