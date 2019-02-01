package com.eebbk.bfc.im.push.service.heartbeat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.EebbkPush;
import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.PushImplements;
import com.eebbk.bfc.im.push.communication.BaseHandleService;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NotificationUtil;

/**
 * 手机连接上网络后处理service
 */
public class SyncHandleService extends BaseHandleService {
    private static final String TAG = "SyncHandleService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SyncHandleService() {
        super("SyncHandleService");
    }

    @SuppressLint("NewApi")
    @Override
    protected void onHandleIntent(final Intent intent) {
        super.onHandleIntent(intent);
        if(intent==null){
            LogUtils.e(TAG,"inteent is null !!!");
            return;
        }
        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e(TAG,"action is null !!!");
            return;
        }
        LogUtils.i(TAG,"receive action:" + action);

        startForeground(NotificationUtil.NOTIFICATION_ID, NotificationUtil.getNotification(this));

        handlePushCmd(action, intent);

        LogUtils.e(TAG,"====>>>  service job completed !!! ");
    }

    private void handlePushCmd(String action, Intent intent) {
        if (action.equals(SyncAction.PUSH_LOGIN_ACTION)) {

            PushImplements.getPushApplicationSafely(getApplicationContext(), new OnGetCallBack<PushApplication>() {
                @Override
                public void onGet(PushApplication app) {
                    if (app == null) {
                        LogUtils.d(TAG,"app is null !!!");
                        return;
                    }
                    app.setInitCalled(false);
                    app.callOnInitSuccessListener();
                }
            });
        } else if (action.equals(SyncAction.PUSH_INFO_COLLECTION)) {
            // do nothing
        } else if (action.equals(SyncAction.KILL_PUSH_PROCESS)) {
            int pid = intent.getIntExtra(ConnectionService.KILL_PUSH_PROCESS_ID_TAG, 0);
            if (pid != 0) {
                Process.killProcess(pid);

                LogUtils.w(TAG,"connection service is destroy !!!!");

                EebbkPush.init(getApplicationContext(), new OnInitSateListener() {
                    @Override
                    public void onSuccess() {
                        LogUtils.w(TAG,"connection service is destroy ,reInit here,success");
                    }

                    @Override
                    public void onFail(String errorMsg, String errorCode) {
                        LogUtils.e(TAG,"connection service is destroy ,reInit here,fail!!!");
                    }
                });
            }
        }
    }
}
