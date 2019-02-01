package com.eebbk.bfc.im.push.service.host;

import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.PushImplements;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.communication.BaseHandleService;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.util.PublicValueStoreUtil;

public class HostElectionHandleService extends BaseHandleService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public HostElectionHandleService() {
        super("HostElectionHandleService");
    }
    private static final String TAG = "HostElectionHandleService";

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        if (intent == null) {
            LogUtils.e(TAG, "intent is null , then do nothing !!!");
            return;
        }
        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e(TAG,"action is null , then do nothing !!!");
            return;
        }
        LogUtils.d("宿主切换  >>> service action:" + action);
        PushImplements.getPushApplicationSafely(getApplicationContext(), new OnGetCallBack<PushApplication>() {
            @Override
            public void onGet(PushApplication app) {
                if (action.equals(SyncAction.STOP_CONN_SERVICE_ACTION)) {
                    app.electHostService();
                } else if (action.equals(SyncAction.PUSH_HOST_SERVICE_CHECK)) {
                    app.checkUpHostService();
                } else if (action.equals(SyncAction.PUSH_HOST_SERVICE_UPDATE)) {
                    Intent serviceIntent = app.getConnectionServiceManager().getServiceIntent();
                    if (serviceIntent != null && serviceIntent.getComponent() != null) {
                        String pkgName = serviceIntent.getComponent().getPackageName();
                        String hostPackageName = PublicValueStoreUtil.getHostPackageName();
                        if(!TextUtils.equals(pkgName, hostPackageName)){
                            app.electHostService();
                            Da.record(getApplicationContext(), new DaInfo().setFunctionName(Da.functionName.ELECT_HOST)
                                    .setTrigValue("HostElectionHandleService electHost pkgName" + pkgName + " hostPackageName:" + hostPackageName));
                        }else {
                            app.updateHostService();
                        }
                    }else {
                        app.updateHostService();
                    }
                }
            }
        });
    }
}
