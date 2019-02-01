package com.eebbk.bfc.im.push.service.heartbeat;

import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushInterface;
import com.eebbk.bfc.im.push.entity.request.EncryptSetRequestEntity;
import com.eebbk.bfc.im.push.entity.request.PublicKeyRequestEntity;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.communication.BaseHandleService;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

/**
 * 推送连接状态改变后的处理service
 */
public class PushConnStatusChangedHandleService extends BaseHandleService {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public PushConnStatusChangedHandleService() {
        super("PushConnStatusChangedHandleService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        super.onHandleIntent(intent);
        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        LogUtils.i("receive action:" + action);
        handleOnPushConnectStatus(action);
        PushInterface.getSyncApplicationSafely(new OnGetCallBack<SyncApplication>() {
            @Override
            public void onGet(SyncApplication app) {
                if (action.equals(SyncAction.SYNC_CONNECTED_ACTION)) {
                    LogUtils.i("handle action:" + action);
                    String createServicePkgName = intent.getStringExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG);
                    app.setCreateServicePackageName(createServicePkgName);
                    if (createServicePkgName.equals(getPackageName())) {
                        handleSyncPushConnected(app, intent);
                    } else {
                        LogUtils.w("the app is not the host,do not handle connected...");
                    }
                } else if (action.equals(SyncAction.SYNC_DISCONNECTED_ACTION)) {
                    handleSyncPushDisconnected(app);
                }
            }
        });
    }

    private void handleOnPushConnectStatus(String action) {
        if (action.equals(SyncAction.SYNC_CONNECTED_ACTION)) {
            SyncApplication.callOnPushConnectStatus(OnPushStatusListener.CONNECTED);
        } else if (action.equals(SyncAction.SYNC_DISCONNECTED_ACTION)) {
            SyncApplication.callOnPushConnectStatus(OnPushStatusListener.DISCONNECTED);
        } else if (action.equals(SyncAction.SYNC_CONNECTING_ACTION)) {
            SyncApplication.callOnPushConnectStatus(OnPushStatusListener.CONNECTING);
        } else if (action.equals(SyncAction.SYNC_CONNECT_FAIL_ACTION)) {
            SyncApplication.callOnPushConnectStatus(OnPushStatusListener.CONNECT_FAILED);
        }
    }

    private void handleSyncPushConnected(SyncApplication app, Intent intent) {
        LogUtils.d("handleSyncPushConnected");
        String hostname = intent.getStringExtra("hostname");
        int port = intent.getIntExtra("port", 0);
        app.setConnectionEnabled(true);
        app.setHostname(hostname);
        app.setPort(port);
        app.updateServerInfo(hostname, port);
        getPublicKey(app);
    }

    private void getPublicKey(final SyncApplication app) {
        if (app.hasPublicKey()) {
            setEncrypt(app);
            return;
        }
        PublicKeyRequestEntity entity = app.getRequestEntityFactory().createPublicKeyRequestEntity();
        Request request = Request.createRequest(app, entity);
        request.setOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    LogUtils.i("get public key success:" + response.getResponseEntity());
                } else {
                    LogUtils.e("get public key error:" + response.getResponseEntity());
                }
            }
        });
        request.send();
    }

    private void setEncrypt(SyncApplication app) {
        EncryptSetRequestEntity entity = app.getRequestEntityFactory().createEncryptSetRequestEntity();
        Request request = Request.createRequest(app, entity);
        request.setOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    LogUtils.d("set encrypt success");
                } else {
                    LogUtils.e("set encrypt error:" + response.getResponseEntity());
                }
            }
        });
        request.send();
    }

    private void handleSyncPushDisconnected(SyncApplication app) {
        LogUtils.d("handleSyncPushDisconnected");
        app.clearSyncRegistInfo();
    }
}
