package com.eebbk.bfc.im.push.service.heartbeat;

import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushInterface;
import com.eebbk.bfc.im.push.bean.PushCollectInfo;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.communication.BaseHandleService;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.util.GsonUtil;

/**
 * 手机连接上网络后处理service
 */
public class SyncHandleService extends BaseHandleService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SyncHandleService() {
        super("SyncHandleService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        super.onHandleIntent(intent);
        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        LogUtils.i("receive action:" + action);
        handlePushCmd(action, intent);
        PushInterface.getSyncApplicationSafely(new OnGetCallBack<SyncApplication>() {
            @Override
            public void onGet(SyncApplication app) {
                if (action.equals(SyncAction.NETWORK_CONNECTED_ACTION)) {
                    app.reconnect();
                } else if (action.equals(action.equals(SyncAction.PUSH_LOGIN_ACTION))) {
                    app.callOnInitSuccessListener();
                } else if (action.equals(SyncAction.DEVICE_SHUTDOWN_ACTION)) {
                    app.exit();
                }
            }
        });
    }

    private void handlePushCmd(String action, Intent intent) {
        if (action.equals(SyncAction.PUSH_LOGIN_ACTION)) {
            long registerId = intent.getLongExtra(ConnectionService.REGISTER_TAG, 0);
            SyncApplication.callOnPushLogin(registerId);
        } else if (action.equals(SyncAction.PUSH_INFO_COLLECTION)) {
            String pushCollectInfoStr = intent.getStringExtra(ConnectionService.PUSH_INFO_COLLECTION_TAG);
            PushCollectInfo pushCollectInfo = null;
            if (!TextUtils.isEmpty(pushCollectInfoStr)) {
                pushCollectInfo = GsonUtil.fromJSON(pushCollectInfoStr, PushCollectInfo.class);
            }
            SyncApplication.callOnPushCollect(pushCollectInfo);
        } else if (action.equals(SyncAction.KILL_PUSH_PROCESS)) {
            int pid = intent.getIntExtra(ConnectionService.KILL_PUSH_PROCESS_ID_TAG, 0);
            if (pid != 0) {
                Process.killProcess(pid);
            }
        }
    }
}
