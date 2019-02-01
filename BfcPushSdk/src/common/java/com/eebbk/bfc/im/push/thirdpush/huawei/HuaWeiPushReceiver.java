package com.eebbk.bfc.im.push.thirdpush.huawei;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.service.dispatcher.SyncNotificationManager;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.huawei.android.pushagent.api.PushEventReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HuaWeiPushReceiver extends PushEventReceiver {

    public static final String TAG = "huawei";

    @Override
    public void onToken(Context context, String token, Bundle extras) {
        String belongId = extras.getString("belongId");
        String content = "华为推送获取token和belongId成功，token = " + token + ",belongId = " + belongId;
        if (!TextUtils.isEmpty(token)) {
            StoreUtil.saveHuaWeiPushToken(context, token);
        }
        LogUtils.i(TAG, "onToken:" + content);
    }

    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle extra) {
        LogUtils.i(TAG, "收到一条华为推送消息onPushMsg");
        /*SyncPushClient.getSyncApplicationSafely(new OnGetCallBack<SyncApplication>() {
            @Override
            public void onGet(SyncApplication app) {
                if (app.isClosed()) {
                    app.reconnect();
                }
            }
        });*/
        return false;
    }

    @Override
    public void onEvent(Context context, Event event, Bundle extra) {
        if (Event.NOTIFICATION_OPENED.equals(event)) {
            LogUtils.d(TAG,"huawei push notification on clicked");
            String content = extra.getString(BOUND_KEY.pushMsgKey);
            try {
                JSONArray jsonArray = new JSONArray(content);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                long dialogId = Long.valueOf(jsonObject.getString("dialogId"));
                long syncKey = Long.valueOf(jsonObject.getString("syncKey"));
                SyncNotificationManager.getSyncNotificationManager().onClickNotification(dialogId, syncKey);
            } catch (JSONException e) {
                LogUtils.e(e);
            }
        }
    }
}
