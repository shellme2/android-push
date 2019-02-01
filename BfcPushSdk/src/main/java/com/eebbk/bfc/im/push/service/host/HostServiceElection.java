package com.eebbk.bfc.im.push.service.host;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.communication.ConnectionServiceManager;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.util.PublicValueStoreUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.heartbeat.HeartBeatReceiver;
import com.eebbk.bfc.im.push.service.heartbeat.SyncPushSystemReceiver;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.TimeFormatUtil;

import java.util.List;

public class HostServiceElection {

    private static final String TAG = "HostServiceElection";
    private ConnectionServiceManager connectionServiceManager;

    public HostServiceElection(ConnectionServiceManager connectionServiceManager,Context context) {
        this.connectionServiceManager = connectionServiceManager;
    }

    private String getHostServicePackageName(){
        //todo 切换宿主 的 时候  在宿主选择流程中 应该再次查看真正的宿主是谁
        return PublicValueStoreUtil.getHostPackageName();
    }

    /**
     * Class: HostServiceElection
     * Tag: 宿主选择
     * Ref: HostElectionHandleService.SyncAction.STOP_CONN_SERVICE_ACTION
     * Fun: 如果当前IM服务为非宿主的，则Stop当前的IM服务，并且重新绑定到远程宿主的IM服务
     * @param context
     */
    public void electHostService(Context context) {
        Intent serviceIntent = connectionServiceManager.getServiceIntent();
        ComponentName componentName = serviceIntent.getComponent();
        if (componentName != null) {
            String pkgName = componentName.getPackageName();
            String hostPackageName = getHostServicePackageName();
            if (TextUtils.isEmpty(hostPackageName)) {
                LogUtils.e(TAG,"host service package name is empty !!!");
                // 当前IM服务查本地还没有宿主 不做处理
                return;
            }
            if (TextUtils.equals(pkgName, hostPackageName)) {
                LogUtils.d("the app:" + context.getPackageName() + " has bind to the host app:" + pkgName);
                // 当前IM服务为宿主的  不做处理
                return;
            }
            connectionServiceManager.shutdown();
            Intent intent = ConnectionService.createServiceIntent(context, hostPackageName);
            connectionServiceManager.setServiceIntent(intent);
            setLocalReceiver(context);
            connectionServiceManager.startConnect(true);
            LogUtils.e(TAG, context.getPackageName() + " elect the host app:" + intent.getComponent().getPackageName());
            Da.record(context, new DaInfo().setFunctionName(Da.functionName.ELECT_HOST)
                    .setTrigValue(context.getPackageName() + " elect the host app:" + intent.getComponent().getPackageName()));
        }
    }

    /**
     * Class: HostServiceElection
     * Tag: 宿主选择
     * Ref：ConnectionServiceManager.startConnect()
     * Fun: 检查宿主  检查当前IM服务是否和本地保存的宿主packageName一致，如果不一致则通知宿主进行check处理（PUSH_HOST_SERVICE_CHECK）
     * @param context
     */
    public void checkUpOwnService(Context context) {
        Intent serviceIntent = connectionServiceManager.getServiceIntent();
        ComponentName componentName = serviceIntent.getComponent();
        if (componentName != null) {
            String pkgName = componentName.getPackageName();
            String hostPackageName = getHostServicePackageName();
            if (TextUtils.isEmpty(hostPackageName)) {
                LogUtils.e(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"host service package name is empty !!!");
                return;
            }
            if (!TextUtils.equals(pkgName, hostPackageName)) {
                if (AppUtil.isAppActive(context, hostPackageName)) {
                    Intent stopIntent = new Intent(SyncAction.PUSH_HOST_SERVICE_CHECK);
                    stopIntent.setClassName(hostPackageName, HostElectionHandleService.class.getName());
                    LogUtils.d(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"start handle service,action:" + SyncAction.PUSH_HOST_SERVICE_CHECK);
                    context.startService(stopIntent);
                }
            }
        }
    }

    /**
     * Class: HostServiceElection
     * Tag: 宿主选择
     * Ref：HostElectionHandleService >>>>>>> SyncAction.PUSH_HOST_SERVICE_CHECK
     * Fun: 检查宿主IM服务
     * @param context
     */
    public void checkUpHostService(Context context) {
        List<ActivityManager.RunningServiceInfo> list = AppUtil.getRunningServiceList(context, ConnectionService.class.getName());
        LogUtils.i("RunningServiceInfo size:" + list.size());
        if (list.size() > 0) {
            printServiceInfo(list);
            setLocalReceiver(context);
            checkUpRunningServiceInfo(context, list);
        } else {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_PUSH_INIT,"call back when init is failed ,start connection service error!!!");
        }
    }

    /**
     * Class: HostServiceElection
     * Tag: 宿主选择
     * Ref: HostServiceElection.checkUpHostService(context)
     * Fun: 检测本身的app是否启动了多余的推送共享service，如果当前的app为宿主，同时这些活着的Service是非宿主的，则做进一步处理（远程停止非宿主service）
     * @param list service列表
     */
    private void checkUpRunningServiceInfo(Context context, List<ActivityManager.RunningServiceInfo> list) {
        String localPkgName = context.getPackageName();
        for (ActivityManager.RunningServiceInfo connectionServiceInfo : list) {
            String servicePkgName = connectionServiceInfo.service.getPackageName();
            String hostPackageName = getHostServicePackageName();
            if (TextUtils.isEmpty(hostPackageName)) {
                LogUtils.e(TAG,"host service package name is empty !!!");
                return;
            }
            if (localPkgName.equals(hostPackageName)
                    && !servicePkgName.equals(hostPackageName)) { // 只有宿主app才能发出停止其他寄生app的service的请求
                LogUtils.w(TAG,"宿主切换 >>> localPkgName="+localPkgName+"  getHostServicePackageName="+hostPackageName+" servicePkgName="+servicePkgName);
                Intent stopIntent = new Intent(SyncAction.STOP_CONN_SERVICE_ACTION);
                stopIntent.setClassName(servicePkgName, HostElectionHandleService.class.getName());
                context.startService(stopIntent);
                LogUtils.d("start handle service,action:" + SyncAction.STOP_CONN_SERVICE_ACTION);
            }
        }
        LogUtils.d("checkUpRunningServiceInfo finished");
    }

    /**
     * Class: HostServiceElection
     * Tag: 宿主选择
     * Ref：HostServiceElection.checkUpHostService(context) | HostServiceElection.electHostService(context)
     * Fun：禁用非宿主的静态广播  启动宿主的静态广播（heartbeatReceiver&syncPushSysReceiver）
     * @param context
     */
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
                    + "," + HeartBeatReceiver.class.getName() + " & " + SyncPushSystemReceiver.class.getName() + " is disabled...");
        } else {
            // 启用 manifest receiver
            pm.setComponentEnabledSetting(heartbeatReceiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(syncPushSysReceiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            LogUtils.i("service pkgName:" + serviceIntentComponent.getPackageName() + ",local pkgName:" + pkgName
                    + "," + HeartBeatReceiver.class.getName() + " & " + SyncPushSystemReceiver.class.getName() + " is enable...");
        }
    }

    private void printServiceInfo(List<ActivityManager.RunningServiceInfo> list) {
        for (ActivityManager.RunningServiceInfo connectionServiceInfo : list) {
            LogUtils.i(TextUtils.concat(
                    "printServiceInfo==================================================>>>",
                    "pid:" + connectionServiceInfo.pid, "\n",
                    "process:" + connectionServiceInfo.process, "\n",
                    "serviceName:" + connectionServiceInfo.service.getClassName(), "\n",
                    "packageName:" + connectionServiceInfo.service.getPackageName(), "\n",
                    "activeSince:" + TimeFormatUtil.format(connectionServiceInfo.activeSince), "\n",
                    "lastActivityTime:" + TimeFormatUtil.format(connectionServiceInfo.lastActivityTime), "\n",
                    "started:" + connectionServiceInfo.started, "\n",
                    "foreground:" + connectionServiceInfo.foreground, "\n",
                    "clientPackage:" + connectionServiceInfo.clientPackage, "\n",
                    "clientCount:" + connectionServiceInfo.clientCount, "\n",
                    "crashCount:" + connectionServiceInfo.crashCount
            ).toString());
        }
    }
}
