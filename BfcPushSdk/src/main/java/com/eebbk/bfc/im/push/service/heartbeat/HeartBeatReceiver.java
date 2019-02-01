package com.eebbk.bfc.im.push.service.heartbeat;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.communication.BaseHandleService;
import com.eebbk.bfc.im.push.service.host.HostServiceInfo;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.service.task.TaskType;

/**
 * 心跳包接收广播
 */
public class HeartBeatReceiver extends BaseHandleService.WakefulBroadcastReceiver {

    public static final String REDUNDANCY = "redundancy";
    public static final String SCREEN = "screen";

    @Override
    public void onReceive(final Context context, Intent intent) {
        HostServiceInfo hostServiceInfo = new HostServiceInfo();
        ConnectionService.getHostServiceInfo(context, hostServiceInfo);
        String servicePkgName = hostServiceInfo.getServicePkgName();
        if (TextUtils.isEmpty(servicePkgName)) {
            return;
        }
        LogUtils.i("servicePkgName:" + servicePkgName);
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(SyncAction.HEARTBEAT_REQUEST)) {
            LogUtils.i("receive a heartbeat request action:" + action);
            if (HeartBeatScheduler.isCanceled()) {
                LogUtils.w("heartbeat is canceled!");
            } else {
                HeartBeatScheduler.startNextHeartbeat(context);
            }
            boolean isRedundancy = intent.getBooleanExtra(REDUNDANCY, false);
            boolean isScreen = intent.getBooleanExtra(SCREEN, false);
            Intent serviceIntent = ConnectionService.createIntent(context, servicePkgName);
            serviceIntent.putExtra(TaskType.TAG, TaskType.HEART_BEAT);
            serviceIntent.putExtra(REDUNDANCY, isRedundancy);
            serviceIntent.putExtra(SCREEN, isScreen);
            startWakefulService(context, serviceIntent);
        }
    }

}
