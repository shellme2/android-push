package com.eebbk.bfc.im.push.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.bean.SyncMessage;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.util.JsonUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

public abstract class PushReceiver extends BroadcastReceiver {
    private static final String TAG = "PushReceiver";
    @Override
    public final void onReceive(Context context, Intent intent) {
        if (intent == null) {
            LogUtils.e(TAG,"intent is null !!!");
            return;
        }
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e( TAG, "action is null.");
            return;
        }

        // TODO: 2016/10/22 simple something
        if (action.equals(SyncAction.SYNC_RESPONSE_ACTION)) {
            String data = intent.getStringExtra("data");

            LogUtils.e(LogTagConfig.LOG_TAG_POINT_PUSH_MSG_GET,"PushReceiver data: "+data);
            SyncMessage syncMessage = JsonUtil.fromJson(data, SyncMessage.class);
            onMessage(context, syncMessage);
        }
    }

    protected abstract void onMessage(Context context, SyncMessage syncMessage);
}
