package com.eebbk.bfc.im.push.communication;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.IConnectionService;
import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.PushImplements;
import com.eebbk.bfc.im.push.SDKVersion;
import com.eebbk.bfc.im.push.bean.AppBindInfo;
import com.eebbk.bfc.im.push.bean.AppPushInfo;
import com.eebbk.bfc.im.push.bean.PandaAppInfo;
import com.eebbk.bfc.im.push.bean.SyncRegisterInfo;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnServiceConnectionListener;
import com.eebbk.bfc.im.push.panda.PandaAppManager;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.heartbeat.ConnectSwitchService;
import com.eebbk.bfc.im.push.service.host.HostElectionHandleService;
import com.eebbk.bfc.im.push.service.host.HostServiceElection;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.ExecutorsUtils;
import com.eebbk.bfc.im.push.util.JsonUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NetUtil;
import com.eebbk.bfc.im.push.util.StoreUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 对push进程维护连接的service提供aidl接口，原则上aidl接口的
 * 调用要越少越好，所以可以通过缓存的方式来降低aidl接口的调用次数
 */
public class ConnectionServiceManager {

    private static final String TAG = "ConnectionServiceManager";
    private static final int BIND_IM_SERVER_STATE_SUCCESS = 1;
    private static final int BIND_IM_SERVER_STATE_FAILED = 2;
    private static final int BIND_IM_SERVER_STATE_UPDATE = 3;

    /**
     * SDK全局环境
     */
    private PushApplication app;

    private AIDLHelper aidlHelper;

    /**
     * tcp连接服务的aidl接口对象
     */
    private volatile IConnectionService iConnectionService;

    private IConnServiceConnection iConnServiceConnection;

    private Context context;

    /**
     * TCP连接服务启动的Intent
     */
    private volatile Intent serviceIntent;

    private volatile long reLoginTime;

    private volatile boolean starting;

    private volatile boolean isBindSuccess;

    private volatile boolean shutdown;

    private OnServiceConnectionListener onServiceConnectionListener;

    private HostServiceElection hostServiceElection;

    private volatile String oldHostPkgName;
    /**
     * 老宿主的绑定App列表信息
     */
    private volatile AppBindInfo mOldAppBindInfo;

    public ConnectionServiceManager(PushApplication app, OnServiceConnectionListener onServiceConnectionListener) {
        this.context = app.getContext();
        this.app = app;
        this.onServiceConnectionListener = onServiceConnectionListener;
        iConnServiceConnection = new IConnServiceConnection();
        initConnectionServiceIntent();
        aidlHelper = new AIDLHelper(this);
        hostServiceElection = new HostServiceElection(this, context);
    }

    private void initConnectionServiceIntent() {
        String packageName=app.getHostServicePackageName();
        if(packageName!=null){
            serviceIntent = ConnectionService.createServiceIntent(context, packageName);
        }else{
            String temp=context.getPackageName();
            serviceIntent = ConnectionService.createServiceIntent(context, temp);
            if(app.putHostServicePackageName(temp)){
                LogUtils.i(TAG,"host service package name store success is " + temp);
            }else {
                LogUtils.e(TAG,"host service package name store fail ,please check!!" );
            }
        }
        LogUtils.i(TAG,"service intent component package:" + serviceIntent.getComponent().getPackageName());
    }

    /**
     * Class: ConnectionServiceManager
     * Tag: 宿主选择
     * Ref: PushApplication.start(), AIDLHelp.retryStart() , HostServiceElection.electHostService()
     * Fun: 开启IM连接   App初始化  启动&绑定宿主IM服务的关键方法
     */
    public void startConnect(boolean reconnect) {
        if (NetUtil.isConnectToNet(context) && NetUtil.isDianXinAnd2GNet(context)) {
            LogUtils.w(TAG,"The net is DianXin and 2G , We cannot start IM Connect this time !!! " );
            return;
        }
        synchronized (this) {
            if (starting) {
                LogUtils.w(TAG,"ConnectionServiceManager is starting: starting=" + starting);
                return;
            }
            starting = true;
        }
        shutdown = false;
        LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"开启IM连接,启动IM服务"," Is reconnect? reconnect="+reconnect);

        if (startService()/*启动IM服务*/) {
            if (bindService(serviceIntent)) {
                if (reconnect) {
                    connectServer();
                }
            }
        }
//        imServiceHostCheck();
        starting = false;
    }

    private void imServiceHostCheck() {
        hostServiceElection.checkUpOwnService(context);
        hostServiceElection.checkUpHostService(context);
    }

    public void electHostService() {
        hostServiceElection.electHostService(context);
    }

    public void checkUpHostService() {
        hostServiceElection.checkUpHostService(context);
    }

    public boolean isStarting() {
        return starting;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public Intent getServiceIntent() {
        return serviceIntent;
    }

    public void setServiceIntent(Intent serviceIntent) {
        this.serviceIntent = serviceIntent;
    }

    /**
     * Class: ConnectionServiceManager
     * Tag: 宿主选择
     * Ref: ConnectionServiceManager.startConnect()
     * Fun: 启动serviceIntent，如果启动准备好的service失败，则启动自己本身，如果成功则自己成为宿主，并且保存packageName到本地
     */
    private boolean startService() {
        boolean started = false;
        ComponentName componentName = null;
        try {
            componentName = context.startService(serviceIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (componentName != null) {
            started = true;
            LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"start connection service successfully,componentName " + componentName);
        } else {
            // 6.0系统禁止互相唤醒后启动远程服务会失败,所以只能启动本地服务
            LogUtils.e(TAG,"start connection service fail,the device may forbid to start remote service," +
                    "to start local service:" + context.getPackageName());
            serviceIntent = ConnectionService.createServiceIntent(context, context.getPackageName());
            ComponentName cn = context.startService(serviceIntent);
            if (cn != null) {
                started = true;
                String temp=cn.getPackageName();
                LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"start local connection service success:" + temp);
                if(app.putHostServicePackageName(temp)){
                    LogUtils.d(TAG,"host service package name change success is " + temp);
                }else {
                    LogUtils.e(TAG,"host service package name store fail ,please check!!" );
                }
            } else {
                LogUtils.e(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"start local connection service fail...");
            }
        }
        return started;
    }

    private synchronized boolean bindService(Intent intent) {
        boolean isBindSuccess = context.bindService(intent, iConnServiceConnection, Service.BIND_AUTO_CREATE);
        ComponentName cn = intent.getComponent();
        String bindPkg = null;
        if (cn != null) {
            bindPkg = cn.getPackageName();
        }
        //Todo 暂时在此处处理  无论成败 都改下这个标志位！！！
        PushImplements.isIniting = false;
        LogUtils.e(TAG,"after bind service ,failed or success ,just set isIniting to "+PushImplements.isIniting );

        if (isBindSuccess) {
            LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,context.getPackageName()+ "bind connection service successfully,bind pkgName:" + bindPkg);
        } else {
            LogUtils.e(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,context.getPackageName()+"bind connection service fail,the device may forbid to bind remote service,bind pkgName:"+ bindPkg);
        }
        return isBindSuccess;
    }


    private boolean sdkVersionSyncCheck() {
        //首先判断是否为启动自身的im服务
        try {
            String hostSdkInfo = getHostSdkInfo();
            SDKVersion.SdkInfo sdkInfo = JsonUtil.fromJson(hostSdkInfo, SDKVersion.SdkInfo.class);


            int hostSdkVersionCode = sdkInfo == null ? 0 : sdkInfo.getSdkVersionCode();
            int localSdkVersionCode = SDKVersion.getSDKInt();
            LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, "hostSdkVersionCode: " + hostSdkVersionCode + " >>> localSdkVersionCode" + localSdkVersionCode);
            if (localSdkVersionCode > hostSdkVersionCode) {
                // 说明本地SDK版本高于已经启动的宿主版本，then 需要把宿主切换到本地
                String servicePackageName = serviceIntent.getComponent().getPackageName();
                LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, " 宿主切换  >>>  " + servicePackageName + "  to " + context.getPackageName());
                //todo 老宿主校验
                oldHostPkgName = sdkInfo == null ? "" : sdkInfo.getAppName();//老宿主AppName
                if (!TextUtils.isEmpty(oldHostPkgName) && oldHostPkgName.equalsIgnoreCase(servicePackageName)) {
                    LogUtils.v(TAG, " 宿主切换 校验 老宿主为：" + oldHostPkgName);
                } else {
                    LogUtils.e(TAG, " 宿主切换 校验 警告！！！ 老宿主有问题  oldHostPkgName = " + oldHostPkgName + " --- serviceIntent.getPackageName = " + servicePackageName);
                }
                mOldAppBindInfo = getOldAppBindInfo();
                //检查是否有家长管控
                if (mOldAppBindInfo != null && mOldAppBindInfo.getBindAppMap() != null) {
                    Map<String, AppPushInfo> appPushInfoMap = mOldAppBindInfo.getBindAppMap();
                    List<PandaAppInfo> pandaList = PandaAppManager.getInstance().getPandaApps();
                    for (PandaAppInfo pandaApp : pandaList) {
                        if (!appPushInfoMap.containsKey(pandaApp.getPackageName())) {
                            //单独兼容需要把家长管理（因为无启动桌面图标），这个很重要！！！
                            //这个地方拉起来可能还是绑定的老宿主，如果是5.0.0以后的版本，后续可以切换到新宿主
                            PandaAppManager.getInstance().notifyPandaAppTurnOn(context, pandaApp);
                        }
                    }
                }
                return true;
            } else {
                LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, " 宿主切换  不切換  >>> 当前宿主：  " + serviceIntent.getComponent().getPackageName() + "  当前应用： " + context.getPackageName());
                return false;
            }
        } catch (Exception e) {
            PandaAppManager.getInstance().notifyAllPandaApp(context, true);// 兼容getHostSdkInfo接口，部分老版本发出去没有这个接口，单独兼容需要把家长管理（因为无启动桌面图标），这个很重要！！！
            LogUtils.e(TAG, e);
            return true;
        }
    }

    private void killOldHostApp() {
        // 远程Kill老宿主的push进程
//        int pushPid = AppUtil.getPidByName(context, "com.eebbk.bfc.demo.push");
//        int pushPid1 = AppUtil.getPidByName(context, "com.eebbk.bfc.demo.push_1");
        if(TextUtils.isEmpty(oldHostPkgName)){
            LogUtils.d(TAG,"宿主切换 killOldHostApp()：" + " oldHostPkgName = " + oldHostPkgName);
            return;
        }

        List<Integer> imPid = AppUtil.getPidByName(context, "eebbk.push");

        List<Integer> oldHostPid = AppUtil.getPidByName(context, oldHostPkgName);
        int currentPid = Process.myPid();
        LogUtils.d(TAG,"宿主切换 杀掉老宿主：" + " currentPid=" + currentPid + "  oldHostPkgName = "+oldHostPkgName);

        imPid.remove(new Integer(currentPid));
        oldHostPid.remove(new Integer(currentPid));

        for (int pid : imPid) {
            LogUtils.d(TAG,"宿主切换 杀掉老宿主：" + " imPid=" + pid);
            turnOff(oldHostPkgName, pid);
        }

        for (int pid : oldHostPid) {
            LogUtils.d(TAG,"宿主切换 杀掉老宿主：" + " oldHostPid=" + pid);
            turnOff(oldHostPkgName, pid);
        }
    }

    private void turnOff(String appName, int pid) {
        if(TextUtils.isEmpty(appName)){
            return;
        }
        try {
            Intent intent = new Intent();
            intent.setAction(SyncAction.CONNECT_SWITCH_SERVICE_ACTION);
//        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app 死了就不用再發了
            intent.putExtra(ConnectSwitchService.BUNDLE_KEY_SERVICE_SWITCH, ConnectSwitchService.BUNDLE_VALUE_SERVICE_SWITCH_OFF);
            //不能直接杀，有些会自启动的app会死循环起来被杀起来被杀
//            intent.putExtra(ConnectSwitchService.BUNDLE_KEY_PID, pid);
            intent.setComponent(new ComponentName(appName, ConnectSwitchService.class.getName()));
            context.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 切换宿主app为自己
     */
    private void changeHostSdk() {
        syncRegisterByOldHost();
        String temp = context.getPackageName();
        serviceIntent = ConnectionService.createServiceIntent(context,temp);
        if(app.putHostServicePackageName(temp)){
            LogUtils.i(TAG,"宿主切换 host service package name store success is " + temp);
        }else {
            LogUtils.e(TAG,"宿主切换 host service package name store fail ,please check!!" );
        }

        starting = false;
        startConnect(false);//TODO 潜在问题：存在死锁的问题 ，存在死循环的问题
        killOldHostApp();//todo 这个地方杀死老宿主
    }

    /**
     * 同步老宿主的注册信息
     */
    private void syncRegisterByOldHost(){
        LogUtils.i(TAG,"宿主切换 syncRegisterByOldHost()" );
        SyncRegisterInfo syncRegisterInfo = getSyncRegisterInfo();
        if (PushApplication.checkRegisterInfo(syncRegisterInfo)) {
            LogUtils.i(TAG,"宿主切换 syncRegisterInfo:" + syncRegisterInfo);
            StoreUtil.saveRegisterInfo(app.getPlatform().getStore(), syncRegisterInfo);
        }else {
            LogUtils.i(TAG,"宿主切换 syncRegister fail");
        }
    }

    private synchronized void notifyOldBindAppList() {
        if (mOldAppBindInfo == null) {
            LogUtils.i(TAG,"宿主切换 mOldAppBindInfo == null");
            notifyPaHostElection(context, null);
            return;
        }
        Map<String, AppPushInfo> bindAppMap = mOldAppBindInfo.getBindAppMap();
        if (bindAppMap == null) {
            LogUtils.i(TAG,"宿主切换 mOldAppBindInfo.getBindAppMap() == null");
            notifyPaHostElection(context, null);
            return;
        }
        //TODO 兼容
        for (String appName : bindAppMap.keySet()) {
            LogUtils.i(TAG,"宿主切换 appName:" + appName);
            if (TextUtils.equals(appName, context.getPackageName())) {
                //自己忽略掉
            }else {
                //todo 4.0.7以下老版本接口没有 PUSH_HOST_SERVICE_UPDATE
                Intent updateIntent = new Intent(SyncAction.PUSH_HOST_SERVICE_UPDATE);
                updateIntent.setClassName(appName, HostElectionHandleService.class.getName());
                LogUtils.d(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"start handle service,action:" + SyncAction.PUSH_HOST_SERVICE_UPDATE);
                context.startService(updateIntent);
            }
        }
        notifyPaHostElection(context, bindAppMap);
        mOldAppBindInfo = null;//用完置空
    }

    private void notifyPaHostElection(Context context, Map<String, AppPushInfo> bindAppMap){
        List<PandaAppInfo> pandaList = PandaAppManager.getInstance().getPandaApps();
        for (PandaAppInfo pandaApp : pandaList) {
            String appName = pandaApp.getPackageName();
            if(bindAppMap == null || (!bindAppMap.containsKey(appName) && !TextUtils.equals(appName, context.getPackageName()))){
                Intent updateIntent = new Intent(SyncAction.PUSH_HOST_SERVICE_UPDATE);
                updateIntent.setClassName(appName, HostElectionHandleService.class.getName());
                LogUtils.d(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"start handle service1,action:" + SyncAction.PUSH_HOST_SERVICE_UPDATE);
                context.startService(updateIntent);
            }
        }
    }

    public SyncRegisterInfo getSyncRegisterInfo() {
        SyncRegisterInfo syncRegisterInfo = aidlHelper.call(new AIDLTaskImpl<SyncRegisterInfo>() {
            @Override
            public SyncRegisterInfo submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v(TAG,"submit getSyncRegisterInfo on aidl");
                return iConnectionService.getSyncRegisterInfo();
            }
        });
        if (syncRegisterInfo == null) {
            syncRegisterInfo = SyncRegisterInfo.EMPTY_SYNC_REGISTER_INFO;
            LogUtils.w(TAG,"use an empty SyncRegisterInfo object.");
        }
        return syncRegisterInfo;
    }

    public void getSyncRegisterInfoSafely(final OnGetCallBack<SyncRegisterInfo> onGetCallBack) {
        aidlHelper.waitForRun(new AIDLTaskImpl<SyncRegisterInfo>() {
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v(TAG,"execute getSyncRegisterInfo on aidl");
                SyncRegisterInfo syncRegisterInfo = iConnectionService.getSyncRegisterInfo();
                if (syncRegisterInfo == null) {
                    syncRegisterInfo = SyncRegisterInfo.EMPTY_SYNC_REGISTER_INFO;
                    LogUtils.w(TAG,"use an empty sync register info object.");
                }
                if (PushApplication.checkRegisterInfo(syncRegisterInfo)) { // 如果是宿主app，此处会重复保存，因为在push进程已经保存过了
                    StoreUtil.saveRegisterInfo(app.getPlatform().getStore(), syncRegisterInfo);
                }
                onGetCallBack.onGet(syncRegisterInfo);
            }
        });
    }

    public String  getHostSdkInfo() {
        String sdkInfo=aidlHelper.call(new AIDLTaskImpl<String>(){
            @Override
            public String submit(IConnectionService iConnectionService) throws RemoteException {
                return iConnectionService.getHostSDKVersion();
            }
        });
        return sdkInfo;
    }

    public AppBindInfo getOldAppBindInfo() {
        AppBindInfo appBindInfo=aidlHelper.call(new AIDLTaskImpl<AppBindInfo>(){
            @Override
            public AppBindInfo submit(IConnectionService iConnectionService) throws RemoteException {
                return iConnectionService.getBindAppInfo();
            }
        });
        return appBindInfo;
    }

    /**
     * 获取当前宿主真实绑定的app列表信息
     * 5.0.6及之前版本没有此接口
     * @return
     */
    public AppBindInfo getCurrAppBindInfo() {
        AppBindInfo appBindInfo= null;
        try {
            appBindInfo = aidlHelper.call(new AIDLTaskImpl<AppBindInfo>(){
                @Override
                public AppBindInfo submit(IConnectionService iConnectionService) throws RemoteException {
                    return iConnectionService.getCurrBindAppInfo();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appBindInfo;
    }

    public String getHostName() {
        String hostName=aidlHelper.call(new AIDLTaskImpl<String>(){
            @Override
            public String submit(IConnectionService iConnectionService) throws RemoteException {
                return iConnectionService.getHostname();
            }
        });
        return hostName;
    }

    public boolean isLogin() {
        Boolean isLoginObj = aidlHelper.call(new AIDLTaskImpl<Boolean>() {
            @Override
            public Boolean submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v(TAG,"submit isLogin on aidl");
                return iConnectionService.isLogin();
            }
        });
        if(isLoginObj == null) {
            isLoginObj = Boolean.FALSE;
        }
        return isLoginObj.booleanValue();
    }

    public boolean hasPublicKey() {
        Boolean hasPublicKeyObj = aidlHelper.call(new AIDLTaskImpl<Boolean>() {
            @Override
            public Boolean submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v(TAG,"execute hasPublicKey on aidl");
                return iConnectionService.hasPublicKey();
            }
        });
        if (hasPublicKeyObj == null) {
            hasPublicKeyObj = Boolean.FALSE;
        }
        return hasPublicKeyObj;
    }

    public void addBackupServerInfo(final String[] serverInfo, final boolean clearBefore) {
        aidlHelper.waitForRun(new AIDLTaskImpl() {
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException, RuntimeException {
                LogUtils.v(TAG,"execute addBackupServerInfo on aidl");
                iConnectionService.addBackupServerInfo(serverInfo, clearBefore);
            }
        });
    }

    /**
     * 发送数据请求
     */
    public void enqueueTask(final Request request) {
        aidlHelper.waitForRun(new AIDLTaskImpl(){
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                if (request == null) {
                    LogUtils.e(TAG, "request is null !!!");
                    return;
                }
                if (request.getRequestEntity() == null) {
                    LogUtils.e(TAG,"request entity is null !!!");
                    return;
                }
                LogUtils.d(TAG,"execute enqueueSendTask on aidl"+ request.toString()+"\n requestEntity: "+request.getRequestEntity().toString());
                //TODO 查到断网重连的时候这里 toByteArray 会存在问题 format data error(value is null),name:accountToken,type:class java.lang.String 05-15 16:22:12.636
                byte[] data=request.getRequestEntity().toByteArray();
                LogUtils.i(TAG, "data: "+ Arrays.toString(data));
                iConnectionService.enqueueSendTask(request.getSendTime(), data, request.getTimeout());
            }
        });
    }

    /**
     * 发送心跳包
     */
    public void heartbeat() {
        aidlHelper.run(new AIDLTaskImpl(){
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v(TAG,"execute heartbeat on aidl");
                iConnectionService.heartbeat();
            }
        });
    }

    /**
     * IM连接是否断开
     * init app 时 有问题， 单独写出来进行处理
     * @return true表示断开，false表示未断开
     */
    public boolean isClosedOnInit() {
        Boolean isClosedObj = aidlHelper.callOnInit(new AIDLTaskImpl<Boolean>(){
            @Override
            public Boolean submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v(TAG,"submit isClosed on aidl , iConnectionService"+iConnectionService);
                if (iConnectionService != null) {
                    return iConnectionService.isClosed();
                }else {
                    /*
                    * 初始化的时候出现一种情况
                    * aidlHelper.iConnectionService == null (初始化出现这种情况就不在进行重试操作)
                    * 但是 查遍Log并没有设置为null的地方
                    * 暂时假装没有关闭 so return false ，初始化init之前进行一次exit处理，接着进行init
                    * */
                    return false;
                }
            }
        });

        if (isClosedObj == null) {
            LogUtils.v(TAG,"isClosed() return null...");
            isClosedObj = Boolean.TRUE;
        }
        return isClosedObj;
    }
    /**
     * IM连接是否断开
     * @return true表示断开，false表示未断开
     */
    public boolean isClosed() {
        Boolean isClosedObj = aidlHelper.call(new AIDLTaskImpl<Boolean>(){
            @Override
            public Boolean submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v(TAG,"submit isClosed on aidl");
                return iConnectionService.isClosed();
            }
        });
        if (isClosedObj == null) {
            LogUtils.v(TAG,"isClosed() return null...");
            isClosedObj = Boolean.TRUE;
        }
        return isClosedObj;
    }

    public void clearBackupServerInfo() {
        aidlHelper.run(new AIDLTaskImpl(){
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v(TAG,"execute clearServerInfo on aidl");
                iConnectionService.clearBackupServerInfo();
            }
        });
    }

    public void setHeartbeatPeriod(final int minHeart, final int maxHeart, final int heartStep) {
        aidlHelper.waitForRun(new AIDLTaskImpl(){
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                iConnectionService.setHeartbeatPeriod(minHeart, maxHeart, heartStep);
            }
        });
    }

    /**
     * 异步连接请求
     */
    private void connectServer() {
        aidlHelper.waitForRun(new AIDLTaskImpl(){
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"connectServer"," execute connect on aidl === HostName:"+ app.getHostname() + "  Port:" + app.getPort());
                if (TextUtils.isEmpty(app.getHostname()) || app.getPort() <= 0 || app.getPort() > 65535) {
                    LogUtils.e(TAG,"connectServer error host and port:" + app.getHostname() + ":" + app.getPort());
                    return;
                }
                iConnectionService.connect(app.getHostname(), app.getPort(),null);
            }
        });
    }

    private void closeConnection() {
        aidlHelper.run(new AIDLTaskImpl(){
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v(TAG,"execute close on aidl");
                iConnectionService.close();
            }
        });
    }

    public void reLogin() {
        // 如果重新登录的请求距离上一次重新登录的请求超过20秒则进行重新登录，否则不进行重新登录，避免重新登录的请求过多
        long now = System.currentTimeMillis();
        if (now - reLoginTime > 20000) {
            reLoginTime = now;
            app.login();
        } else {
            LogUtils.w(TAG,"reLogin is too frequently!");
        }
    }

    public synchronized void unBindService(){
        if (iConnServiceConnection != null) {
            try {
                LogUtils.e(TAG,"unBindService");
                context.unbindService(iConnServiceConnection);
                iConnectionService = null;
                aidlHelper.setIConnectionService(null, null);
            } catch (Exception e) {
                LogUtils.e(TAG,"unbindService error:" + e.toString());
            }
        } else {
            LogUtils.w(TAG,"iConnServiceConnection is null.");
        }
        /*iConnectionService = null;
        isBindSuccess = false;
        aidlHelper.setIConnectionService(null);*/
    }

    /**
     * 停止当前IM的连接服务并断开TCP连接
     */
    public void shutdown() {
        LogUtils.w(TAG,"宿主切换  关闭当前的IM服务  当前APP: "+context.getPackageName());
        closeConnection();
        stopService(serviceIntent);
        shutdown = true;
    }

    public boolean isBind() {
        return iConnectionService != null;
    }
    /**
     * Class: ConnectionServiceManager
     * Tag: 宿主选择
     * Ref: connectionServiceManager.shutdown();
     * Fun 停止IM服务
     */
    private void stopService(Intent intent) {
        unBindService();
        context.stopService(intent);
        LogUtils.d(TAG,"stop the TCPConnection Service");
    }

    public final class IConnServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(final ComponentName name, IBinder service) {
            LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"ServiceConnectSuccess",name.getPackageName() + ":connect to service successfully.");
            //asInterface方法中判断绑定的服务是否为跨进程的ConnectionService,并返回真正的iConnectionService，也许不再一个进程中
            iConnectionService = IConnectionService.Stub.asInterface(service);
            aidlHelper.setIConnectionService(iConnectionService, name.getPackageName());
            ExecutorsUtils.execute(new Runnable() {
                @Override
                public void run() {
                    dealServiceConnected();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.w(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"ServiceConnectFailed",name.getPackageName() + " disconnect from service.");
            if(aidlHelper.isCurrConnectionService(name.getPackageName())){
                iConnectionService = null;
                aidlHelper.setIConnectionService(null, null);
                if (onServiceConnectionListener != null) {
                    onServiceConnectionListener.onServiceDisconnected();
                }
            }
        }
    }

    private void dealServiceConnected(){
        //绑定成功回调后 需要进行一个判断 保证了aidlHelper 指向当前宿主
        if (sdkVersionSyncCheck()) {
            changeHostSdk();
            notifyOldBindAppList();
            return;
        }
        imServiceHostCheck();
        if (onServiceConnectionListener != null) {
            onServiceConnectionListener.onServiceConnected();
        }
        if (isClosed()) {
            connectServer();
            LogUtils.d(TAG,"is closed, connect server...");
        } else {
            if (isLogin()) {
                app.callOnInitSuccessListener();
                LogUtils.d(TAG,"is login init into here...");
            }
        }
        PandaAppManager.getInstance().notifyPandaAppReboot(context, getCurrAppBindInfo());//单独兼容需要把家长管理重启绑定到新的宿主上
    }

}
