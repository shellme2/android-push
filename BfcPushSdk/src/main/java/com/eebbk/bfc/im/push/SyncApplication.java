package com.eebbk.bfc.im.push;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.bean.AliasAndTagsInfo;
import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncTriggerRequestEntity;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnStopResumeListener;
import com.eebbk.bfc.im.push.request.SetAliasAndTagRequest;
import com.eebbk.bfc.im.push.communication.ConnectionServiceManager;
import com.eebbk.bfc.im.push.service.dispatcher.SyncNotificationManager;
import com.eebbk.bfc.im.push.util.SettingStoreUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.bean.PushCollectInfo;
import com.eebbk.bfc.im.push.bean.SyncRegistInfo;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.LoginRequestEntity;
import com.eebbk.bfc.im.push.entity.request.PublicKeyRequestEntity;
import com.eebbk.bfc.im.push.entity.request.RegistRequestEntity;
import com.eebbk.bfc.im.push.entity.request.RequestEntityFactory;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnPushCollectListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.listener.OnServiceConnectionListener;
import com.eebbk.bfc.im.push.listener.OnSpecialConnectListener;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;
import com.eebbk.bfc.im.push.util.platform.Platform;
import com.eebbk.bfc.im.push.util.platform.PhonePlatform;
import com.eebbk.bfc.im.push.request.LoginRequest;
import com.eebbk.bfc.im.push.request.RegistRequest;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.request.RequestManager;
import com.eebbk.bfc.im.push.request.RequestSweeper;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.ResponseDispatcher;
import com.eebbk.bfc.im.push.response.SyncKeyManager;
import com.eebbk.bfc.im.push.util.IDUtil;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.eebbk.bfc.im.push.util.platform.Store;
import com.eebbk.bfc.im.push.version.VersionConfig;
import com.eebbk.bfc.im.push.voice.SliceReceiverManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * IM SDK整体环境
 */
public class SyncApplication {

    private static final String TAG=SyncApplication.class.getName();

    private static final String HOSTNAME_DEF ="testgw.im.okii.com";
    private static final int PORT_DEF =28000;

    private static SyncApplication mInstance=null;

    private Context mContext;

    private String mCreateServicePackageName;

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
     * 语音切片接收管理器
     */
    private SliceReceiverManager mSliceReceiverManager;

    /**
     * 连接服务管理
     */
    private ConnectionServiceManager mConnectionServiceManager;

    private Platform mPlatform;

    private String mPackageName;

    /**
     * sdk版本号
     */
    private int mSyncSDKVersionCode = VersionConfig.VERSION_CODE;

    private String mSyncSDKVersionName = VersionConfig.VERSION_NAME;

    private SyncRegistInfo mSyncRegistInfo;

    private List<String> mServerInfoList;

    /**
     * 连接是否可用，一般只要成功连接上一次就认为连接是可用的
     */
    private boolean mConnectionEnabled;

    /**
     * 初始化回调监听
     */
    private OnInitSateListener mOnInitSateListener;

    private boolean mInitCalled;

    /**
     * 推送状态监听
     */
    private static OnPushStatusListener mOnPushStatusListener;

    /**
     * 推送数据信息搜集监听
     */
    private static OnPushCollectListener mOnPushCollectListener;

    private Queue<String> mSyncRespMsgIdQueue;

    private String mAlias;

    private String mHostServicePackageName;

    private String mHostname =HOSTNAME_DEF;
    private int mPort =PORT_DEF;

    private PushConfig mConfing =null;

    private OnServiceConnectionListener mOnServiceConnectionListener = new OnServiceConnectionListener() {
        @Override
        public void onServiceConnected() {
            getSyncRegistInfoSafelyImpl(null);
        }

        @Override
        public void onServiceDisconnected() {
            SyncApplication.this.mSyncRegistInfo = null;
        }
    };

    private SyncApplication(Context ctx) {
        initVar(ctx);
        IDUtil.init(this);
    }

    static void initInstance(Context context){
        if(mInstance==null){
            synchronized (SyncApplication.class){
                if(mInstance==null){
                    mInstance=new SyncApplication(context);
                }
            }
        }
    }

    public static SyncApplication getInstance(){
        if(mInstance==null){
             throw new RuntimeException("没有初始化环境变量");
        }
        return mInstance;
    }

    private void initVar(Context ctx) {
        this.mContext = ctx.getApplicationContext() != null ? ctx.getApplicationContext() : ctx;
        mPackageName = mContext.getPackageName();
        mServerInfoList = new ArrayList<>();
        mSyncRespMsgIdQueue = new ConcurrentLinkedQueue<>();
        mPlatform = new PhonePlatform(mContext);  // 创建手机平台工具类

        mSyncKeyManager = new SyncKeyManager(this);
        mRequestManager = new RequestManager(this);    // request请求管理
        mRequestEntityFactory = new RequestEntityFactory(this);    // request entity创建工厂
        mSliceReceiverManager = new SliceReceiverManager();
        mDispatcher = new ResponseDispatcher(this);    //response分发
        mRequestSweeper = new RequestSweeper(this);  //创建Request定时清理器
        mRequestSweeper.start();

        mConnectionServiceManager = new ConnectionServiceManager(this, mOnServiceConnectionListener);
        mHostServicePackageName = getHostServicePackageName();
    }

    /**
     * 初始化SDK环境
     */
    public void init(OnInitSateListener onInitSateListener) {
        this.mOnInitSateListener = onInitSateListener;
        start(false);
    }

    /**
     * 添加预埋ip
     */
    public void addBackupServerInfo(String[] backupServerInfo, boolean clearBefore) {
        if (backupServerInfo == null || backupServerInfo.length == 0) {
            LogUtils.w("serverInfo is empty.");
            return;
        }
        if (clearBefore) {
            mServerInfoList.clear();
        }
        for(String s : backupServerInfo) {
            String[] ip_port = s.split(":");
            if (ip_port.length != 2) {
                continue;
            }
            try {
                String m_ip = ip_port[0];
                int m_port = Integer.parseInt(ip_port[1]);
                updateServerInfo(m_ip, m_port);
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }
        LogUtils.i("mServerInfoList:" + mServerInfoList);
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
        LogUtils.d("start to reconnect to server...");
        if (mConnectionServiceManager.isShutdown()) {
            return;
        }
        start(true);
    }

    public void exit() {
        mInitCalled = false;
        mConnectionServiceManager.clearBackupServerInfo();
        mConnectionServiceManager.shutdown();
        mServerInfoList.clear();
        mRequestSweeper.cancel();
        mSyncKeyManager.clear();
        mRequestManager.clear();
        mDispatcher.stop();
        mSyncRespMsgIdQueue.clear();
        SyncNotificationManager.getSyncNotificationManager().clear();
        mCreateServicePackageName = null;
        mSyncRegistInfo = null;
    }

    public boolean isClosed() {
        return mConnectionServiceManager.isClosed();
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

    public String getCreateServicePackageName() {
        return mCreateServicePackageName;
    }

    public void setCreateServicePackageName(String mCreateServicePackageName) {
        this.mCreateServicePackageName = mCreateServicePackageName;
    }

    public SliceReceiverManager getSliceReceiverManager() {
        return mSliceReceiverManager;
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
        return mPackageName;
    }

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public int getSyncSDKVersionCode() {
        return mSyncSDKVersionCode;
    }

    public String getSyncSDKVersionName() {
        return mSyncSDKVersionName;
    }

    public SyncKeyManager getSyncKeyManager() {
        return mSyncKeyManager;
    }

    public Context getContext() {
        return mContext;
    }

    public String getAlias(){
        return mAlias;
    }

    public void callOnInitSuccessListener() {
        if (mInitCalled) {
            LogUtils.w("on init success listener was called");
            return;
        }
        mInitCalled = true;
        if (mOnInitSateListener != null) {
            mOnInitSateListener.onSuccess();
        }
        LogUtils.d("call on init success listener");
    }

    public boolean isLogined() {
        return mConnectionServiceManager.isLogined();
    }

    public boolean hasPublicKey() {
        byte[] publicKey = StoreUtil.readPublicKey(mPlatform.getStore());
        if (publicKey != null && publicKey.length > 0) {
            return true;
        } else {
            return mConnectionServiceManager.hasPublicKey();
        }
    }

    public void savePublicKey(byte[] publicKey) {
        if (publicKey == null || publicKey.length == 0) {
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

    public void setHostname(String hostname){
        this.mHostname=hostname;
    }

    public void setPort(int port){
        this.mPort=port;
    }

    public String getRealHostname() {
        return mConnectionServiceManager.getRealHostname();
    }

    public int getRealPort() {
        return mConnectionServiceManager.getRealPort();
    }

    public void setHeartbeatPeriod(int minHeart, int maxHeart, int heartStep) {
        mConnectionServiceManager.setHeartbeatPeriod(minHeart * 1000, maxHeart * 1000, heartStep * 1000);
    }

    public boolean isConnectionEnabled() {
        return mConnectionEnabled;
    }

    public void setConnectionEnabled(boolean mConnectionEnabled) {
        this.mConnectionEnabled = mConnectionEnabled;
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

    public void reLogin() {
        mConnectionServiceManager.reLogin();
    }

    public static boolean checkRegistInfo(SyncRegistInfo syncRegistInfo) {
        if (syncRegistInfo == null) {
            return false;
        }
        if (syncRegistInfo.getRegistId() == 0) {
            return false;
        }
        if (TextUtils.isEmpty(syncRegistInfo.getRegistToken())) {
            return false;
        }
        if (!syncRegistInfo.isLogin()) {
            return false;
        }
        return true;
    }

    public boolean isRegisted() {
        SyncRegistInfo syncRegistInfo = getmSyncRegistInfo();
        return syncRegistInfo.getRegistId() != 0;
    }

    public void clearSyncRegistInfo() {
        mSyncRegistInfo = null;
    }

    public SyncRegistInfo getmSyncRegistInfo() {
        if (checkRegistInfo(mSyncRegistInfo)) {
            return mSyncRegistInfo;
        }
        SyncRegistInfo tmp = mConnectionServiceManager.getSyncRegistInfo();
        if (checkRegistInfo(tmp)) {
            mSyncRegistInfo = tmp;
        }
        return tmp;
    }

    public void getSyncRegistInfoSafely(final OnGetCallBack<SyncRegistInfo> onGetCallBack) {
        if (checkRegistInfo(mSyncRegistInfo)) {
            LogUtils.i("get cache sync regist info:" + mSyncRegistInfo);
            onGetCallBack.onGet(mSyncRegistInfo);
            return;
        }
        getSyncRegistInfoSafelyImpl(onGetCallBack);
    }


    private void getSyncRegistInfoSafelyImpl(final OnGetCallBack<SyncRegistInfo> onGetCallBack) {
        mConnectionServiceManager.getSyncRegistInfoSafely(new OnGetCallBack<SyncRegistInfo>() {
            @Override
            public void onGet(SyncRegistInfo syncRegistInfo) {
                if (checkRegistInfo(syncRegistInfo)) {
                    SyncApplication.this.mSyncRegistInfo = syncRegistInfo;
                    LogUtils.i("get sync regist info success:" + syncRegistInfo);
                }
                if (onGetCallBack != null) {
                    onGetCallBack.onGet(syncRegistInfo);
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

    public void regist() {
        List<Request> list = mRequestManager.search(Command.REGIST_REQUEST);
        if (list.size() > 0) {
            LogUtils.w("the regist request is on going,do not execute another regist request...");
            return;
        }
        RegistRequestEntity entity = mRequestEntityFactory.createRegistRequestEntity();
        LogUtils.d("to regist,entity:" + entity);
        RegistRequest registRequest = new RegistRequest(this, entity, new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    LogUtils.i("register success!!!");
                } else {
                    LogUtils.e("register failed:" + response.getResponseEntity());
                }
            }
        });
        registRequest.send();
    }

    public void login() {
        List<Request> list = mRequestManager.search(Command.LOGIN_REQUEST);
        if (list.size() > 0) {
            LogUtils.w("the login request is on going,do not execute another login request...");
            return;
        }
        getSyncRegistInfoSafely(new OnGetCallBack<SyncRegistInfo>() {
            @Override
            public void onGet(SyncRegistInfo syncRegistInfo) {
                LoginRequestEntity entity = mRequestEntityFactory.createLoginRequestEntity(syncRegistInfo.getRegistId(), syncRegistInfo.getRegistToken());
                LogUtils.d("to login,entity:" + entity);
                LoginRequest loginRequest = new LoginRequest(SyncApplication.this, entity, new OnReceiveListener() {
                    @Override
                    public void onReceive(Request request, Response response) {
                        if (response.isSuccess()) {
                            LogUtils.i("login success!!!");
                        } else {
                            LogUtils.e("login failed:" + response.getResponseEntity());
                        }
                    }
                });
                loginRequest.send();
            }
        });
    }

    public void setStopPush(final OnStopResumeListener onStopResumeListener){
        List<Request> list = mRequestManager.search(Command.PUSH_ALIAS_AND_TAG_REQUEST);
        if (list.size() > 0) {
            LogUtils.w("the setStopPush request is on going,do not execute another login request...");
            return;
        }

        getSyncRegistInfoSafely(new OnGetCallBack<SyncRegistInfo>() {
            @Override
            public void onGet(SyncRegistInfo syncRegistInfo) {
                AliasAndTagsRequestEntity entity = getRequestEntityFactory().
                        createAliasAndTagRequestEntity("", null, syncRegistInfo.getRegistId());

                SetAliasAndTagRequest setAliasAndTagRequest = new SetAliasAndTagRequest(SyncApplication.this, entity,
                        new OnReceiveListener() {
                    @Override
                    public void onReceive(Request request, Response response) {
                        if(response.isSuccess()){
                            mAlias="";
                            StoreUtil.saveIsStopPush(new PhoneStore(mContext),true);
                        }else{
                            LogUtils.e("stop push fail !!!");
                        }
                        if(onStopResumeListener!=null){
                            onStopResumeListener.onReceive(request,response);
                        }
                    }
                });
                setAliasAndTagRequest.send();
            }
        });

    }

    public void setResumePush(final OnStopResumeListener onStopResumeListener){
        List<Request> list = mRequestManager.search(Command.PUSH_ALIAS_AND_TAG_REQUEST);
        if (list.size() > 0) {
            LogUtils.w("the setResumePush request is on going,do not execute another login request...");
            return;
        }

        final Store store=new PhoneStore(mContext);
        final AliasAndTagsInfo aliasAndTagsInfo=StoreUtil.readAliasAndTag(store);

        LogUtils.i("setResumePush read AliasAndTag isSet=="+aliasAndTagsInfo.isSet());
        LogUtils.i("setResumePush read AliasAndTag alias=="+aliasAndTagsInfo.getAlias());
        LogUtils.i("setResumePush read AliasAndTag tags=="+aliasAndTagsInfo.getTags());

        if(TextUtils.isEmpty(aliasAndTagsInfo.getAlias())){
            aliasAndTagsInfo.setAlias(Build.SERIAL);
        }

        getSyncRegistInfoSafely(new OnGetCallBack<SyncRegistInfo>() {
            @Override
            public void onGet(SyncRegistInfo syncRegistInfo) {
                AliasAndTagsRequestEntity entity = getRequestEntityFactory().
                        createAliasAndTagRequestEntity(aliasAndTagsInfo.getAlias(),
                                aliasAndTagsInfo.getTagsList(), syncRegistInfo.getRegistId());

                SetAliasAndTagRequest setAliasAndTagRequest = new SetAliasAndTagRequest(SyncApplication.this, entity,
                        new OnReceiveListener() {
                            @Override
                            public void onReceive(Request request, Response response) {
                                if(response.isSuccess()){
                                    mAlias=aliasAndTagsInfo.getAlias();
                                    StoreUtil.saveIsStopPush(store,false);
                                }else{
                                    LogUtils.e("resume push fail !!!");
                                }
                                if(onStopResumeListener!=null){
                                    onStopResumeListener.onReceive(request,response);
                                }
                            }
                        });
                setAliasAndTagRequest.send();
            }
        });
    }

    public void setAliasAndTagRequest(String alias, List<String> tags, final OnReceiveListener onReceiveListener) {
        List<Request> list = mRequestManager.search(Command.PUSH_ALIAS_AND_TAG_REQUEST);
        if (list.size() > 0) {
            LogUtils.w("the setAliasAndTagRequest request is on going,do not execute another login request...");
            return;
        }

        final AliasAndTagsInfo aliasAndTagsInfo=checkAliasAndTags(alias,tags);

        if(aliasAndTagsInfo==null){
            LogUtils.d("the setAliasAndTagRequest  is seted.....");
            sendPushSyncTrigger(null);
            return;
        }

        getSyncRegistInfoSafely(new OnGetCallBack<SyncRegistInfo>() {
            @Override
            public void onGet(SyncRegistInfo syncRegistInfo) {
                AliasAndTagsRequestEntity entity = getRequestEntityFactory().
                        createAliasAndTagRequestEntity(aliasAndTagsInfo.getAlias(),
                                aliasAndTagsInfo.getTagsList(), syncRegistInfo.getRegistId());//别名默认为序列号

                LogUtils.i("setAliasAndTagRequest alias and tag--> "+aliasAndTagsInfo.getAlias()+"::"+aliasAndTagsInfo.getTags());

                SetAliasAndTagRequest setAliasAndTagRequest = new SetAliasAndTagRequest(
                        SyncApplication.this, entity, onReceiveListener);
                setAliasAndTagRequest.send();
            }
        });

    }

    private AliasAndTagsInfo checkAliasAndTags(String alias,List<String> tags){

//        LogUtils.i("set AliasAndTag alias=="+alias);
//        LogUtils.i("set AliasAndTag tags=="+tags);

        Store store=new PhoneStore(mContext);
        AliasAndTagsInfo aliasAndTagsInfo=StoreUtil.readAliasAndTag(store);

        LogUtils.i("read AliasAndTag isSet=="+aliasAndTagsInfo.isSet());
        LogUtils.i("read AliasAndTag alias=="+aliasAndTagsInfo.getAlias());
        LogUtils.i("read AliasAndTag tags=="+aliasAndTagsInfo.getTags());

        if(aliasAndTagsInfo.isSet()){
            if(TextUtils.isEmpty(alias)&&(tags==null||tags.isEmpty())){
                mAlias=aliasAndTagsInfo.getAlias();
                return null;
            }
            if(!TextUtils.isEmpty(alias)){
                aliasAndTagsInfo.setAlias(alias);
            }
            if(tags!=null&&!tags.isEmpty()){
                aliasAndTagsInfo.setTags(tags);
            }
        }else{
            if(alias==null){
                alias=Build.SERIAL;
            }
            aliasAndTagsInfo.setAlias(alias);
            aliasAndTagsInfo.setTags(tags);
        }

        mAlias=aliasAndTagsInfo.getAlias();
        return aliasAndTagsInfo;
    }

    public void sendPushSyncTrigger( final OnReceiveListener onReceiveListener) {

        getSyncRegistInfoSafely(new OnGetCallBack<SyncRegistInfo>() {
            @Override
            public void onGet(SyncRegistInfo syncRegistInfo) {
                PushSyncTriggerRequestEntity entity =getRequestEntityFactory().
                        createPushSyncTriggerRequestEntity(mAlias, syncRegistInfo.getRegistId());

                LogUtils.i("sendPushSyncTrigger alias --> "+mAlias);

                final Request request = Request.createRequest(SyncApplication.this, entity);
                request.setNeedResponse(true);
                request.setNeedRetry(true);
                request.setOnReceiveListener(onReceiveListener);
                request.send();

            }
        });
    }


    public void requestPublicKey(OnReceiveListener onReceiveListener) {
        PublicKeyRequestEntity entity = mRequestEntityFactory.createPublicKeyRequestEntity();
        Request request = Request.createRequest(this, entity);
        request.setOnReceiveListener(onReceiveListener);
        request.send();
    }

    public static void setOnPushStatusListener(OnPushStatusListener mOnPushStatusListener) {
        SyncApplication.mOnPushStatusListener = mOnPushStatusListener;
    }

    public static void callOnPushConnectStatus(int connectStatus) {
        if (mOnPushStatusListener != null) {
            mOnPushStatusListener.onConnectStatus(connectStatus);
        }
    }

    public static void callOnPushLogin(long registerId) {
        if (mOnPushStatusListener != null) {
            mOnPushStatusListener.onLogin(registerId);
        }
    }

    public static void setOnPushCollectListener(OnPushCollectListener mOnPushCollectListener) {
        SyncApplication.mOnPushCollectListener = mOnPushCollectListener;
    }

    public static void callOnPushCollect(PushCollectInfo pushCollectInfo) {
        if (mOnPushCollectListener != null) {
            mOnPushCollectListener.onCollect(pushCollectInfo);
        }
    }

    public void specialConnect(String hostInfo, OnSpecialConnectListener onSpecialConnectListener) {
        if (TextUtils.isEmpty(hostInfo)) {
            LogUtils.e("hostInfo is null");
            if (onSpecialConnectListener != null) {
                onSpecialConnectListener.onError("hostInfo is null");
            }
            return;
        }
        String[] ip_port = hostInfo.split(":");
        if (ip_port.length != 2) {
            LogUtils.e("hostInfo format is wrong,right format is xx.xx.xx.xx:mPort");
            if (onSpecialConnectListener != null) {
                onSpecialConnectListener.onError("hostInfo format is wrong,right format is xx.xx.xx.xx:mPort");
            }
            return;
        }
        try {
            String m_ip = ip_port[0];
            int m_port = Integer.parseInt(ip_port[1]);
            if (!TextUtils.isEmpty(m_ip) && m_port > 0 && m_port <= 65535) {
                mConnectionServiceManager.specialConnectServer(m_ip, m_port, onSpecialConnectListener);
            }
        } catch (Exception e) {
            LogUtils.e(e);
            if (onSpecialConnectListener != null) {
                onSpecialConnectListener.onError(e.toString());
            }
        }
    }

    public List<String> getServerInfoList() {
        return mServerInfoList;
    }

    /**
     *获取宿主ServicePackageName，有TCP连接服务在运行就用现有的，不然以自己为宿主
     */
    public String getHostServicePackageName(){
        if(TextUtils.isEmpty(mHostServicePackageName)){
            LogUtils.w(TAG,"宿主包名为空-----");
            mHostServicePackageName = SettingStoreUtil.getHostPackgName(mContext);
            if(!TextUtils.isEmpty(mHostServicePackageName)){
                if(!isPackageWork(mContext,mHostServicePackageName)){
                LogUtils.w(TAG,"旧宿主包名为-----"+mHostServicePackageName+" 没有启动");
                    mHostServicePackageName=mContext.getPackageName();
                    putHostServicePackageName(mHostServicePackageName);
                }
            }else{
            LogUtils.w(TAG,"获取。。宿主包名为空-----");
                mHostServicePackageName=mContext.getPackageName();
                putHostServicePackageName(mHostServicePackageName);
            }
        }
        LogUtils.w(TAG,"宿主包名为-----"+mHostServicePackageName);
        return mHostServicePackageName;
    }

    /**
     * 判断某个服务是否正在运行的方法
     */
    private boolean isPackageWork(Context mContext, String packageName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
        if (myList.size() <= 0) {
            return false;
        }

        int size= myList.size();
        for (int i = 0; i < size; i++) {
            if(myList.get(i).service.getPackageName().equals(packageName)){
                isWork = true;
                break;
            }

        }
        return isWork;
    }

    public boolean putHostServicePackageName(String packageName){
        return SettingStoreUtil.putHostPackgName(mContext,packageName);
    }

    public PushConfig getPushConfig(){
        return mConfing;
    }

    public void setPushConfig(PushConfig config){
        this.mConfing=config;
    }

}


