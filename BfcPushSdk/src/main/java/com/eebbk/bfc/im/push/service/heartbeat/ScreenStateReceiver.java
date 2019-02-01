package com.eebbk.bfc.im.push.service.heartbeat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.heartbeat.heartpackage.HeartbeatScheduler;
import com.eebbk.bfc.im.push.service.host.HostServiceInfo;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

public class ScreenStateReceiver extends BroadcastReceiver {
    private long screenOnTime;
    public ScreenStateReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        HostServiceInfo hostServiceInfo = new HostServiceInfo();
        ConnectionService.getHostServiceInfo(context, hostServiceInfo);
        String servicePkgName = hostServiceInfo.getServicePkgName();
        if (TextUtils.isEmpty(servicePkgName)) {
            return;
        }
        LogUtils.i("servicePkgName:" + servicePkgName);
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        if (!AppUtil.isServiceRunning(context, context.getPackageName(), ConnectionService.class.getName())
                && !AppUtil.isServiceRunning(context, servicePkgName, ConnectionService.class.getName())) {
            return;
        }
        LogUtils.i("receive action:" + action);
        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            long now = System.currentTimeMillis();
            if (now - screenOnTime >= 1000 * 60 * 3) {
                HeartBeatReceiver.startHeartbeatTask(context, servicePkgName, HeartbeatScheduler.REDUNDANCY_HEART);
            } else {
                LogUtils.w("screen on too frequency in 3 minutes!");
            }
            screenOnTime = now;
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
           /* Intent dozeIntent = new Intent(SyncAction.HEARTBEAT_REQUEST);
            dozeIntent.setClassName(servicePkgName, HeartBeatReceiver.class.getName());
            dozeIntent.setPackage(context.getPackageName());
            dozeIntent.putExtra(HeartBeatReceiver.REDUNDANCY, true);
            dozeIntent.putExtra(HeartBeatReceiver.SCREEN, true);
            context.sendBroadcast(dozeIntent);*/
        }
    }
}
