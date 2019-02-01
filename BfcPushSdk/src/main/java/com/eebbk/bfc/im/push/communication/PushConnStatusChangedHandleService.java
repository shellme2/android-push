package com.eebbk.bfc.im.push.communication;

import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.PushImplements;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.entity.request.EncryptSetRequestEntity;
import com.eebbk.bfc.im.push.entity.request.PublicKeyRequestEntity;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.PublicValueStoreUtil;

/**
 * 推送连接状态改变后的处理service
 */
public class PushConnStatusChangedHandleService extends BaseHandleService {


    private static final String TAG = "PushConnStatusChangedHandleService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public PushConnStatusChangedHandleService() {
        super("PushConnStatusChangedHandleService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        super.onHandleIntent(intent);

        if (intent == null) {
            LogUtils.e(TAG,"intent is null !!!");
            return;
        }

        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e(TAG,"action == null");
            return;
        }
        LogUtils.i("receive action:" + action+ "   app:"+getPackageName());

        if (action.equalsIgnoreCase(SyncAction.SYNC_CONNECTING_ACTION)) {
            // do nothing
            return;
        }

        PushImplements.getPushApplicationSafely(getApplicationContext(), new OnGetCallBack<PushApplication>() {
            @Override
            public void onGet(PushApplication app) {
                if (action.equals(SyncAction.SYNC_CONNECTED_ACTION)) {
                    LogUtils.i("handle action:" + action);
                    String createServicePkgName = intent.getStringExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG);
                    String hostPackageName = PublicValueStoreUtil.getHostPackageName();
                    String currHostService = null;
                    try {
                        currHostService = app.getConnectionServiceManager().getServiceIntent().getComponent().getPackageName();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    LogUtils.e(TAG, "hostPackageName:" + hostPackageName + " currHostService:" + currHostService);
                    Da.record(getApplicationContext(), new DaInfo().setFunctionName(Da.functionName.CHECK_HOST)
                            .setTrigValue("hostPackageName:" + hostPackageName + " currHostService:" + currHostService));
                    if(!TextUtils.equals(hostPackageName, currHostService)){
                        LogUtils.e(TAG, "PushConnStatusChangedHandleService --> electHostService()");
                        app.setInitCalled(false);
                        app.electHostService();
                    }else {
                        //todo 不知道为什么之前 连接成功后不通知非宿主应用 暂且这么处理 看有没有什么影响
                        handleSyncPushConnected(app, intent);
                        app.callBackPushStatus(OnPushStatusListener.Status.CONNECTED);
                    }
                    LogUtils.w("the app is not the host,but handle connected too ... createServicePkgName:"+createServicePkgName);
                } else if (action.equals(SyncAction.SYNC_DISCONNECTED_ACTION)) {
                    handleSyncPushDisconnected(app);
                    String currHostService = intent.getStringExtra(ConnectionService.CREATE_SERVICE_PACKAGE_NAME_TAG);
                    String hostPackageName = PublicValueStoreUtil.getHostPackageName();
                    if(TextUtils.equals(hostPackageName, currHostService)){
                        app.callBackPushStatus(OnPushStatusListener.Status.DISCONNECTED);
                    }
                } else if (action.equals(SyncAction.SYNC_CONNECT_FAIL_ACTION)) {
                    String errorMsg = intent.getStringExtra(ConnectionService.CONNECT_ERROR_MSG_TAG);
                    String hostName = intent.getStringExtra(ConnectionService.HOSTNAME_TAG);
                    String port = String.valueOf(intent.getIntExtra(ConnectionService.PORT_TAG,0));
                    app.callOnInitFailListener("=====TCP连接失败=====\n errorMsg: "+errorMsg+" \n hostName="+hostName+"\n port="+port,"000");
                }
            }
        });
    }

    private void handleSyncPushConnected(PushApplication app, Intent intent) {
        LogUtils.d("handleSyncPushConnected");
        String hostname = intent.getStringExtra("hostname");
        int port = intent.getIntExtra("port", 0);
        app.setConnectionEnabled(true);
        app.setHostname(hostname);
        app.setPort(port);
        app.updateServerInfo(hostname, port);
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_PUBLIC_KEY,"GetPublicKey","get public key when tcp is connected");
        getPublicKey(app);
    }

    private void getPublicKey(final PushApplication app) {
        if (app.hasPublicKey()) {
            LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_PUBLIC_KEY,"PublicKeyExist","Public key is exist,so just set encrypt next !!!");
            setEncrypt(app);
            return;
        }
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_PUBLIC_KEY,"PublicKeyRequest","Public key is not exist,so we send public key request next !!!");
        PublicKeyRequestEntity entity = app.getRequestEntityFactory().createPublicKeyRequestEntity();
        Request request = Request.createRequest(app, entity);
        request.setOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    LogUtils.i(TAG,"get public key success:" + response.getResponseEntity());
                } else {
                    LogUtils.e( TAG, "get public key error:" + response.getResponseEntity());
                }
            }
        });
        request.send();
    }

    private void setEncrypt(PushApplication app) {
        LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_SET_ENCRYPT,"we set encrypt when  public key is exist !!!");

        EncryptSetRequestEntity entity = app.getRequestEntityFactory().createEncryptSetRequestEntity();
        Request request = Request.createRequest(app, entity);
        request.setOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    LogUtils.d(TAG,"set encrypt success");
                } else {
                    LogUtils.e( TAG, "set encrypt error:" + response.getResponseEntity());
                }
            }
        });
        request.send();
    }

    private void handleSyncPushDisconnected(PushApplication app) {
        LogUtils.d("handleSyncPushDisconnected");
        app.clearSyncRegisterInfo();
    }
}
