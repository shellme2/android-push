package com.eebbk.bfc.im.push.communication;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.service.heartbeat.PushConnStatusChangedHandleService;

/**
 * 连接数据接收广播
 */
public class SyncDataReceiver extends BaseHandleService.WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        dispatchAction(context, intent);
    }

    private void dispatchAction(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e("receiver action is empty!");
            return;
        }
        LogUtils.i("receive action is:" + action);
        if (action.equals(SyncAction.SYNC_CONNECTED_ACTION)) {
            intent.setClassName(context.getPackageName(), PushConnStatusChangedHandleService.class.getName());
            startWakefulService(context, intent);
        } else if (action.equals(SyncAction.SYNC_DISCONNECTED_ACTION)) {
            intent.setClassName(context.getPackageName(), PushConnStatusChangedHandleService.class.getName());
            startWakefulService(context, intent);
        } else if (action.equals(SyncAction.READ_DATA_ACTION)) {
            intent.setClassName(context.getPackageName(), MessageHandleService.class.getName());
            startWakefulService(context, intent);
        }
    }
}
