package com.eebbk.bfc.im.push.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.IConnectCallback;
import com.eebbk.bfc.im.push.IConnectionService;
import com.eebbk.bfc.im.push.SDKVersion;
import com.eebbk.bfc.im.push.bean.AppBindInfo;
import com.eebbk.bfc.im.push.bean.AppPushInfo;
import com.eebbk.bfc.im.push.bean.HeartbeatStatus;
import com.eebbk.bfc.im.push.bean.HostInfo;
import com.eebbk.bfc.im.push.bean.PushCollectInfo;
import com.eebbk.bfc.im.push.bean.SyncRegisterInfo;
import com.eebbk.bfc.im.push.code.DataCoderUtil;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.config.UrlConfig;
import com.eebbk.bfc.im.push.debug.DLog;
import com.eebbk.bfc.im.push.debug.DebugEventCode;
import com.eebbk.bfc.im.push.debug.DebugEventTool;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.HeartBeatRequestEntity;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.entity.response.LoginResponseEntity;
import com.eebbk.bfc.im.push.entity.response.PublicKeyResponseEntity;
import com.eebbk.bfc.im.push.entity.response.RegisterResponseEntity;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.entity.response.SendErrorResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncResponseEntity;
import com.eebbk.bfc.im.push.error.ErrorCode;
import com.eebbk.bfc.im.push.exception.ConnectException;
import com.eebbk.bfc.im.push.exception.DecodeException;
import com.eebbk.bfc.im.push.exception.WriteDataException;
import com.eebbk.bfc.im.push.listener.OnConnectListener;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.service.dispatcher.HandleServiceNotify;
import com.eebbk.bfc.im.push.service.dispatcher.StatusCode;
import com.eebbk.bfc.im.push.service.heartbeat.ConnectSwitchService;
import com.eebbk.bfc.im.push.service.heartbeat.HeartBeatReceiver;
import com.eebbk.bfc.im.push.service.heartbeat.ScreenStateReceiver;
import com.eebbk.bfc.im.push.service.heartbeat.heartpackage.HeartbeatManager;
import com.eebbk.bfc.im.push.service.heartbeat.heartpackage.HeartbeatScheduler;
import com.eebbk.bfc.im.push.service.host.HostInfoManager;
import com.eebbk.bfc.im.push.service.host.HostServiceInfo;
import com.eebbk.bfc.im.push.service.host.httpdns.HttpDnsClient;
import com.eebbk.bfc.im.push.service.task.SendTask;
import com.eebbk.bfc.im.push.service.task.Task;
import com.eebbk.bfc.im.push.service.task.TaskExecutor;
import com.eebbk.bfc.im.push.service.task.TaskType;
import com.eebbk.bfc.im.push.service.tcp.Connection;
import com.eebbk.bfc.im.push.service.tcp.ConnectionProcessContext;
import com.eebbk.bfc.im.push.service.tcp.ReadAndWriteDataThread;
import com.eebbk.bfc.im.push.service.tcp.TCPConnection;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.DateFormatUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NetUtil;
import com.eebbk.bfc.im.push.util.NotificationUtil;
import com.eebbk.bfc.im.push.util.PublicValueStoreUtil;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;
import com.eebbk.bfc.im.push.util.platform.Store;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 跨进程服务，维护和IM服务器的长连接
 */
public class ConnectionService extends Service {

    private static final String TAG = "ConnectionService";

    public static final String ACTION = "com.eebbk.bfc.im.connection_service";

    public static final String CREATE_SERVICE_PACKAGE_NAME_TAG = "create_service_pkg_name";

    public static final String REGISTER_TAG = "register_id";

    public static final String PUSH_INFO_COLLECTION_TAG = "push_info_collection";

    public static final String CONNECT_ERROR_MSG_TAG = "connect_error_msg";

    public static final String KILL_PUSH_PROCESS_ID_TAG = "kill_push_process_id";

    public static final String HOSTNAME_TAG = "hostname";

    public static final String PORT_TAG = "port";

    private IConnectionService mBinder = new ConnectionServiceBinder();

    private long serviceCreateTime;

    private boolean destroyed;

    private String CREATE_SERVICE_PACKAGE_NAME;

    private TaskExecutor sendTaskExecutor;

    /**
     * 保存已经发送成功的任务列表
     */
    private List<SendTask> sendSuccessTaskList = new ArrayList<>();

    /**
     * 所有共享长连接service的app信息
     * (宿主切换的时候，主要让老宿主绑定的app切换到新宿主的功能，
     * 但是，这个map里可能存在没有真实绑定过来的假数据（setBindAppInfo()），尤其是在切换的过程中，
     * 4.0.5-bugfix版本的app就切不过来)
     */
    private Map<String, AppPushInfo> bindPkgNameMap = new ConcurrentHashMap<>();

    /**
     * 当前宿主真实绑定的app信息列表
     */
    private Map<String, AppPushInfo> currentBindPkgNameMap = new ConcurrentHashMap<>();

    /**
     * 设备注册信息
     */
    private SyncRegisterInfo syncRegisterInfo;

    private Connection connection;

    private Store store;

    private String hostname = UrlConfig.sHostNameDef;
    private int port = UrlConfig.sPortDef;

    private static final int HEART_BEAT_TIMEOUT = 1;

    private Intent wakefulIntent;

    private ScreenStateReceiver screenStateReceiver;

    private ServiceHandler serviceHandler;

    private HeartbeatManager mHeartbeatManager;


    private static class ServiceHandler extends Handler {

        WeakReference<ConnectionService> reference;

        ServiceHandler(Looper looper, ConnectionService service) {
            super(looper);
            reference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            ConnectionService service = reference.get();
            if (service == null) {
                LogUtils.w(TAG, "connection service has been gc!");
                return;
            }
            int what = msg.what;
            switch (what) {
                case HEART_BEAT_TIMEOUT:
                    service.handleHeartbeatTimeout((Intent) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleHeartbeatTimeout(Intent intent) {
        LogUtils.w(TAG, "heartbeat no response on time,try connect...");
        releaseConnection();
        connect(hostname, port, true, null);
//        BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    public static class InnerService extends Service {

        @TargetApi(Build.VERSION_CODES.ECLAIR)
        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(NotificationUtil.NOTIFICATION_ID, NotificationUtil.getNotification(this));
            LogUtils.i(TAG, "inner service onCreate...");
            stopSelf();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            LogUtils.i(TAG, "inner service onStartCommand...");
            return START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            LogUtils.i(TAG, "inner service destroy!");
        }
    }

    private void askForHeartbeat() {
        if (!mHeartbeatManager.isHeartbeatFrequency()) {
            HeartBeatReceiver.startHeartbeatTask(getApplicationContext(), getPackageName(), HeartbeatScheduler.REDUNDANCY_HEART);
        }
    }

    private class ConnectionServiceBinder extends IConnectionService.Stub {

        @Override
        public void connect(String hostname, int port, IConnectCallback iConnectCallback) throws RemoteException {
            ConnectionService.this.connect(hostname, port, true, iConnectCallback);
        }

        @Override
        public void enqueueSendTask(long sendTime, byte[] data, int timeout) throws RemoteException {
            ConnectionService.this.enqueueSendTask(sendTime, data, timeout);
        }

        @Override
        public void heartbeat() throws RemoteException {
            ConnectionService.this.askForHeartbeat();
        }

        @Override
        public SyncRegisterInfo getSyncRegisterInfo() throws RemoteException {
            return ConnectionService.this.getSyncRegisterInfo();
        }

        @Override
        public boolean isLogin() throws RemoteException {
            return ConnectionService.this.isLogin();
        }

        @Override
        public void close() throws RemoteException {
            ConnectionService.this.close();
        }

        @Override
        public boolean isClosed() throws RemoteException {
            return ConnectionService.this.isClosed();
        }

        @Override
        public void addBackupServerInfo(String[] serverInfo, boolean clearBefore) throws RemoteException {
            ConnectionService.this.addBackupServerInfo(serverInfo, clearBefore);
        }

        @Override
        public void clearBackupServerInfo() throws RemoteException {
            ConnectionService.this.clearBackupServerInfo();
        }

        @Override
        public String getHostname() throws RemoteException {
            if (connection != null) {
                return connection.getHostname();
            } else {
                return null;
            }
        }

        @Override
        public int getPort() throws RemoteException {
            if (connection != null) {
                return connection.getPort();
            } else {
                return 0;
            }
        }

        @Override
        public boolean hasPublicKey() throws RemoteException {
            byte[] publicKey = ConnectionProcessContext.getInstance().getPublicKey();
            return publicKey != null && publicKey.length > 0;
        }

        @Override
        public void setHeartbeatPeriod(int minHeart, int maxHeart, int heartStep) throws RemoteException {
            mHeartbeatManager.set(minHeart, maxHeart, heartStep);
        }

        @Override
        public String getHostSDKVersion() {
            return ConnectionService.this.getPushSdkInfo();
        }

        @Override
        public void setBindAppInfo(AppBindInfo appBindInfo) throws RemoteException {
            addBindAppInfoFromApp(appBindInfo);
        }

        @Override
        public AppBindInfo getBindAppInfo() throws RemoteException {
            AppBindInfo appBindInfo = new AppBindInfo(new HashMap<>(bindPkgNameMap));
            return appBindInfo;
        }

        @Override
        public AppBindInfo getCurrBindAppInfo() throws RemoteException {
            return new AppBindInfo(new HashMap<>(currentBindPkgNameMap));
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                return super.onTransact(code, data, reply, flags);
            } catch (RuntimeException e) {
                LogUtils.e(TAG, "AIDL接口异常,终于捕获到了");
                LogUtils.e(e);
                throw e;
            }
        }
    }

    /**
     * TCP连接
     *
     * @param hostname
     * @param port
     * @param cancel
     * @param iConnectCallback
     */
    private void connect(String hostname, int port, boolean cancel, IConnectCallback iConnectCallback) {
        if (TextUtils.isEmpty(hostname) || port <= 0 || port > 65535) {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP, " hostname or port is error,hostname:" + hostname + ",port:" + port);
            return;
        }
        //TODO 这个地方欠考虑 是否需要重试进行连接
       /* if (!NetUtil.isConnectToNet(this)) {
            LogUtils.w(TAG, "Net is not connect !!! ,so we can not start connect to tcp !!!");
            return;
        }*/
        boolean isDianXin2G1 = NetUtil.isConnectToNet(this) && NetUtil.isDianXinAnd2GNet(this);
        if (isDianXin2G1) {
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP, ">>> The net is DianXin and 2G net, so we cannot connect tcp !!! ");
            return;
        }

        this.hostname = hostname;
        this.port = port;

        if (connection == null) {
            TCPConnection tcpConnection = new TCPConnection(this, hostname, port);
            initOnConnectListenerForConnection(tcpConnection);
            initOnDataListenerForConnection(tcpConnection);
            connection = tcpConnection;
        }
        boolean isDianXin2G2 = NetUtil.isConnectToNet(this) && NetUtil.isDianXinAnd2GNet(this);
        if ((cancel || isDianXin2G2) && connection != null) {
            connection.cancelConnect();
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP, "cancelConnect because cancel =" + cancel + " and isDianXin2G=" + isDianXin2G2);
        }
        if (!isDianXin2G2 && connection != null) {
            LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP, "Start Connect TCP", " HostName:" + hostname + "  Port:" + port);
            connection.connect(hostname, port, true, iConnectCallback);
        } else {
            LogUtils.w(TAG, "The net is DianXin and 2G net, so we cannot connect tcp !!! ");
        }

        if(connection != null){
            LogUtils.i(TAG, "connect status:" + connection.isConnected());
        }
    }

    private void enqueueSendTask(long sendTime, byte[] data, int timeout) {
        if (data == null || data.length == 0) {
            LogUtils.e(TAG, "send data is empty!");
            return;
        }
        Task sendTask = new SendTask(this, sendTime, data, timeout);
        sendTaskExecutor.execute(sendTask);
    }

    private void executeHeartbeat(Intent intent) {
        if (connection == null) {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT, "executeHeartbeat,but connection is null,so we re initPush !!!");
//            BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(intent);
            initPush();
            return;
        }
//        BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(wakefulIntent);
        TCPConnection tcpConnection = (TCPConnection) connection;
        if (!tcpConnection.isConnected()) {
            if (!tcpConnection.isAlarmed()) {
                LogUtils.ec(TAG, "connection is closed,connect again from heart beat execute", ErrorCode.EC_TCP_IS_CLOSED);
//                BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(intent);
                connect(hostname, port, true, null);
            }
            LogUtils.w("connection is disconnected,do not send heartbeat...");
            return;
        }
        if (intent != null) {
            wakefulIntent = intent;
        }
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT, "executeHeartbeat...");
        removeHeartbeatTimeoutMessage();
        int heartType = intent.getIntExtra(HeartbeatScheduler.HEART_TYPE_TAG, 0);
        mHeartbeatManager.startOne(heartType);
        Message msg = serviceHandler.obtainMessage();
        msg.what = HEART_BEAT_TIMEOUT;
        msg.obj = intent;
        serviceHandler.sendMessageDelayed(msg, mHeartbeatManager.getTimeout()); // 心跳包20秒超时
        HeartBeatRequestEntity requestEntity = new HeartBeatRequestEntity();
        SendTask sendTask = new SendTask(this, System.currentTimeMillis(), requestEntity.toByteArray(), mHeartbeatManager.getTimeout());
        sendTask.setIntent(intent);
        sendTask.setInnerSendListener(new SendTask.InnerSendListener() {
            @Override
            public void onSended(int code) {
                if (code == StatusCode.SEND_SUCCESS) {
                    LogUtils.d("send heartbeat successfully");
                } else {
                    LogUtils.e(TAG, "send heartbeat failed");
                }
            }
        });
        sendTaskExecutor.execute(sendTask);
        mHeartbeatManager.setSendHeartbeatTime(System.currentTimeMillis());

    }

    private void removeHeartbeatTimeoutMessage() {
        if (serviceHandler.hasMessages(HEART_BEAT_TIMEOUT)) {
            serviceHandler.removeMessages(HEART_BEAT_TIMEOUT);
            mHeartbeatManager.cancelOne();
        }
    }

    public SyncRegisterInfo getSyncRegisterInfo() {
        return syncRegisterInfo;
    }

    /**
     * 设备是否登录
     */
    public boolean isLogin() {
        return syncRegisterInfo.isLogin();
    }

    /**
     * 数据发送
     */
    public int send(RequestEntity requestEntity) {
        if (requestEntity == null) {
            LogUtils.e(TAG, "send data entity is null");
            return StatusCode.DATA_SEND_NULL_ERROR;
        }
        DebugEventTool.getInstance().event(System.currentTimeMillis(), Process.myPid()+"", DebugEventCode.DEBUG_EVENT_CODE_REQUEST, "请求",
                requestEntity.toString());
        byte[] data = DataCoderUtil.encodeData(requestEntity);
        return sendImpl(data);
    }

    /**
     * 关闭连接
     */
    private void close() {
        if (connection != null && connection.isConnected()) {
            connection.close();
        }
//        connection = null;
        HostInfoManager.getInstance().clear();
        HttpDnsClient.clear();
//        WeHeartBeatScheduler.clearHeartbeatInfo();
        sendTaskExecutor.cancelAll();
    }

    private boolean isClosed() {
        if (connection != null && connection.isConnected()) {
            return false;
        }
        return true;
    }


    private void addBackupServerInfo(String[] serverInfo, boolean clearBefore) {
        if (serverInfo == null || serverInfo.length == 0) {
            LogUtils.w(TAG, "addBackupServerInfo:serverInfo is empty.");
            return;
        }

        HostInfoManager hostInfoManager = HostInfoManager.getInstance();
        if (clearBefore) {
            hostInfoManager.clear();
        }
        for (String s : serverInfo) {
            final String[] host = s.split(":");
            if (host.length != 2) {
                continue;
            }
            try {
                String ip = host[0];
                int port = Integer.parseInt(host[1]);
                if (TextUtils.isEmpty(ip) || port <= 0 || port > 65535) {
                    LogUtils.test("hostname:" + ip + ",port:" + port);
                    continue;
                }
                HostInfo hostInfo = new HostInfo(ip, port);
                hostInfoManager.add(hostInfo);
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }
        LogUtils.i(TAG, "add serverInfo success:" + hostInfoManager.toString());
//        parseAndAddServerInfo(serverInfo, hostInfoManager); // 如果ip是域名，则把域名转换成ip后加入到预埋ip列表
    }

    private void clearBackupServerInfo() {
        HostInfoManager.getInstance().clear();
    }

    private String getPushSdkInfo() {
        return SDKVersion.getSdkInfo(this)/* + "\n 宿主Key： " + AppUtil.getAppKey(this)*/;
    }

    public static Intent createServiceIntent(Context context, String componentPckName) {
        Intent serviceIntent = new Intent();
        serviceIntent.setPackage(context.getPackageName());
        serviceIntent.setComponent(new ComponentName(componentPckName, ConnectionService.class.getName()));
        serviceIntent.setAction(ConnectionService.ACTION);
        serviceIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
        return serviceIntent;
    }

    @Override
    public void onCreate() {
//        initLogUtil();
        LogUtils.d(TAG, "connection Service is entry----->");
        super.onCreate();
        //初始化心跳管理器
        HeartbeatManager.setPolicy(HeartbeatManager.WATCH);
        mHeartbeatManager = HeartbeatManager.getInstance();

        serviceCreateTime = System.currentTimeMillis();
        CREATE_SERVICE_PACKAGE_NAME = getPackageName();
        sendTaskExecutor = new TaskExecutor(1024);

        store = new PhoneStore(this);

        initHandler();

        loadData();


        startNotificationToForeground();

        startInnerService();

        registerScreenStateReceiver();
        LogUtils.d(TAG, "TCPConnection Service has created,CREATE_SERVICE_PACKAGE_NAME:" + CREATE_SERVICE_PACKAGE_NAME);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            KeepLiveService.startJobScheduler(this);//如果功耗问题还是存在就注释掉吧，这里只是加个保险
//        }
    }

    private void startNotificationToForeground() {
        startForeground(NotificationUtil.NOTIFICATION_ID, NotificationUtil.getNotification(this));
    }

    public long getConnectedDuration() {
        if (connection != null) {
            TCPConnection tcpConnection = (TCPConnection) connection;
            return tcpConnection.getCurrentConnectedDuration();
        }
        return 0;
    }

    private void initHandler() {
        HandlerThread handlerThread = new HandlerThread("ConnectionService_Handler");
        handlerThread.start();
        serviceHandler = new ServiceHandler(handlerThread.getLooper(), this);
    }

    private void registerScreenStateReceiver() {
        screenStateReceiver = new ScreenStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStateReceiver, intentFilter);
    }


    private void loadData() {
        // 加载设备注册信息
        StoreUtil.saveRegisterInfo(store, false); // 首先把文件中的设备登录状态还原
        syncRegisterInfo = StoreUtil.readRegisterInfo(store);


        // 加载公钥信息
        byte[] publicKey = StoreUtil.readPublicKey(store);
        if (publicKey != null) {
            ConnectionProcessContext.getInstance().setPublicKey(publicKey);
        }

        // 加载包名信息
        List<AppPushInfo> list = StoreUtil.readAppPushInfo(store);
        if (list == null) {
            return;
        }
        for (AppPushInfo appPushInfo : list) {
            if (appPushInfo == null) {
                continue;
            }
            if (TextUtils.isEmpty(appPushInfo.getPkgName())) {
                continue;
            }
            bindPkgNameMap.put(appPushInfo.getPkgName(), appPushInfo);
        }
        LogUtils.i(TAG, "loadData bindPkgNameMap:" + bindPkgNameMap);
    }

    private void startInnerService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this.getPackageName(), "com.eebbk.bfc.im.push.service.ConnectionService$InnerService"));
        startService(intent);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        LogUtils.i(TAG, this.getClass().getSimpleName() + ".onStartCommand,pkgName:" + getPackageName());
        if (intent != null) {
            int taskType = intent.getIntExtra(TaskType.TAG, 0);
            LogUtils.i(TAG, "start connection service task type:" + taskType);
            switch (taskType) {
                case TaskType.HEART_BEAT:
                    if (NetUtil.isConnectToNet(this) && NetUtil.isDianXinAnd2GNet(this)) {
                        LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT, "The net is DianXin and 2G this time ,so we can not response the " +
                                "heartbeat");
                    } else {
                        executeHeartbeat(intent);
                        recordConnectInfo(hostname, port, false, false);
                    }
                    break;
                case TaskType.ALARM_CONNECT:
                    alarmConnect(intent);
                    break;
                case TaskType.APP_REMOVED:
                    String pkgName = intent.getStringExtra("package_name");
                    LogUtils.w(TAG, "remove pkgName:" + pkgName);
                    if (!TextUtils.equals(pkgName, getPackageName())) {
                        unbindAppPushInfo(pkgName);
                        unbindCurrAppPushInfo(pkgName);
                    }
                    break;
                case TaskType.DEFAULT:
                    LogUtils.d(TAG, "default do nothing");
                    break;
                default:
                    LogUtils.w(TAG, "unKnow task type:" + taskType);
            }
        } else {
            LogUtils.d(TAG, "ConnectionService.onStartCommand");
            initPush();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void initPush() {
        Intent intentConnect = new Intent();
        intentConnect.setAction(SyncAction.CONNECT_SWITCH_SERVICE_ACTION);
        intentConnect.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
        intentConnect.putExtra(ConnectSwitchService.BUNDLE_KEY_SERVICE_SWITCH, ConnectSwitchService.BUNDLE_VALUE_SERVICE_SWITCH_ON);
        intentConnect.setComponent(new ComponentName(this.getPackageName(), ConnectSwitchService.class.getName()));
        startService(intentConnect);
    }

    private void recordConnectInfo(String hostname, int port, boolean connecting, boolean notify) {
        TCPConnection tcpConnection = (TCPConnection) connection;
        if (tcpConnection != null) {
            long connectedTime = tcpConnection.getConnectedTime();
            long disconnected = tcpConnection.getDisconnectedTime();
            long duration = tcpConnection.getCurrentConnectedDuration();
            boolean connected = tcpConnection.isConnected();
            boolean isLogin = isLogin();
            PushCollectInfo pushCollectInfo = new PushCollectInfo(this, serviceCreateTime, destroyed, connectedTime, disconnected, duration);
            pushCollectInfo.setPushConnected(connected);
            pushCollectInfo.setPushLogin(isLogin);
            pushCollectInfo.setRegisterId(syncRegisterInfo.getRegisterId());
            pushCollectInfo.setHostname(hostname);
            pushCollectInfo.setPort(port);
            pushCollectInfo.setConnecting(connecting);

            HeartbeatStatus heartbeatStatus = new HeartbeatStatus();
            heartbeatStatus.setStarted(mHeartbeatManager.isStarted());
            heartbeatStatus.setStabled(mHeartbeatManager.isStabled());
            heartbeatStatus.setCurHeart(mHeartbeatManager.getCurHeart());
            heartbeatStatus.setHeartbeatStabledSuccessCount(mHeartbeatManager.getHeartbeatStabledSuccessCount());
            heartbeatStatus.setHeartbeatProbeDuration(mHeartbeatManager.getHeartbeatProbeDuration());
            heartbeatStatus.setHeartbeatTotalCount(mHeartbeatManager.getHeartbeatTotalCount());
            heartbeatStatus.setHeartbeatTotalRedundancyCount(mHeartbeatManager.getHeartbeatTotalRedundancyCount());
            heartbeatStatus.setHeartbeatTotalAlarmCount(mHeartbeatManager.getHeartbeatTotalAlarmCount());
            heartbeatStatus.setHeartbeatTotalSuccessCount(mHeartbeatManager.getHeartbeatTotalSuccessCount());
            heartbeatStatus.setHeartbeatTotalFailedCount(mHeartbeatManager.getHeartbeatTotalFailedCount());
            try {
                pushCollectInfo.setHeartbeatInfo(heartbeatStatus.toString());
            } catch (Exception e) {
                LogUtils.e(TAG, e);
            }
            LogUtils.i("pushCollectInfo:" + pushCollectInfo);
            if (notify) {
                HandleServiceNotify.notifyOnPushCollectHandleService(this, pushCollectInfo);
            } else {
                if (mHeartbeatManager.isStabled() && !mHeartbeatManager.isStabledCollected()) { // 当探测到稳定心跳的时候就进行一次上报
                    HandleServiceNotify.notifyOnPushCollectHandleService(this, pushCollectInfo);
                    mHeartbeatManager.setStabledCollected(true);
                }
            }

            LogUtils.i(TAG, "pushCollectInfo:" + pushCollectInfo);
        } else {
            LogUtils.e(TAG, "recordConnectInfo connection is null.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.i(TAG, "TCPConnection Service onBind:" + intent.getPackage());
        if (intent != null) {
            bindAppPushInfo(intent.getPackage(), true);
            currbindAppPushInfo(intent.getPackage());
        }
        return mBinder.asBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        LogUtils.i(TAG, "TCPConnection Service onRebind:" + intent.getPackage());
        if (intent != null) {
            bindAppPushInfo(intent.getPackage(), true);
            currbindAppPushInfo(intent.getPackage());
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (intent != null) {
            unbindCurrAppPushInfo(intent.getPackage());
            LogUtils.e(TAG, "TCPConnection Service has unBind,action:" + intent.getAction() + ",packageName:" + intent.getPackage());
        }
        return true;
    }

    @Override
    public void onDestroy() {
        mHeartbeatManager.clear(this);
        serviceHandler.getLooper().quit();
        sendTaskExecutor.shutdown();
        HttpDnsClient.clear();
        clear();
        unregisterReceiver(screenStateReceiver);
        stopForeground(true);
        destroyed = true;
        LogUtils.e(TAG, "宿主切换 TCPConnection Service has destroyed:" + getPackageName() + " pid:" + Process.myPid());
        super.onDestroy();
//        Process.killProcess(Process.myPid());
        ConnectSwitchService.turnOnConnectService(this, Process.myPid());
    }

    /**
     * Class: ConnectionService
     * Tag: 宿主选择
     * Ref: iConnectionService.setBindAppInfo()
     * Fun：将老宿主的app绑定列表同步到新宿主
     * @param appBindInfo
     */
    private void addBindAppInfoFromApp(AppBindInfo appBindInfo) {
        if (appBindInfo == null) {
            LogUtils.e(TAG,"appBindInfo is null !!!");
            return;
        }
        Map<String, AppPushInfo> olBindAppMap = appBindInfo.getBindAppMap();
        if (olBindAppMap == null) {
            LogUtils.e(TAG,"olBindAppMap is null !!!");
            return;
        }
        bindPkgNameMap.putAll(olBindAppMap);//老宿主的绑定列表添加到新宿主，重复的会自动覆盖

        for (String key : olBindAppMap.keySet()) {
            bindAppPushInfo(key, true);
        }
    }

    /**
     * Class: ConnectionService
     * Tag: 宿主选择
     * Ref: ConnectionService.onBind(intent) | ConnectionService.onRebind(intent) | setBindAppInfo()
     * Fun：当有其他App绑定到IM服务后，和宿主切换需重新绑定，存入该App的包名到 bindPkgNameMap
     */
    private void bindAppPushInfo(String bindPkgName, boolean needStore) {
        if (TextUtils.isEmpty(bindPkgName)) {
            LogUtils.e(TAG, "bindPkgName is null!");
            return;
        }
        //校验是否存在
        Context bindPkgCtx = getContextByPkgName(bindPkgName);
        if (bindPkgCtx != null) {
            AppPushInfo appPushInfo = bindPkgNameMap.get(bindPkgName);
            if (appPushInfo == null) {
                LogUtils.i(TAG, "BIND_PACKAGE_NAME:" + bindPkgName);
                appPushInfo = new AppPushInfo(bindPkgName);
                bindPkgNameMap.put(bindPkgName, appPushInfo);
            }
            if (TextUtils.isEmpty(appPushInfo.getPkgName())) {
                appPushInfo.setPkgName(bindPkgName);
            }
            if (appPushInfo.getRidTag() == null) {
                appPushInfo.setRidTag(AppUtil.getRIDTag(bindPkgCtx));
            }
            if (needStore) {
                StoreUtil.saveAppPushInfo(store, appPushInfo);
            }
        }

        LogUtils.i(TAG, "bindPkgNameMap:" + bindPkgNameMap);
    }

    /**
     * Class: ConnectionService
     * Tag: 宿主选择
     * Ref: ConnectionService.onBind(intent) | ConnectionService.onRebind(intent) | setBindAppInfo()
     * Fun：广播监听到其他的App被移除后，从 bindPkgNameMap 中移除该App
     */
    private void unbindAppPushInfo(String unbindPkgName) {
        if (TextUtils.isEmpty(unbindPkgName)) {
            return;
        }

        AppPushInfo appPushInfo = bindPkgNameMap.get(unbindPkgName);
        if (appPushInfo != null) {
            bindPkgNameMap.remove(unbindPkgName);
            StoreUtil.removeAppPushInfo(store, appPushInfo);
            LogUtils.i(TAG, "bindPkgNameMap:" + bindPkgNameMap);
        }
//        clearDialogIdInfo(unbindPkgName);
    }

    /**
     * Class: ConnectionService
     * Tag: 宿主选择
     * Ref: ConnectionService.onBind(intent) | ConnectionService.onRebind(intent)
     * Fun：当有其他App绑定到IM服务后，存入该App的包名到 bindPkgNameMap
     */
    private void currbindAppPushInfo(String bindPkgName) {
        if (TextUtils.isEmpty(bindPkgName)) {
            LogUtils.e(TAG, "bindCurrPkgName is null!");
            return;
        }
        //校验是否存在
        Context bindPkgCtx = getContextByPkgName(bindPkgName);
        if (bindPkgCtx != null) {
            AppPushInfo appPushInfo = currentBindPkgNameMap.get(bindPkgName);
            if (appPushInfo == null) {
                LogUtils.i(TAG, "BIND_PACKAGE_NAME_CURR:" + bindPkgName);
                appPushInfo = new AppPushInfo(bindPkgName);
                appPushInfo.setRidTag(AppUtil.getRIDTag(bindPkgCtx));
                currentBindPkgNameMap.put(bindPkgName, appPushInfo);
            }
        }
        LogUtils.i(TAG, "bindCurrPkgNameMap:" + currentBindPkgNameMap);
    }

    /**
     * Class: ConnectionService
     * Tag: 宿主选择
     * Ref: ConnectionService.onBind(intent) | ConnectionService.onRebind(intent) | setBindAppInfo()
     * Fun：广播监听到其他的App被移除后，从 bindPkgNameMap 中移除该App，和解绑的
     */
    private void unbindCurrAppPushInfo(String unbindPkgName) {
        if (TextUtils.isEmpty(unbindPkgName)) {
            LogUtils.e(TAG, "unbindCurrPkgName is null!");
            return;
        }
        AppPushInfo appPushInfo = currentBindPkgNameMap.get(unbindPkgName);
        if (appPushInfo != null) {
            currentBindPkgNameMap.remove(unbindPkgName);
            LogUtils.i(TAG, "bindCurrPkgNameMap:" + currentBindPkgNameMap);
        }
//        clearDialogIdInfo(unbindPkgName);
    }

    private Context getContextByPkgName(String pkgName) {
        Context context = null;
        try {
            context = createPackageContext(pkgName, CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(e);
        }
        return context;
    }

    public boolean hasSamePushSyncRequestTask(SendTask sendTask) {
        PushSyncRequestEntity srcEntity = (PushSyncRequestEntity) sendTask.getRequestEntity();
        synchronized (sendSuccessTaskList) {
            if (sendSuccessTaskList.size() == 0) {
                return false;
            }
            for (SendTask st : sendSuccessTaskList) {
                if (st.getCommand() != Command.PUSH_SYNC_REQUEST) {
                    continue;
                }
                PushSyncRequestEntity destEntity = (PushSyncRequestEntity) st.getRequestEntity();
                String alias = destEntity.getAlias();
                if (TextUtils.isEmpty(alias)) {
                    continue;
                }
                if (alias.equals(srcEntity.getAlias()) && destEntity.getSyncKey() >= srcEntity.getSyncKey()) {
                    return true;
                }
            }
        }
        return false;
    }

    public TaskExecutor getSendTaskExecutor() {
        return sendTaskExecutor;
    }

    public void handleSendTaskSuccess(SendTask sendTask) {
        LogUtils.d(TAG, "handleSendTaskSuccess");
        RequestEntity requestEntity = sendTask.getRequestEntity();
        int command = requestEntity.getCommand();
        if (command == Command.PUSH_SYNC_FIN_ACK) {
            return;
        }
        if (command == Command.HEART_BEAT_REQUEST) {
            Intent intent = sendTask.getIntent();
            int heartType = intent.getIntExtra(HeartbeatScheduler.HEART_TYPE_TAG, 0);
            LogUtils.d(TAG, "send a heartbeat data successfully...,heartType:" + heartType);
        }

        synchronized (sendSuccessTaskList) {
            sendSuccessTaskList.add(sendTask);
        }
    }

    public void handleSendTaskError(int errorCode, SendTask sendTask) {
        LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP, "handleSendTaskError errorCode:" + errorCode);
        if (sendTask.getCommand() == Command.HEART_BEAT_REQUEST) {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP, "send heartbeat data error,try connect tcp again...");
            serviceHandler.removeMessages(HEART_BEAT_TIMEOUT);
            Intent intent = sendTask.getIntent();
            connect(hostname, port, true, null);
//            BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(intent);
        } else {
            SendErrorResponseEntity sendErrorResponseEntity = new SendErrorResponseEntity();
            sendErrorResponseEntity.setRID(sendTask.getRequestEntity().getRID());
            sendErrorResponseEntity.setCode(Response.Code.SEND_ERROR);
            sendErrorResponseEntity.setDesc("send error,error code:" + errorCode);
            sendErrorResponseEntity.setRequestEntity(sendTask.getRequestEntity());
            deliverResponseEntity(sendErrorResponseEntity);
        }
    }

    private void alarmConnect(Intent intent) {
        LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP, "ALARM_CONNECT", "start an alarm connect next time !!!");
        String hostname = intent.getStringExtra("hostname");
        int port = intent.getIntExtra("port", 0);
        LogUtils.i(TAG, "alarm hostname:" + hostname + ",port:" + port);
        if (connection != null) {
            TCPConnection tcpConnection = (TCPConnection) connection;
            tcpConnection.connect(hostname, port, true, null);
        } else {
            LogUtils.e(TAG, "alarmConnect connection is null.");
            connect(hostname, port, true, null);
        }
        recordConnectInfo(hostname, port, true, true);
    }

    private void initOnConnectListenerForConnection(final TCPConnection tcpConnection) {
        tcpConnection.setOnConnectListener(new OnConnectListener() {

            @Override
            public void onStartConnect(String hostname, int port) {
                LogUtils.i(TAG, "start connect,hostname:" + hostname + ",port:" + port);
                HandleServiceNotify.notifyStartConnectHandleService(ConnectionService.this, new HashMap<>(bindPkgNameMap), hostname, port);
            }

            @Override
            public void onConnected(byte[] socketSecretKey, String hostname, int port) {
                LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP, "TCP Connected CallBack", "connect to server success,hostname:" + hostname +
                        ",port:" + port);
                LogUtils.v(TAG, "on thread:" + Thread.currentThread().getName()+"connect to server success,hostname:" + hostname +
                        ",port:" + port);
                sendTaskExecutor.start();
                HandleServiceNotify.notifyConnectedHandleService(ConnectionService.this, new HashMap<>(bindPkgNameMap), hostname, port);
                ConnectionService.this.hostname = hostname;
                ConnectionService.this.port = port;
                ConnectionProcessContext.getInstance().setSecretKey(socketSecretKey);
                LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT, "Heartbeat Start", "we start heartbeat when the tcp connected !!!");
                mHeartbeatManager.start(ConnectionService.this);

                LogUtils.i(TAG, "预埋IP信息:" + HostInfoManager.getInstance().toString());
            }

            @Override
            public void onDisconnected(boolean reconnect) {
                LogUtils.w(TAG, LogTagConfig.LOG_TAG_ERROR_TCP, "connection is disconnected");
                LogUtils.v(TAG, "on thread:" + Thread.currentThread().getName());
                sendTaskExecutor.stop();
                serviceHandler.removeMessages(HEART_BEAT_TIMEOUT); // 取消心跳超时检测
                syncRegisterInfo.setLogin(false);
                StoreUtil.saveRegisterInfo(store, syncRegisterInfo); // 把设备登录状态设置为未登录
                ConnectionProcessContext.getInstance().setEncrypt(false); // 数据加密状态设置为未加密

                LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT, "HeartBeatFailed", "heartbeat is failed then stop heartbeat when tcp " +
                        "connection is call back for on disconnected !");
                mHeartbeatManager.receiveHeartbeatFailed(ConnectionService.this);
                mHeartbeatManager.stop(ConnectionService.this);

                HandleServiceNotify.notifyDisconnectedHandleService(ConnectionService.this, new HashMap<>(bindPkgNameMap));

                LogUtils.w(TAG, "预埋IP信息:" + HostInfoManager.getInstance().toString());
            }

            @Override
            public void onFailed(ConnectException error, String hostname, int port) {
                LogUtils.e(TAG, LogTagConfig.LOG_TAG_ERROR_TCP, "connect fail , notify top level try connect again, error msg:" + error.toString()
                        + ",hostname:" + hostname + ",port:" + port);
                //ToDo 通知上层app进程重置初始化状态，以便下一次再次连接  2017-03-24
                LogUtils.v(TAG, "on thread:" + Thread.currentThread().getName());
                LogUtils.e(TAG, "预埋IP信息:" + HostInfoManager.getInstance().toString());
                String errorMsg = "[" + hostname + ":" + port + "] connect failed:" + error.toString();
                HandleServiceNotify.notifyConnectFailedHandleService(ConnectionService.this, new HashMap<>(bindPkgNameMap), errorMsg, hostname, port);
            }
        });
    }

    private void initOnDataListenerForConnection(TCPConnection tcpConnection) {
        tcpConnection.setOnDataListener(new ReadAndWriteDataThread.OnDataListener() {
            @Override
            public void onWrite(int writeByte) {
                LogUtils.i(TAG, "write [" + writeByte + "] bytes.");
                sweepSendRequestSuccessList(0);
            }

            @Override
            public void onWriteError(String errorMsg) {
                LogUtils.e(TAG, "write error:" + errorMsg);
                sweepSendRequestSuccessList(0);
            }

            @Override
            public void onRead(byte[] data) {
                LogUtils.i(TAG, LogTagConfig.LOG_TAG_ST, "read [" + data.length + "] bytes");
                ResponseEntity responseEntity = null;
                try {
                    responseEntity = DataCoderUtil.decodeData(ConnectionService.this, data);
                    if (responseEntity != null) {
                        deliverResponseEntity(responseEntity);
                        sweepSendRequestSuccessList(responseEntity.getRID());
                    } else {
                        LogUtils.e(TAG, "responseEntity is null.");
                        sweepSendRequestSuccessList(0);
                    }
                } catch (DecodeException e) {
                    releaseConnection();
                    connect(hostname, port, true, null);
                    LogUtils.e(e);
                    sweepSendRequestSuccessList(0);
                }
            }
        });
    }


    private void sweepSendRequestSuccessList(int rid) {
        synchronized (sendSuccessTaskList) {
            Iterator<SendTask> iterator = sendSuccessTaskList.iterator();
            while (iterator.hasNext()) {
                SendTask sendTask = iterator.next();
                RequestEntity requestEntity = sendTask.getRequestEntity();
                if (requestEntity.getRID() == rid) {
                    iterator.remove();
                    LogUtils.i(TAG, "remove a request entity:" + requestEntity + ",size:" + sendSuccessTaskList.size());
                } else if (sendTask.isTimeout()) {
                    iterator.remove();
                    LogUtils.i(TAG, "remove a timeout request entity:" + requestEntity + ",size:" + sendSuccessTaskList.size());
                }
            }
        }
    }

    /**
     * Class: ConnectionService
     * Tag: 宿主选择
     * Ref: ConnectionService.onBind(intent) | ConnectionService.onRebind(intent)
     * Fun：当有其他App绑定到IM服务后，存入该App的包名到 bindPkgNameMap
     */
    private void deliverResponseEntity(ResponseEntity responseEntity) {
        LogUtils.e(LogTagConfig.LOG_TAG_POINT_PUSH_MSG_GET, "deliverResponseEntity response entity:" + responseEntity);
        Da.record(getApplicationContext(), new DaInfo().setFunctionName(Da.functionName.RESPONSE)
                .setTrigValue("response:" + responseEntity));
        DebugEventTool.getInstance().event(System.currentTimeMillis(), this.getPackageName(), DebugEventCode.DEBUG_EVENT_CODE_RESPONSE,"响应",responseEntity.toString());
        // 推送消息内容接收处理
        if (responseEntity instanceof PushSyncResponseEntity) {
            byte[] bytes =  ((PushSyncResponseEntity) responseEntity).getMessage();
            String content = new String(bytes);
            DLog.d(DebugEventTool.TAG, "content=" + content);

            if (!DebugEventTool.PUSH_DEBUG_ON.equals(content) &&! DebugEventTool.PUSH_DEBUG_OFF.equals(content)) {
                DebugEventTool.getInstance().event(System.currentTimeMillis(), this.getPackageName(), DebugEventCode.DEBUG_EVENT_CODE_MSG_RECEIVE,content,responseEntity.toString());
//                return;
            }else{
                if (DebugEventTool.setPushMode(content)) {
                    DebugEventTool.init(this);
                }else {
                    DebugEventTool.getInstance().destroy();
                }
            }

        }else {

        }
        boolean filter = filterResponseEntity(responseEntity);
        if (filter) {
            return;
        }
        List<Context> contextList = ContextSelector.selectReceiveContexts(this, responseEntity, new HashMap<>(bindPkgNameMap));
        HandleServiceNotify.notifyMessageHandleService(this, responseEntity, contextList);
    }

    private boolean filterResponseEntity(ResponseEntity responseEntity) {
        int command = responseEntity.getCommand();
        if (command == Command.HEART_BEAT_RESPONSE) {   // 处理心跳包,心跳包数据不往外发
            handleHeartbeatResponseSuccess();
            return true;
        } else {
            if (command != Command.SEND_ERROR_RESPONSE) {
                // 把除了本地发送失败的任意请求回馈(无论成功还是失败)包当作心跳包的响应
                LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT, "ResponseNotError", "we remove timeout heartbeat and reset heartbeat whatever " +
                        "" + "" + "" + "not error response !!!");
                removeHeartbeatTimeoutMessage();
                mHeartbeatManager.resetScheduledHeart(this);
            }
        }

        if (command == Command.ENCRYPT_SET_RESPONSE) {
            handleEncryptSetResponse(responseEntity);
        } else if (command == Command.PUBLIC_KEY_RESPONSE) {
            handlePublicKeyResponse(responseEntity);
        } else if (command == Command.REGISTER_RESPONSE) {
            handleRegisterResponse(responseEntity);
        } else if (command == Command.LOGIN_RESPONSE) {
            handleLoginResponse(responseEntity);
        }
        return false;
    }

    private void handleHeartbeatResponseSuccess() {
        removeHeartbeatTimeoutMessage();
        mHeartbeatManager.receiveHeartbeatSuccess(this);
        LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT, "HeartBeatResponseSuccess", "heartbeat response on time:" + DateFormatUtil.format
                ("HH:mm:ss", mHeartbeatManager.getHeartbeatSuccessTime()));
    }

    private void handleEncryptSetResponse(ResponseEntity responseEntity) {
        ConnectionProcessContext connectionProcessContext = ConnectionProcessContext.getInstance();
        if (responseEntity.getCode() == Response.Code.SUCCESS) {
            connectionProcessContext.setEncrypt(true);
        } else {
            connectionProcessContext.setEncrypt(false);
        }
    }

    private void handlePublicKeyResponse(ResponseEntity responseEntity) {
        if (responseEntity.getCode() == Response.Code.SUCCESS) {
            PublicKeyResponseEntity publicKeyResponseEntity = (PublicKeyResponseEntity) responseEntity;
            byte[] publicKeyBytes = publicKeyResponseEntity.getPublicKey();
            if (publicKeyBytes == null || publicKeyBytes.length == 0) {
                return;
            }
            String publicKeyStr = parsePublicKeyString(publicKeyResponseEntity.getPublicKey());
            ConnectionProcessContext.getInstance().setPublicKey(publicKeyStr.getBytes());
            StoreUtil.savePublicKey(store, publicKeyStr.getBytes());
        }
    }

    private void handleRegisterResponse(ResponseEntity responseEntity) {
        RegisterResponseEntity registerResponseEntity = (RegisterResponseEntity) responseEntity;
        if (registerResponseEntity.getCode() == Response.Code.SUCCESS) {
            long registerId = registerResponseEntity.getRegisterId();
            syncRegisterInfo.setRegisterId(registerId);
            syncRegisterInfo.setRegisterToken(registerResponseEntity.getRegisterToken());
            StoreUtil.saveRegisterInfo(store, syncRegisterInfo);
        } else {
            LogUtils.e(TAG, "register fail:" + registerResponseEntity);
        }
    }

    private void handleLoginResponse(ResponseEntity responseEntity) {
        LoginResponseEntity loginResponseEntity = (LoginResponseEntity) responseEntity;
        int code = loginResponseEntity.getCode();
        if (code == Response.Code.SUCCESS) {
            syncRegisterInfo.setLogin(true);
            StoreUtil.saveRegisterInfo(store, syncRegisterInfo);
            HandleServiceNotify.notifyOnLoginHandleService(this, new HashMap<>(bindPkgNameMap), syncRegisterInfo.getRegisterId());
        } else if (code == Response.Code.UN_KNOW_REGISTER_ID || code == Response.Code.REGISTER_TOKEN_INVALID) {
            syncRegisterInfo = SyncRegisterInfo.EMPTY_SYNC_REGISTER_INFO;
            StoreUtil.clearRegisterInfo(store);
            LogUtils.e(TAG, "login fail,un know register id or register token is invalid!");
        } else {
            syncRegisterInfo.setLogin(false);
            StoreUtil.saveRegisterInfo(store, syncRegisterInfo);
            LogUtils.e(TAG, "login fail:" + loginResponseEntity);
        }
    }


    private String parsePublicKeyString(byte[] publicKey) {
        String publicKeyStr = new String(publicKey);
        String start = "-----BEGIN PUBLIC KEY-----";
        String end = "-----END PUBLIC KEY-----";
        int startIndex = publicKeyStr.indexOf(start) + start.length();
        int endIndex = publicKeyStr.indexOf(end);
        publicKeyStr = publicKeyStr.substring(startIndex, endIndex);

        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(publicKeyStr);
        publicKeyStr = m.replaceAll("");

        return publicKeyStr;
    }

    private int sendImpl(byte[] data) {
        if (connection == null) {
            return StatusCode.CONNECTION_OBJ_IS_NULL;
        }
        if (data == null || data.length == 0) {
            return StatusCode.DATA_SEND_NULL_ERROR;
        }
        int code;
        try {
            connection.send(data);
            code = StatusCode.SEND_SUCCESS;
            LogUtils.d(TAG, "data sent successfully.");
        } catch (WriteDataException error) {
            code = StatusCode.WRITE_DATA_ERROR;
            LogUtils.e(error);
        }
        return code;
    }

    private void releaseConnection() {
        if (connection != null && connection.isConnected()) {
            connection.releaseConnection();
        }
    }

    private void clear() {
        if (connection != null) {
            TCPConnection tcpConnection = (TCPConnection) connection;
            tcpConnection.setOnConnectListener(null);
            tcpConnection.setOnDataListener(null);
            tcpConnection.shutdownExecutor();
            connection = null;
        }
        bindPkgNameMap.clear();
        synchronized (sendSuccessTaskList) {
            sendSuccessTaskList.clear();
        }
    }

    /**
     * Class: ConnectionService
     * Tag: 宿主选择
     * Ref: ConnectionService.getHostServiceInfo()
     * Fun: 找活着的IM服务  先找本地保存的宿主的IM服务，如果isRunning则返回，如果没有则找自己的service是否running ，如果是则返回自己
     */
    private static String getRunningServicePkgName(Context context) {
        String runningServicePkgName = null;
        String hostPackageName = PublicValueStoreUtil.getHostPackageName();
        //找宿主
        if (!TextUtils.isEmpty(hostPackageName)) {
            if (AppUtil.isServiceRunning(context, hostPackageName, ConnectionService.class.getName())) {
                runningServicePkgName = hostPackageName;
                LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, "HostService", "host runningServicePkgName = hostPackageName=" +
                        hostPackageName);
                return runningServicePkgName;
            } else {
                LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, "HostService", "host host package name is exist, but is not running !!! " +
                        "" + "" + "hostPackageName=" + hostPackageName);
            }
        } else {
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, "HostService", "host package name is empty !!! ");
        }

        //找自己
        if (AppUtil.isServiceRunning(context, context.getPackageName(), ConnectionService.class.getName())) {
            runningServicePkgName = context.getPackageName();
            LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, "HostService", " My service is  running  , then we set running service " +
                    "package name to context.getPackageName()=" + context.getPackageName());
        } else {
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, "HostService", " My service is not running too , then we will return null "
                    + "!!! context.getPackageName()=" + context.getPackageName());
        }
        return runningServicePkgName;
    }

    /**
     * Class: ConnectionService
     * Tag: 宿主选择
     * Ref: ConnectSwitchService.getHostServicePackageName()  | HeartBeatReceiver.onReceive() | ScreenStateReceiver.onReceive()
     * Fun；（1）、如果当前没有活着的IM服务 同时 当前的App就是本地保存的宿主 则设置宿主为当前的App
     *      （2）、如果本地有保存宿主App包名 当前有活着的IM服务  并且 当前的App就是本地保存的宿主 同时 活着的IM服务所属APP 也是当前活着的宿主  则设置宿主为当前的App
     *      （3）、如果本地没有保存宿主App包名 当前活着的Im服务就是当前的App 则设置当前的App为宿主App
     */
    public static void getHostServiceInfo(Context context, HostServiceInfo hostServiceInfo) {
        String runningServicePkgName = getRunningServicePkgName(context);
        if (TextUtils.isEmpty(runningServicePkgName)) {
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, "runningServicePkgName is null.");
            // 说明此刻没有ConnectionService服务正在运行，直接返回了一个null,这么处理会存在问题，当切换网络之类的无法再次启动服务，导致永远无法收到消息
            String currentPackageName = context.getPackageName();
            String hostPackageName = PublicValueStoreUtil.getHostPackageName();
            if (currentPackageName.equals(hostPackageName)) {
                hostServiceInfo.setServicePkgName(hostPackageName);
                LogUtils.w(TAG, LogTagConfig.LOG_TAG_POINT_SERVICE, "HostService", "Get host service info when current package name equals stored "
                        + "host name but is not running . >>> 【 Host :" + hostPackageName + "】");
            }
            return;
        }

        String localPkgName = context.getPackageName();
        String hostPackageName = PublicValueStoreUtil.getHostPackageName();
        if (!TextUtils.isEmpty(hostPackageName)) {
            if (runningServicePkgName.equals(hostPackageName)) {
                if (localPkgName.equals(hostPackageName)) {
                    hostServiceInfo.setServicePkgName(runningServicePkgName);
                    LogUtils.i(TAG, LogTagConfig.LOG_TAG_POINT_SERVICE, "HostService", "Get host service info when [runningServicePkgName] = " +
                            "[hostPackageName] = [localPkgName] = 【 hostPackageName 】=【 " + hostPackageName + " 】");
                }
            }
        } else if (runningServicePkgName.equals(localPkgName)) {
            hostServiceInfo.setServicePkgName(runningServicePkgName);
            LogUtils.i(TAG, LogTagConfig.LOG_TAG_POINT_SERVICE, "HostService", "Get host service info when there is no stored hostService but " +
                    "[runningServicePkgName] = " + "  【 localPkgName 】=【 " + localPkgName + " 】");
        } else {
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE, "HostService", "getHostServiceInfo failed,runningServicePkgName:" +
                    runningServicePkgName + ", current pkgName:" + context.getPackageName());
        }
    }
}
