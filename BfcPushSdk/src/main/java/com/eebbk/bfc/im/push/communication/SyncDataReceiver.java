package com.eebbk.bfc.im.push.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.util.LogUtils;

/**
 * 连接数据接收广播
 */
public class SyncDataReceiver extends BroadcastReceiver {
    private static final String TAG = "SyncDataReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        dispatchAction(context, intent);
    }

    private void dispatchAction(Context context, Intent intent) {
        if (intent == null) {
            LogUtils.e(TAG, "intent is null , then do nothing !!!");
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e( TAG, "receiver action is empty!");
            return;
        }
        LogUtils.i("receive action is:" + action);
        if (action.equals(SyncAction.SYNC_CONNECTED_ACTION)) {
            intent.setClassName(context.getPackageName(), PushConnStatusChangedHandleService.class.getName());
//            startWakefulService(context, intent);
        } else if (action.equals(SyncAction.SYNC_DISCONNECTED_ACTION)) {
            intent.setClassName(context.getPackageName(), PushConnStatusChangedHandleService.class.getName());
//            startWakefulService(context, intent);
        } else if (action.equals(SyncAction.READ_DATA_ACTION)) {
            intent.setClassName(context.getPackageName(), MessageHandleService.class.getName());
//            startWakefulService(context, intent);
        }
    }
}
