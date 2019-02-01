package com.eebbk.bfc.im.push.service.host;

import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushInterface;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.communication.BaseHandleService;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.SyncApplication;

public class HostElectionHandleService extends BaseHandleService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public HostElectionHandleService() {
        super("HostElectionHandleService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        LogUtils.d("service action:" + action);
        PushInterface.getSyncApplicationSafely(new OnGetCallBack<SyncApplication>() {
            @Override
            public void onGet(SyncApplication app) {
                if (action.equals(SyncAction.STOP_CONN_SERVICE_ACTION)) {
                    app.electHostService();
                } else if (action.equals(SyncAction.PUSH_HOST_SERVICE_CHECK)) {
                    app.checkUpHostService();
                }
            }
        });
    }
}
