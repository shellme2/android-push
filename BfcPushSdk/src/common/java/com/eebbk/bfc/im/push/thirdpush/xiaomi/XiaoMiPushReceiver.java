package com.eebbk.bfc.im.push.thirdpush.xiaomi;

import android.content.Context;
import android.content.Intent;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.PushImplements;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;
import java.util.Map;

public class XiaoMiPushReceiver extends PushMessageReceiver {

    public static final String TAG = "xiaomi";

    @Override
    public void onReceiveMessage(Context context, MiPushMessage miPushMessage) {
        LogUtils.i(TAG, "收到一条小米推送消息onReceiveMessage");
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
        LogUtils.d(TAG,"xiaomi push notification on clicked");
        if (miPushMessage != null) {
            long dialogId = 0, syncKey = 0;
            Map<String, String> extra = miPushMessage.getExtra();
            if (extra != null) {
//                dialogId = Long.valueOf(extra.get("dialogId"));
//                syncKey = Long.valueOf(extra.get("syncKey"));
            }
//            SyncNotificationManager.getSyncNotificationManager().onClickNotification(dialogId, syncKey);
            launchApp(context);
        }
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        LogUtils.i(TAG, "小米推送通知消息到达onNotificationMessageArrived");
        if (miPushMessage != null) {
            long dialogId = 0, syncKey = 0;
            Map<String, String> extra = miPushMessage.getExtra();
            if (extra != null) {
//                dialogId = Long.valueOf(extra.get("dialogId"));
//                syncKey = Long.valueOf(extra.get("syncKey"));
            }
//            SyncNotificationManager.getSyncNotificationManager().onShowNotification(dialogId, syncKey);
        }
        PushImplements.getPushApplicationSafely(context,new OnGetCallBack<PushApplication>() {
            @Override
            public void onGet(PushApplication app) {
                if (app.isClosed()) {
                    app.reconnect();
                }
            }
        });
    }

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        LogUtils.i(TAG, "小米推送透传消息到达onReceivePassThroughMessage");
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        String mRegId;
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                LogUtils.i(TAG, "mRegId:" + mRegId);
            } else {
                LogUtils.e(TAG, "xiaomi push register error:" + message.getResultCode());
            }
        }
    }

    private void launchApp(Context context) {
        Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if(appIntent == null) {
            LogUtils.e(TAG, "cannot find app: " + context.getPackageName());
        } else {
            appIntent.setPackage((String)null);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            context.startActivity(appIntent);
        }
    }
}
