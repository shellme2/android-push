package com.eebbk.bfc.im.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.eebbk.bfc.im.push.bean.AliasAndTagsInfo;
import com.eebbk.bfc.im.push.bean.SyncRegisterInfo;
import com.eebbk.bfc.im.push.communication.ConnectionServiceManager;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.config.UrlConfig;
import com.eebbk.bfc.im.push.debug.DebugBasicInfo;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.LoginRequestEntity;
import com.eebbk.bfc.im.push.entity.request.PublicKeyRequestEntity;
import com.eebbk.bfc.im.push.entity.request.RegisterRequestEntity;
import com.eebbk.bfc.im.push.entity.request.RequestEntityFactory;
import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncTriggerRequestEntity;
import com.eebbk.bfc.im.push.error.ErrorCode;
import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.listener.OnResultListener;
import com.eebbk.bfc.im.push.listener.OnServiceConnectionListener;
import com.eebbk.bfc.im.push.request.LoginRequest;
import com.eebbk.bfc.im.push.request.RegisterRequest;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.request.RequestManager;
import com.eebbk.bfc.im.push.request.RequestSweeper;
import com.eebbk.bfc.im.push.request.SetAliasAndTagRequest;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.ResponseDispatcher;
import com.eebbk.bfc.im.push.response.SyncKeyManager;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.dispatcher.SyncNotificationManager;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.DateFormatUtil;
import com.eebbk.bfc.im.push.util.DeviceUtils;
import com.eebbk.bfc.im.push.util.ExecutorsUtils;
import com.eebbk.bfc.im.push.util.FileUtil;
import com.eebbk.bfc.im.push.util.IDUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NetUtil;
import com.eebbk.bfc.im.push.util.PublicValueStoreUtil;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.eebbk.bfc.im.push.util.platform.PhoneDevice;
import com.eebbk.bfc.im.push.util.platform.PhonePlatform;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;
import com.eebbk.bfc.im.push.util.platform.Platform;
import com.eebbk.bfc.im.push.util.platform.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.eebbk.bfc.im.push.PushImplements.isIniting;
import static com.eebbk.bfc.im.push.PushImplements.isStopPushRun;

/**
 * IM SDK整体环境
 */
public class PushApplication {

    private static final String TAG = "PushApplication";


    private static PushApplication mInstance = null;

    private Context mContext;

    private RequestEntityFactory mRequestEntityFactory;

    /**
     * 请求反馈分发
     */
    private ResponseDispatcher mDispatcher;

    /**
     * 请求管理
     */
    private RequestManager mRequestManager;

    /**
     * Request队列定时清理
     */
    private RequestSweeper mRequestSweeper;

    /**
     * 消息同步序号管理器
     */
    private SyncKeyManager mSyncKeyManager;

    /**
     * 连接服务管理
     */
    private ConnectionServiceManager mConnectionServiceManager;

    private Platform mPlatform;

    private SyncRegisterInfo mSyncRegisterInfo;

    private List<String> mServerInfoList;

    /**
     * 连接是否可用，一般只要成功连接上一次就认为连接是可用的
     */
    private boolean mConnectionEnabled;

    /**
     * 初始化回调监听
     */
    private OnInitSateListener mOnInitSateListener;

    /**
     * 推送状态回调监听
     */
    private OnPushStatusListener mOnPushStatusListener;

    private boolean mInitCalled;

    private Queue<String> mSyncRespMsgIdQueue;

    private String mCurrentAlias;

    private String mHostServicePackageName;

    private String mHostname = UrlConfig.sHostNameDef;
    private int mPort = UrlConfig.sPortDef;
    private static final String LOCK_SATE_LISTENER = "LOCK_SATE_LISTENER";

    private OnServiceConnectionListener mOnServiceConnectionListener = new OnServiceConnectionListener() {
        @Override
        public void onServiceConnected() {
            getSyncRegisterInfoSafelyImpl(null);
        }

        @Override
        public void onServiceDisconnected() {
            PushApplication.this.mSyncRegisterInfo = null;
        }
    };

    private PushApplication(Context ctx) {
        initVar(ctx);
        IDUtil.init(this);
    }

    public static void initInstance(Context context) {
        if (mInstance == null) {
            synchronized (PushApplication.class) {
                if (mInstance == null) {
                    LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_PUSH_INIT, "Step02: new a  PushApplication static instance . ");
                    mInstance = new PushApplication(context);
                }
            }
        }
    }

    public static PushApplication getInstance() {
        if (mInstance == null) {
            throw new RuntimeException(ErrorCode.EC_INIT_PUSH_APPLICATION + "::PushApplication is not init ,please check!");
        }
        return mInstance;
    }

    private void initVar(Context ctx) {
        this.mContext = ctx.getApplicationContext() != null ? ctx.getApplicationContext() : ctx;
        mServerInfoList = new ArrayList<>();
        mSyncRespMsgIdQueue = new ConcurrentLinkedQueue<>();
        mPlatform = new PhonePlatform(mContext);  // 创建手机平台工具类

        mSyncKeyManager = new SyncKeyManager(this);
        mRequestManager = new RequestManager(this);    // request请求管理
        mRequestEntityFactory = new RequestEntityFactory(this);    // request entity创建工厂
        mDispatcher = new ResponseDispatcher(this);    //response分发
        mRequestSweeper = new RequestSweeper(this);  //创建Request定时清理器
        mRequestSweeper.start();

        mConnectionServiceManager = new ConnectionServiceManager(this, mOnServiceConnectionListener);
        mHostServicePackageName = getHostServicePackageName();
    }

    /**
     * 初始化SDK环境
     */
    public void init(OnInitSateListener onInitSateListener, OnPushStatusListener onPushStatusListener) {
        setOnInitSateListener(onInitSateListener);
        if(onPushStatusListener != null){
            //因为很多地方重启都会跑这里，这些调用的地方onPushStatusListener都是传空，为了避免被冲掉,所以非空才赋值
            this.mOnPushStatusListener = onPushStatusListener;
        }
        isIniting = true;
        LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_PUSH_INIT, "set isIniting to " + isIniting);
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_PUSH_INIT, "Step End:Then we start connectionService ,LogTag is next to : " +
                "LOG_TAG_FLOW_CONNECT_SERVICE ");
        start(false);
    }

    /**
     * 添加预埋ip
     */
    void addBackupServerInfo(String[] backupServerInfo, boolean clearBefore) {
        if (clearBefore) {
            mServerInfoList.clear();
        }
        for (String s : backupServerInfo) {
            String[] ip_port = s.split(":");
            String m_ip = ip_port[0];
            int m_port = Integer.parseInt(ip_port[1]);
            updateServerInfo(m_ip, m_port);
        }
        LogUtils.i(TAG, "mServerInfoList:" + mServerInfoList);
        mConnectionServiceManager.addBackupServerInfo(backupServerInfo, clearBefore);
    }

    public void updateServerInfo(String hostname, int port) {
        if (TextUtils.isEmpty(hostname) || port <= 0 || port > 65535) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hostname);
        stringBuilder.append(":");
        stringBuilder.append(port);
        if (!mServerInfoList.contains(stringBuilder.toString())) {
            mServerInfoList.add(stringBuilder.toString());
        }
    }

    public void reconnect() {
        LogUtils.i(TAG, "start to reconnect to service...");
        if (mConnectionServiceManager.isShutdown()) {
            LogUtils.e(TAG, "reconnect fail ,service and connect shut down");
            return;
        }
        start(true);
    }

    public synchronized void exit() {

        LogUtils.i(TAG, "app exit clear data..");

//        releaseAnalyze();

        isIniting = false;
        LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_PUSH_INIT, "App Exit", "set isIniting to " + isIniting);
        mInitCalled = false;
        LogUtils.w(TAG,LogTagConfig.LOG_TAG_ST,"mInitCalled:"+mInitCalled);
//        mConnectionServiceManager.clearBackupServerInfo();
//        mConnectionServiceManager.shutdown();
        if (mConnectionServiceManager != null) {
            mConnectionServiceManager.unBindService();
        }
        if (mServerInfoList != null) {
            mServerInfoList.clear();
        }
        if (mRequestSweeper != null) {
            mRequestSweeper.cancel();
        }
        if (mSyncKeyManager != null) {
            mSyncKeyManager.clear();
        }
        if (mRequestManager != null) {
            mRequestManager.clear();
        }
        if (mDispatcher != null) {
            mDispatcher.stop();
        }
        if (mSyncRespMsgIdQueue != null) {
            mSyncRespMsgIdQueue.clear();
        }
        SyncNotificationManager.getSyncNotificationManager().clear();
        mSyncRegisterInfo = null;
    }

    public void setInitCalled(boolean initCalled) {
        this.mInitCalled = initCalled;
    }

    private void releaseAnalyze() {
        if (homeKeyEventReceiver != null) {
            LogUtils.w(TAG,"unregister push analyze");
            mContext.unregisterReceiver(homeKeyEventReceiver);

            homeKeyEventReceiver = null;
        }
    }

    public boolean isClosed() {
        return mConnectionServiceManager.isClosed();
    }
    public boolean isClosedOnInit() {
        return mConnectionServiceManager.isClosedOnInit();
    }

    public void start(boolean reconnect) {
        mConnectionServiceManager.startConnect(reconnect);  //启动连接服务
    }

    public void electHostService() {
        mConnectionServiceManager.electHostService();
    }

    public void checkUpHostService() {
        mConnectionServiceManager.checkUpHostService();
    }

    public void updateHostService() {
        LogUtils.w(TAG, "宿主切换 >>> 更新当前App:" + mContext.getPackageName());
        mConnectionServiceManager.startConnect(false);
    }


    public RequestEntityFactory getRequestEntityFactory() {
        return mRequestEntityFactory;
    }

    public RequestManager getRequestManager() {
        return mRequestManager;
    }

    public RequestSweeper getRequestSweeper() {
        return mRequestSweeper;
    }

    public ConnectionServiceManager getConnectionServiceManager() {
        return mConnectionServiceManager;
    }

    public ResponseDispatcher getDispatcher() {
        return mDispatcher;
    }

    public Platform getPlatform() {
        return mPlatform;
    }

    public String getPackageName() {
        return mContext.getPackageName();
    }

    public SyncKeyManager getSyncKeyManager() {
        return mSyncKeyManager;
    }

    public Context getContext() {
        return mContext;
    }

    public String getAlias() {
        return mCurrentAlias;
    }

    public void callOnInitSuccessListener() {
        if (mInitCalled) {
            //on init success listener was called
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_PUSH_INIT, "on init success listener was called，so return and do nothing next time . mInitCalled："+mInitCalled);
            return;
        }
        mInitCalled = true;
        isIniting = false;
        LogUtils.w(TAG,LogTagConfig.LOG_TAG_ST,"mInitCalled:"+mInitCalled);

        LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_PUSH_INIT, "Init Success", "call back when init is success ,then set isIniting to " + isIniting);
        LogUtils.v( LogTagConfig.LOG_TAG_FLOW_SET_ALIAS, "set alias when is login success !!!");

        setAliasAndTagRequest(null, null, new OnAliasAndTagsListener() {
            @Override
            public void onSuccess(String alias, List<String> tags) {
                LogUtils.i(TAG, "init to set alias success ,the alias is :" + alias);
                callBackInitSateSuccess();
            }

            @Override
            public void onFail(String alias, List<String> tags, String errorMsg, String errorCode) {
                LogUtils.ec(TAG, "init to set alias fail,please check!! the alias is: " + alias, errorCode);
                callBackInitSateFail("set alias is fail,the alias is " + alias, ErrorCode.EC_SET_ALIAS_FAIL);
            }
        });

        LogUtils.d(TAG, "call on init success listener");
    }

    public void callOnInitFailListener(String errorMsg, String errorCode) {
        if (mInitCalled) {
            LogUtils.w(TAG, "on init failed listener was called,so return and do nothing next time . mInitCalled："+mInitCalled);
            return;
        }
        mInitCalled = true;
        isIniting = false;
        LogUtils.w(TAG,LogTagConfig.LOG_TAG_ST,"mInitCalled:"+mInitCalled);
        LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_PUSH_INIT, "Init Failed", "call back when init is failed ,then set isIniting to " +
                isIniting);
        callBackInitSateFail(errorMsg, errorCode);
        LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP, errorMsg, errorCode);
    }

    public boolean isLogin() {
        return mConnectionServiceManager.isLogin();
    }

    public boolean hasPublicKey() {
        byte[] publicKey = StoreUtil.readPublicKey(mPlatform.getStore());
        return publicKey != null && publicKey.length > 0 || mConnectionServiceManager.hasPublicKey();
    }

    public void savePublicKey(byte[] publicKey) {
        if (publicKey == null || publicKey.length == 0) {
            LogUtils.e(TAG, "save public key error,the public key is empty");
            return;
        }
        StoreUtil.savePublicKey(mPlatform.getStore(), publicKey);
    }

    public String getHostname() {
        return mHostname;
    }

    public int getPort() {
        return mPort;
    }

    public void setHostname(String hostname) {
        this.mHostname = hostname;
    }

    public void setPort(int port) {
        this.mPort = port;
    }

    public boolean isConnectionEnabled() {
        return mConnectionEnabled;
    }

    public void setConnectionEnabled(boolean mConnectionEnabled) {
        this.mConnectionEnabled = mConnectionEnabled;
    }

    public void reLogin() {
        mConnectionServiceManager.reLogin();
    }

    public static boolean checkRegisterInfo(SyncRegisterInfo syncRegisterInfo) {
        if (syncRegisterInfo == null) {
            return false;
        }
        if (syncRegisterInfo.getRegisterId() == 0) {
            return false;
        }
        if (TextUtils.isEmpty(syncRegisterInfo.getRegisterToken())) {
            return false;
        }
        return syncRegisterInfo.isLogin();
    }

    public boolean isRegistered() {
        SyncRegisterInfo syncRegisterInfo = getSyncRegisterInfo();
        return syncRegisterInfo != null && syncRegisterInfo.getRegisterId() != 0;
    }

    public void clearSyncRegisterInfo() {
        mSyncRegisterInfo = null;
    }

    public SyncRegisterInfo getSyncRegisterInfo() {
        if (checkRegisterInfo(mSyncRegisterInfo)) {
            return mSyncRegisterInfo;
        }
        SyncRegisterInfo tmp = mConnectionServiceManager.getSyncRegisterInfo();
        if (checkRegisterInfo(tmp)) {
            mSyncRegisterInfo = tmp;
        }
        return tmp;
    }

    public void getSyncRegisterInfoSafely(final OnGetCallBack<SyncRegisterInfo> onGetCallBack) {
        if (checkRegisterInfo(mSyncRegisterInfo)) {
            LogUtils.i(TAG, "get cache sync register info:" + mSyncRegisterInfo);
            onGetCallBack.onGet(mSyncRegisterInfo);
            return;
        }
        getSyncRegisterInfoSafelyImpl(onGetCallBack);
    }

    private void getSyncRegisterInfoSafelyImpl(final OnGetCallBack<SyncRegisterInfo> onGetCallBack) {
        mConnectionServiceManager.getSyncRegisterInfoSafely(new OnGetCallBack<SyncRegisterInfo>() {
            @Override
            public void onGet(SyncRegisterInfo syncRegisterInfo) {
                if (checkRegisterInfo(syncRegisterInfo)) {
                    PushApplication.this.mSyncRegisterInfo = syncRegisterInfo;
                    LogUtils.i(TAG, "get sync register info success: syncRegisterInfo=" + syncRegisterInfo);
                } else {
                    LogUtils.e(TAG, "get sync register info failed : syncRegisterInfo=" + syncRegisterInfo);
                }
                if (onGetCallBack != null) {
                    onGetCallBack.onGet(syncRegisterInfo);
                }
            }
        });
    }

    public void heartbeat() {
        mConnectionServiceManager.heartbeat();
    }

    public void enqueueRequest(Request request) {
        mConnectionServiceManager.enqueueTask(request);
    }

    public void register() {
        List<Request> list = mRequestManager.search(Command.REGISTER_REQUEST);
        if (list.size() > 0) {
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_REGISTER, "the register request is on going,do not execute another register request...");
            return;
        }

        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_REGISTER, "enter to the register .....");

        RegisterRequestEntity entity = mRequestEntityFactory.createRegisterRequestEntity();
        LogUtils.d(TAG, "to register,entity:" + entity);
        RegisterRequest registerRequest = new RegisterRequest(this, entity, new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    LogUtils.i(TAG, "register success!!!");
                } else {
                    LogUtils.e(TAG, "register failed:" + response.getResponseEntity());
                }
            }
        });
        registerRequest.send();
    }

    public void login() {
        List<Request> list = mRequestManager.search(Command.LOGIN_REQUEST);
        if (list.size() > 0) {
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_LOGIN, "the login request is on going,do not execute another login request...");
            return;
        }
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_LOGIN, "enter to the login .....");

        getSyncRegisterInfoSafely(new OnGetCallBack<SyncRegisterInfo>() {
            @Override
            public void onGet(SyncRegisterInfo syncRegisterInfo) {
                if (syncRegisterInfo == null) {
                    LogUtils.e(TAG,"syncRegisterInfo is null ,just stop login !!! ");
                    return;
                }
                LoginRequestEntity entity = mRequestEntityFactory.createLoginRequestEntity(syncRegisterInfo.getRegisterId(), syncRegisterInfo
                        .getRegisterToken());
                LogUtils.d(TAG, "to login,entity:" + entity);
                LoginRequest loginRequest = new LoginRequest(PushApplication.this, entity, new OnReceiveListener() {
                    @Override
                    public void onReceive(Request request, Response response) {
                        if (response.isSuccess()) {
                            LogUtils.i(TAG, "login success!!!");
                        } else {
                            LogUtils.e(TAG, "login failed:" + response.getResponseEntity());
                        }
                    }
                });
                loginRequest.send();
            }
        });
    }

    public void setStopPush(final OnResultListener onResultListener) {
        List<Request> list = mRequestManager.search(Command.PUSH_ALIAS_AND_TAG_REQUEST);
        if (list.size() > 0) {
            LogUtils.w(TAG, "the setStopPush request is on going,do not execute another login request...");
            return;
        }

        LogUtils.i(TAG, "enter to the setStopPush .....");

        doAliasAndTagRequest("", null, new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    mCurrentAlias = "";
//                            StoreUtil.saveIsStopPush(new PhoneStore(mContext),true);
                } else {
                    LogUtils.e(TAG, "stop push fail !!!");
                }
                if (onResultListener != null) {
                    onResultListener.onReceive(request, response);
                }
            }
        });

    }

    public void setResumePush(final OnResultListener onResultListener) {
        List<Request> list = mRequestManager.search(Command.PUSH_ALIAS_AND_TAG_REQUEST);
        if (list.size() > 0) {
            LogUtils.w(TAG, "the setResumePush request is on going,do not execute another login request...");
            return;
        }

        LogUtils.i(TAG, "enter to the setResumePush .....");

        final AliasAndTagsInfo aliasAndTagsInfo = StoreUtil.readAliasAndTag(new PhoneStore(mContext));

        if (TextUtils.isEmpty(aliasAndTagsInfo.getAlias())) {
            aliasAndTagsInfo.setAlias(DeviceUtils.getMachineId(mContext));
        }

        doAliasAndTagRequest(aliasAndTagsInfo.getAlias(), aliasAndTagsInfo.getTagsList(), new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    mCurrentAlias = aliasAndTagsInfo.getAlias();
//                                    StoreUtil.saveIsStopPush(store,false);
                } else {
                    LogUtils.e(TAG, "resume push fail !!!");
                }
                if (onResultListener != null) {
                    onResultListener.onReceive(request, response);
                }
            }
        });
    }

    public void setTagsRequest(final List<String> tags, final OnReceiveListener onReceiveListener) {
        List<Request> list = mRequestManager.search(Command.PUSH_ALIAS_AND_TAG_REQUEST);
        if (list.size() > 0) {
            LogUtils.w(TAG, "the setAliasAndTagRequest request is on going,do not execute another login request...");
            return;
        }

        LogUtils.i(TAG, "enter to the setTagsRequest .....");

        Store store = new PhoneStore(mContext);
        AliasAndTagsInfo aliasAndTagsInfo = StoreUtil.readAliasAndTag(store);

        if (aliasAndTagsInfo.isSet()) {
            mCurrentAlias = aliasAndTagsInfo.getAlias();
        } else {
            mCurrentAlias = DeviceUtils.getMachineId(mContext);
            aliasAndTagsInfo.setAlias(mCurrentAlias);
        }
        aliasAndTagsInfo.setTags(tags);

        doAliasAndTagRequest(mCurrentAlias, tags, onReceiveListener);

    }

    public void setAliasAndTagRequest(String alias, List<String> tags, final OnReceiveListener onReceiveListener) {

        List<Request> list = mRequestManager.search(Command.PUSH_ALIAS_AND_TAG_REQUEST);
        if (list.size() > 0) {
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_SET_ALIAS, "the setAliasAndTagRequest request is on going,do not execute another login " +
                    "request...");
            return;
        }

        final AliasAndTagsInfo aliasAndTagsInfo = StoreUtil.readAliasAndTag(new PhoneStore(mContext));

        if (TextUtils.isEmpty(aliasAndTagsInfo.getAlias())) {
            aliasAndTagsInfo.setAlias(DeviceUtils.getMachineId(mContext));
        }

        mCurrentAlias = aliasAndTagsInfo.getAlias();
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_SET_ALIAS, "start set AliasAndTagRequest .....mCurrentAlias=" + mCurrentAlias);
        doAliasAndTagRequest(aliasAndTagsInfo.getAlias(), aliasAndTagsInfo.getTagsList(), onReceiveListener);

    }

    private void doAliasAndTagRequest(final String alias, final List<String> tags, final OnReceiveListener onReceiveListener) {
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_SET_ALIAS, "doAliasAndTagRequest() tags = " + tags + " tags:" + tags);
        getSyncRegisterInfoSafely(new OnGetCallBack<SyncRegisterInfo>() {
            @Override
            public void onGet(SyncRegisterInfo syncRegisterInfo) {
                if (syncRegisterInfo == null) {
                    LogUtils.e(TAG,"syncRegisterInfo is null !!!");
                    return;
                }
                AliasAndTagsRequestEntity entity = getRequestEntityFactory().
                        createAliasAndTagRequestEntity(alias, tags, syncRegisterInfo.getRegisterId());//别名默认为序列号

                LogUtils.d(TAG, "setAliasAndTagRequest alias and tag--> " + alias + "::" + tags);

                SetAliasAndTagRequest setAliasAndTagRequest = new SetAliasAndTagRequest(PushApplication.this, entity, onReceiveListener);
                setAliasAndTagRequest.send();
            }
        });
    }


    public void sendPushSyncTrigger(final OnResultListener onResultListener) {
        sendPushSyncTrigger(mCurrentAlias, onResultListener);
    }

    public void sendPushSyncTrigger(final String alias, final OnResultListener onResultListener) {
        if (isStopPushRun()) {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_REGISTER, "we had stop the push ,so can not send trigger request !!!");
            if (onResultListener != null) {
                onResultListener.onFail("we have stop the push ,so can not send trigger request !!!", ErrorCode.EC_REQUEST_ERROR_TRIGGER);
            }
            return;
        }
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_REGISTER, "enter to the sendPushSyncTrigger .....");
        getSyncRegisterInfoSafely(new OnGetCallBack<SyncRegisterInfo>() {
            @Override
            public void onGet(SyncRegisterInfo syncRegisterInfo) {
                if (syncRegisterInfo == null) {
                    LogUtils.e(TAG, " syncRegisterInfo is null ");
                    return;
                }
                PushSyncTriggerRequestEntity entity = getRequestEntityFactory().
                        createPushSyncTriggerRequestEntity(alias, syncRegisterInfo.getRegisterId());

                LogUtils.d(TAG, "sendPushSyncTrigger alias --> " + alias);

                final Request request = Request.createRequest(PushApplication.this, entity);
                request.setNeedResponse(true);
                request.setNeedRetry(true);
                request.setOnReceiveListener(onResultListener);
                request.send();
            }
        });
    }

    public void requestPublicKey(OnReceiveListener onReceiveListener) {

        LogUtils.i(TAG, "enter to the requestPublicKey .....");

        PublicKeyRequestEntity entity = mRequestEntityFactory.createPublicKeyRequestEntity();
        Request request = Request.createRequest(this, entity);
        request.setOnReceiveListener(onReceiveListener);
        request.send();
    }

    public List<String> getServerInfoList() {
        return mServerInfoList;
    }

    /**
     * Class: PushApplication
     * Tag: 宿主选择
     * Ref：PushApplication.initVar() ConnectionServiceManager.initConnectionServiceIntent() 都在构造方法中
     * Fun: 获取宿主ServicePackageName，有TCP连接服务在运行就用现有的，不然以自己为宿主
     */
    public String getHostServicePackageName() {
        if (TextUtils.isEmpty(mHostServicePackageName)) {
            LogUtils.d(TAG, "host service package name is empty-----");
            mHostServicePackageName = PublicValueStoreUtil.getHostPackageName();
            if (!TextUtils.isEmpty(mHostServicePackageName)) {
                if (!AppUtil.isServiceRunning(mContext, mHostServicePackageName, ConnectionService.class.getName())) {
                    LogUtils.d(TAG, "old host service package name::" + mHostServicePackageName + " is not start!!");
                    mHostServicePackageName = mContext.getPackageName();
                    putHostServicePackageName(mHostServicePackageName);
                }
            } else {
                LogUtils.d(TAG, "get host service package name is empty-----");
                mHostServicePackageName = mContext.getPackageName();
                putHostServicePackageName(mHostServicePackageName);
            }
        }
        LogUtils.i(TAG, "the host service package name is:: " + mHostServicePackageName);
        return mHostServicePackageName;
    }

    /**
     * Class: PushApplication
     * Tag: 宿主选择
     * Ref：PushApplication.getHostServicePackageName() | ConnectionServiceManager.initConnectionServiceIntent() | ConnectionServiceManager.startService()
     * Fun: 保存宿主App的packageName到本地
     */
    public boolean putHostServicePackageName(String packageName) {
        return PublicValueStoreUtil.putHostPackageName(packageName);
    }

    public void release() {
        setOnInitSateListener(null);
    }

    private void setOnInitSateListener(OnInitSateListener l) {
        synchronized (LOCK_SATE_LISTENER) {
            this.mOnInitSateListener = l;
        }
    }

    private void callBackInitSateSuccess(){
        synchronized (LOCK_SATE_LISTENER) {
            if(mOnInitSateListener != null) {
                mOnInitSateListener.onSuccess();
            }
        }
    }

    private void callBackInitSateFail(String errorMsg, String errorCode){
        synchronized (LOCK_SATE_LISTENER) {
            if(mOnInitSateListener != null){
                this.mOnInitSateListener.onFail(errorMsg, errorCode);
            }
        }
    }

    public void initAnalyze() {
        if (mContext == null) {
            return;
        }
        if (homeKeyEventReceiver != null) {
            return;
        }
        LogUtils.w(TAG,"init push analyze ");
        homeKeyEventReceiver = new BroadcastReceiver() {
            String REASON = "reason";
            String HOMEKEY = "homekey";
            String RECENTAPPS = "recentapps";

            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    return;
                }
                String action = intent.getAction();
                if (TextUtils.equals(action, Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    String reason = intent.getStringExtra(REASON);
                    if (TextUtils.equals(reason, HOMEKEY)) {
                        // 点击 Home键
                        onMenuClicked();
                    } else if (TextUtils.equals(reason, RECENTAPPS)) {
                        // 点击 菜单键
                        onMenuClicked();
                    }
                }
            }
        };
        mContext.registerReceiver(homeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private BroadcastReceiver homeKeyEventReceiver = null;


    private long mLastClickTime = 0;
    private int mClickCnt = 0;


    private void onMenuClicked() {
        if (mLastClickTime == 0) {
            mLastClickTime = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - mLastClickTime <= 1000) {
            mClickCnt++;
            if (mClickCnt >= 10) {
                analyzePush();
                mLastClickTime = 0;
                mClickCnt = 0;
            }
        } else {
            mClickCnt = 0;
        }
        mLastClickTime = System.currentTimeMillis();
    }

    public String analyzePush() {
        Toast.makeText(mContext, "开始分析Push......", Toast.LENGTH_SHORT).show();

        String line = "\n============================================";

        /*获取静态信息*/
        String deviceId = DeviceUtils.getMachineId(mContext);
        PhoneDevice phoneDevice = new PhoneDevice(mContext);
        String mac = phoneDevice.getMacAddress();
        String imei = phoneDevice.getImei();
        String did = phoneDevice.getDeviceId();

        final String time = DateFormatUtil.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis());
        String appPkgName = mContext.getPackageName();
        String nativeSdkInfo = SDKVersion.getSdkInfo()/* + "\n应用AppKey: " + AppUtil.getAppKey(mContext)*/;/*SdkInfo + AppKey*/
        String nativeHostName = UrlConfig.sHostNameDef;

        String hostPkgName = PublicValueStoreUtil.getHostPackageName();

        String hostSdkInfo = mConnectionServiceManager.getHostSdkInfo();/*SdkInfo + AppKey*/
        String appBindInfo = mConnectionServiceManager.getOldAppBindInfo().toString();

        String hostName = mConnectionServiceManager.getHostName();//主机

        boolean isBind = mConnectionServiceManager.isBind();
        boolean isClosed = mConnectionServiceManager.isClosed();
        boolean isLogin = mConnectionServiceManager.isLogin();
        boolean isNetConnect = NetUtil.isConnectToNet(mContext);

        boolean isHostRight = nativeHostName.equalsIgnoreCase(hostName);


        String result = "机器序列号：" + deviceId + "  MAC: "+mac+"  IMEI: "+imei+ "  DeviceId: "+did+"\n应用App: " + appPkgName + "\n应用SDK: " + nativeSdkInfo + "\n应用Host: " + nativeHostName + line +
                "\n宿主APP: " + hostPkgName + "\n宿主SDK: " + hostSdkInfo + "\n 宿主已绑App:"+appBindInfo+"\n宿主Host:" + hostName + line + "\n是否绑定宿主：" + isBind + "\n是否关闭：" + isClosed
                + "\n是否登录：" + isLogin + "\n网络是否连接：" + isNetConnect;
        String AppUrlMode = "";
        if (nativeHostName.contains("test")) {
            AppUrlMode = "测试环境";
        } else {
            AppUrlMode = "正式环境";
        }
        String HostUrlMode = "";
        if (hostName == null) {
            HostUrlMode = "未绑定";
        }else {
            if (hostName.contains("test")) {
                HostUrlMode = "测试环境";
            } else {
                HostUrlMode = "正式环境";
            }
        }
        if (isHostRight) {
            result = result + "\n环境一致，正确... " + " >>>>  应用环境: " + AppUrlMode + "   宿主环境：" + HostUrlMode;
        } else {
            result = result + "\n环境不一致，错误!!! " + " >>>>  应用环境: " + AppUrlMode + "   宿主环境：" + HostUrlMode;
        }
        LogUtils.d(TAG, LogTagConfig.LOG_TAG_ST, result);

        final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/config/debug/bfc/push";
        final String finalResult = result;
        ExecutorsUtils.execute(new Runnable() {
            @Override
            public void run() {
                FileUtil.writeData2SDCard(FILE_PATH, "push-analyze-" + time + ".txt", finalResult);
            }
        });
        return result;
    }

    public DebugBasicInfo getBasicInfo() {
        String deviceId = DeviceUtils.getMachineId(mContext);
        PhoneDevice phoneDevice = new PhoneDevice(mContext);
        String mac = phoneDevice.getMacAddress();
        String imei = phoneDevice.getImei();
        String did = phoneDevice.getDeviceId();
        String hostPkgName = PublicValueStoreUtil.getHostPackageName();
        String hostSdkInfo = mConnectionServiceManager.getHostSdkInfo();
        String hostName = mConnectionServiceManager.getHostName();//主机
        boolean isNetConnect = NetUtil.isConnectToNet(mContext);
        DebugBasicInfo debugBasicInfo = new DebugBasicInfo(deviceId,mac,imei,did,hostPkgName,hostSdkInfo,hostName,isNetConnect+"");
        return debugBasicInfo;
    }

    public void addSyncRespMsgId(String msgId) {
        int size = mSyncRespMsgIdQueue.size();
        if (size >= 1000) {
            mSyncRespMsgIdQueue.poll();
        }
        mSyncRespMsgIdQueue.offer(msgId);
    }

    public boolean containsSyncRespMsgId(String msgId) {
        return mSyncRespMsgIdQueue.contains(msgId);
    }

    public void callBackPushStatus(int status, Object ...values){
        if(mOnPushStatusListener != null){
            LogUtils.v(TAG, "onPushStatus call back status:" + status);
            mOnPushStatusListener.onPushStatus(status, values);
        }
    }

    /**
     * 设置推送状态监听广播
     * @param onPushStatusListener
     */
    public void setOnPushStatusListener(OnPushStatusListener onPushStatusListener){
        mOnPushStatusListener = onPushStatusListener;
    }

}


