package com.eebbk.bfc.im.push.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.bean.SyncMessage;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.GsonUtil;

public abstract class PushReceiver extends BroadcastReceiver {

    @Override
    public final void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e("action is null.");
            return;
        }
        if (action.equals(SyncAction.SYNC_RESPONSE_ACTION)) {
            String data = intent.getStringExtra("data");
            SyncMessage syncMessage = GsonUtil.fromJSON(data, SyncMessage.class);
            onMessage(context, syncMessage);
        }
    }

    protected abstract void onMessage(Context context, SyncMessage syncMessage);
}
