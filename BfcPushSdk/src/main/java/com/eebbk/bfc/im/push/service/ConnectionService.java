package com.eebbk.bfc.im.push.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
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
import com.eebbk.bfc.im.push.code.DataCoderUtil;
import com.eebbk.bfc.im.push.communication.BaseHandleService;
import com.eebbk.bfc.im.push.service.dispatcher.HandleServiceNotify;
import com.eebbk.bfc.im.push.service.tcp.ReadAndWriteDataThread;
import com.eebbk.bfc.im.push.service.heartbeat.ScreenStateReceiver;
import com.eebbk.bfc.im.push.service.heartbeat.HeartBeatReceiver;
import com.eebbk.bfc.im.push.service.heartbeat.HeartBeatScheduler;
import com.eebbk.bfc.im.push.service.host.HostInfoManager;
import com.eebbk.bfc.im.push.service.host.HostServiceInfo;
import com.eebbk.bfc.im.push.service.task.SendTask;
import com.eebbk.bfc.im.push.service.task.Task;
import com.eebbk.bfc.im.push.service.task.TaskExecutor;
import com.eebbk.bfc.im.push.service.tcp.Connection;
import com.eebbk.bfc.im.push.service.tcp.ConnectionProcessContext;
import com.eebbk.bfc.im.push.service.tcp.TCPConnection;
import com.eebbk.bfc.im.push.util.SettingStoreUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.bean.AppPushInfo;
import com.eebbk.bfc.im.push.bean.HostInfo;
import com.eebbk.bfc.im.push.bean.PushCollectInfo;
import com.eebbk.bfc.im.push.bean.SyncRegistInfo;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.HeartBeatRequestEntity;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.entity.response.LoginResponseEntity;
import com.eebbk.bfc.im.push.entity.response.PublicKeyResponseEntity;
import com.eebbk.bfc.im.push.entity.response.RegistResponseEntity;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.entity.response.SendErrorResponseEntity;
import com.eebbk.bfc.im.push.exception.ConnectException;
import com.eebbk.bfc.im.push.exception.DecodeException;
import com.eebbk.bfc.im.push.exception.WriteDataException;
import com.eebbk.bfc.im.push.service.host.httpdns.HttpDnsClient;
import com.eebbk.bfc.im.push.listener.OnConnectListener;
import com.eebbk.bfc.im.push.util.platform.Store;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;
import com.eebbk.bfc.im.push.service.dispatcher.StatusCode;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.service.task.TaskType;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.exception.CrashExceptionCatcher;
import com.eebbk.bfc.im.push.util.DateFormatUtil;
import com.eebbk.bfc.im.push.util.StoreUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
     */
    private Map<String, AppPushInfo> bindPkgNameMap = new ConcurrentHashMap<>();

    /**
     * 设备注册信息
     */
    private SyncRegistInfo syncRegistInfo;

    private Connection connection;

    private Store store;

    private String hostname;

    private int port;

    private static final int notificationId = 1314;

    private static final int HEART_BEAT_TIMEOUT = 1;

    private Intent wakefulIntent;

    private ScreenStateReceiver screenStateReceiver;

    private ServiceHandler serviceHandler;

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
                LogUtils.w("connection service has been gc!");
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
        LogUtils.w("heartbeat no response on time,try connect...");
        releaseConnection();
        connect(hostname, port, true, null);
        BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    public static class InnerService extends Service {

        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(notificationId, new Notification());
            LogUtils.i("inner service onCreate...");
            stopSelf();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            LogUtils.i("inner service onStartCommand...");
            return START_NOT_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            LogUtils.i("inner service destroy!");
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
            Intent intent = new Intent(SyncAction.HEARTBEAT_REQUEST);
            intent.setPackage(getPackageName());
            intent.putExtra(HeartBeatReceiver.REDUNDANCY, true);
            sendBroadcast(intent);
        }

        @Override
        public SyncRegistInfo getSyncRegisitInfo() throws RemoteException {
            return ConnectionService.this.getSyncRegisitInfo();
        }

        @Override
        public boolean isLogined() throws RemoteException {
            return ConnectionService.this.isLogined();
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
            HeartBeatScheduler.setHeartbeatPeriod(minHeart, maxHeart, heartStep);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                return super.onTransact(code, data, reply, flags);
            } catch(RuntimeException e) {
                LogUtils.e("AIDL接口异常,终于捕获到了");
                LogUtils.e(e);
                throw e;
            }
        }

    }

    private void connect(String hostname, int port, boolean cancel, IConnectCallback iConnectCallback) {
        if (TextUtils.isEmpty(hostname) || port <= 0 || port > 65535) {
            LogUtils.e("hostname or port is error,hostname:" + hostname + ",port:" + port);
            LogUtils.test("hostname or port is error,hostname:" + hostname + ",port:" + port);
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
        if (cancel) {
            connection.cancelConnect();
        }
        connection.connect(hostname, port, true, iConnectCallback);

        LogUtils.i("connect status:" + connection.isConnected());
    }

    private void enqueueSendTask(long sendTime, byte[] data, int timeout) {
        if (data == null || data.length == 0) {
            LogUtils.e("send data is empty!");
            return;
        }
        Task sendTask = new SendTask(this, sendTime, data, timeout);
        sendTaskExecutor.execute(sendTask);
    }

    private void executeHeartbeat(Intent intent) {
        if (connection == null) {
            LogUtils.e("connection is null.");
            BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(intent);
            return;
        }
        BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(wakefulIntent);
        if (!connection.isConnected()) {
            LogUtils.e("connection is closed.");
            BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(intent);
            connect(hostname, port, true, null);
            return;
        }
        if (intent != null) {
            wakefulIntent = intent;
        }
        LogUtils.d("executeHeartbeat...");
        serviceHandler.removeMessages(HEART_BEAT_TIMEOUT);
        Message msg = serviceHandler.obtainMessage();
        msg.what = HEART_BEAT_TIMEOUT;
        msg.obj = intent;
        serviceHandler.sendMessageDelayed(msg, HeartBeatScheduler.TIMEOUT); // 心跳包20秒超时
        HeartBeatRequestEntity requestEntity = new HeartBeatRequestEntity();
        SendTask sendTask = new SendTask(this, System.currentTimeMillis(), requestEntity.toByteArray(), HeartBeatScheduler.TIMEOUT);
        sendTask.setIntent(intent);
        sendTaskExecutor.execute(sendTask);
    }

    public SyncRegistInfo getSyncRegisitInfo() {
        return syncRegistInfo;
    }

    /**
     * 设备是否登录
     */
    public boolean isLogined() {
        return syncRegistInfo.isLogin();
    }

    /**
     * 数据发送
     */
    public int send(RequestEntity requestEntity) {
        if (requestEntity == null) {
            LogUtils.e("send data entity is null");
            return StatusCode.DATA_SEND_NULL_ERROR;
        }
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
        HeartBeatScheduler.clearHeartbeatInfo();
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
            LogUtils.w("addBackupServerInfo:serverInfo is empty.");
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
        LogUtils.i("add serverInfo success:" + hostInfoManager.toString());
//        parseAndAddServerInfo(serverInfo, hostInfoManager); // 如果ip是域名，则把域名转换成ip后加入到预埋ip列表
    }

    private void clearBackupServerInfo() {
        HostInfoManager.getInstance().clear();
    }

    public static Intent createIntent(Context context, String componentPckName) {
        Intent serviceIntent = new Intent();
        serviceIntent.setPackage(context.getPackageName());
        serviceIntent.setComponent(new ComponentName(componentPckName, ConnectionService.class.getName()));
        serviceIntent.setAction(ConnectionService.ACTION);
        serviceIntent.addFlags(Intent. FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
        return serviceIntent;
    }

    @Override
    public void onCreate() {
//        initLogUtil();
        CrashExceptionCatcher.getInstance().init(this);
        super.onCreate();
        serviceCreateTime = System.currentTimeMillis();
        CREATE_SERVICE_PACKAGE_NAME = getPackageName();
        sendTaskExecutor = new TaskExecutor(1024);

        store = new PhoneStore(this);

        initHandler();

        loadData();

        startForeground(notificationId, new Notification());
        startInnerService();

        registScreenStateReceiver();
        LogUtils.d("TCPConnection Service has created,CREATE_SERVICE_PACKAGE_NAME:" + CREATE_SERVICE_PACKAGE_NAME);
    }

    private void initHandler() {
        HandlerThread handlerThread = new HandlerThread("ConnectionService_Handler");
        handlerThread.start();
        serviceHandler = new ServiceHandler(handlerThread.getLooper(), this);
    }

    private void registScreenStateReceiver() {
        screenStateReceiver = new ScreenStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStateReceiver, intentFilter);
    }

//    /**
//     * 宿主选举
//     */
//    private boolean selectHostService(String createdPkgName) {
//        boolean created = true;
//        String hostPkgName = StoreUtil.readHostPkgName(store);
//        if (TextUtils.isEmpty(hostPkgName)) {
//            hostPkgName = createdPkgName;
//            StoreUtil.saveHostPkgName(store, hostPkgName);
//            created = true;
//        } else {
//            if (hostPkgName.equals(createdPkgName)) {
//                created = true;
//            } else {
//                created = false;
//            }
//        }
//        return created;
//    }

//    private void initLogUtil() {
//        // 日志设置
//        LogUtils.setDebugMode(true); // 调试模式
//        LogUtils.init(getApplicationContext(), null, true, null, false); // 初始化日志配置
//        LogUtils.setSaveMode(false); // 是否保存日志到文件
//        LogUtils.setSaveLevel(false, true, true, true, true); // 保存日志存文件的级别
//    }

    private void loadData() {
        // 加载设备注册信息
        StoreUtil.saveRegistInfo(store, false); // 首先把文件中的设备登录状态还原
        syncRegistInfo = StoreUtil.readRegistInfo(store);


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
        LogUtils.i("loadData bindPkgNameMap:" + bindPkgNameMap);
    }

    private void startInnerService() {
        Intent intent = new Intent(this, InnerService.class);
        startService(intent);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        LogUtils.i(this.getClass().getSimpleName() + ".onStartCommand,pkgName:" + getPackageName());
        if (intent != null) {
            int taskType = intent.getIntExtra(TaskType.TAG, 0);
            LogUtils.i("start connection service task type:" + taskType);
            switch (taskType) {
                case TaskType.HEART_BEAT:
                    executeHeartbeat(intent);
                    recordConnectInfo(hostname, port, false);
                    break;
                case TaskType.ALARM_CONNECT:
                    alarmConnect(intent);
                    break;
                case TaskType.APP_REMOVED:
                    String pkgName = intent.getStringExtra("package_name");
                    LogUtils.w("remove pkgName:" + pkgName);
                    if (!pkgName.equals(getPackageName())) {
                        unbindAppPushInfo(pkgName);
                    }
                    break;
                case TaskType.DEBUG_MODE:
                    boolean debugMode = intent.getBooleanExtra("debug_mode", false);
                    LogUtils.setDebugMode(debugMode);
                    break;
                case TaskType.DEFAULT:
                    LogUtils.d("default do nothing");
                    break;
                default:
                    LogUtils.w("unknow task type:" + taskType);
            }
        } else {
            LogUtils.d("ConnectionService.onStartCommand");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void recordConnectInfo(String hostname, int port, boolean connecting) {
        TCPConnection tcpConnection = (TCPConnection) connection;
        if (tcpConnection != null) {
            long connectedTime = tcpConnection.getConnectedTime();
            long disconnected = tcpConnection.getDisconnectedTime();
            long duration = tcpConnection.getCurrentConnectedDuration();
            boolean connected = tcpConnection.isConnected();
            boolean isLogined = isLogined();
            PushCollectInfo pushCollectInfo = new PushCollectInfo(this, serviceCreateTime, destroyed, connectedTime, disconnected, duration);
            pushCollectInfo.setPushConnected(connected);
            pushCollectInfo.setPushLogined(isLogined);
            pushCollectInfo.setRegisterId(syncRegistInfo.getRegistId());
            pushCollectInfo.setHostname(hostname);
            pushCollectInfo.setPort(port);
            pushCollectInfo.setConnecting(connecting);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[是否有心跳:" + !HeartBeatScheduler.isCanceled() + "],");
            stringBuilder.append("[短心跳:" + HeartBeatScheduler.isShortHeartbeat() + "," + HeartBeatScheduler.getShortHeartbeat() + "],");
            stringBuilder.append("[探测心跳:" + HeartBeatScheduler.isProbeHeartbeat() + "," + HeartBeatScheduler.getCurrentHeartbeat() + "],");
            stringBuilder.append("[稳定心跳:" + HeartBeatScheduler.isStableHeartbeat() + "," + HeartBeatScheduler.getStableHeartbeat() + "]");
            pushCollectInfo.setHeartbeatInfo(stringBuilder.toString());
            HandleServiceNotify.notifyOnPushCollectHandleService(this, pushCollectInfo);
            LogUtils.i("pushCollectInfo:" + pushCollectInfo);
        } else {
            LogUtils.e("connection is null.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.i("TCPConnection Service onBind:" + intent.getPackage());
        if (intent != null) {
            bindAppPushInfo(intent.getPackage(), true);
        }
        return mBinder.asBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        LogUtils.i("TCPConnection Service onRebind:" + intent.getPackage());
        if (intent != null) {
            bindAppPushInfo(intent.getPackage(), true);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (intent != null) {
            LogUtils.e("TCPConnection Service has unbinded,action:" + intent.getAction() + ",packageName:" + intent.getPackage());
        }
        /*if (intent != null && intent.getPackage() != null) {
            unbindAppPushInfo(intent.getPackage());
        }*/
        return true;
    }

    @Override
    public void onDestroy() {
        HeartBeatScheduler.clear(this);
        serviceHandler.getLooper().quit();
        sendTaskExecutor.shutdown();
        HttpDnsClient.clear();
        clear();
        unregisterReceiver(screenStateReceiver);
        stopForeground(true);
        destroyed = true;
        LogUtils.e("TCPConnection Service has destroyed:" + getPackageName());
        reStartService();
        super.onDestroy();
        HandleServiceNotify.notifyKillPushHandleService(this, Process.myPid());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void reStartService(){
        Intent intent = new Intent(SyncAction.RESTART_SERVICE_ACTION);
        intent.setPackage(this.getPackageName());
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 2020, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 6000, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 6000, pendingIntent);
        }
        LogUtils.i("reStartService alarm period is 6000ms");
    }

    protected boolean isDestroyed() {
        return destroyed;
    }

    private void bindAppPushInfo(String bindPkgName, boolean needStore) {
        if (TextUtils.isEmpty(bindPkgName)) {
            LogUtils.e("bindPkgName is null!");
            return;
        }

        Context bindPkgCtx = getContextByPkgName(bindPkgName);
        if (bindPkgCtx != null) {
            AppPushInfo appPushInfo = bindPkgNameMap.get(bindPkgName);
            if (appPushInfo == null) {
                LogUtils.i("BIND_PACKAGE_NAME:" + bindPkgName);
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
        LogUtils.i("bindPkgNameMap:" + bindPkgNameMap);
    }

    private void unbindAppPushInfo(String unbindPkgName) {
        if (TextUtils.isEmpty(unbindPkgName)) {
            return;
        }

        AppPushInfo appPushInfo = bindPkgNameMap.get(unbindPkgName);
        if (appPushInfo != null) {
            bindPkgNameMap.remove(unbindPkgName);
            StoreUtil.removeAppPushInfo(store, appPushInfo);
            LogUtils.i("bindPkgNameMap:" + bindPkgNameMap);
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

    public boolean hasSameThirdSyncRequestTask(SendTask sendTask) {
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
                if (alias.equals(srcEntity.getAlias())
                        && destEntity.getSyncKey() >= srcEntity.getSyncKey()) {
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
        LogUtils.d("handleSendTaskSuccess");
        RequestEntity requestEntity = sendTask.getRequestEntity();
        int command = requestEntity.getCommand();
        if (command == Command.PUSH_SYNC_FIN_ACK) {
            return;
        }
        if (command == Command.HEART_BEAT_REQUEST) {
            boolean isRedundancy = sendTask.getIntent().getBooleanExtra(HeartBeatReceiver.REDUNDANCY, false);
            LogUtils.i("isRedundancy heartbeat:" + isRedundancy);
            HeartBeatScheduler.setIsRedundancyHeartbeat(isRedundancy);
            LogUtils.d("send a heartbeat data successfully...");
        }
        synchronized (sendSuccessTaskList) {
            sendSuccessTaskList.add(sendTask);
        }
    }

    public void handleSendTaskError(int errorCode, SendTask sendTask) {
        LogUtils.e("handleSendTaskError");
        if (sendTask.getCommand() == Command.HEART_BEAT_REQUEST) {
            LogUtils.e("send heartbeat data error,try connect...");
            serviceHandler.removeMessages(HEART_BEAT_TIMEOUT);
            Intent intent = sendTask.getIntent();
            connect(hostname, port, true, null);
            BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(intent);
        } else {
            SendErrorResponseEntity sendErrorResponseEntity = new SendErrorResponseEntity();
            sendErrorResponseEntity.setRID(sendTask.getRequestEntity().getRID());
            sendErrorResponseEntity.setCode(Response.Code.SEND_ERROR);
            sendErrorResponseEntity.setDesc("send error,error code:" + errorCode);
            sendErrorResponseEntity.setRequestEntity(sendTask.getRequestEntity());
            deliveResponseEntity(sendErrorResponseEntity);
            /*try {
                Entity entity = TLVObjectUtil.parseEntity(TLVObjectUtil.parseByteArray(sendErrorResponseEntity));
                LogUtils.e("test entity:" + entity);
            } catch (Exception e) {
                LogUtils.e(e);
            }*/
        }
    }

    private void alarmConnect(Intent intent) {
        LogUtils.d("alarm connect...");
        String hostname = intent.getStringExtra("hostname");
        int port = intent.getIntExtra("port", 0);
        LogUtils.i("alarm hostname:" + hostname + ",port:" + port);
        if (connection != null) {
            TCPConnection tcpConnection = (TCPConnection) connection;
            tcpConnection.connect(hostname, port, true, null);
        } else {
            LogUtils.e("connection is null.");
            connect(hostname, port, true, null);
        }
        recordConnectInfo(hostname, port, true);
    }

    private void initOnConnectListenerForConnection(final TCPConnection tcpConnection) {
        tcpConnection.setOnConnectListener(new OnConnectListener() {

            @Override
            public void onStartConnect(String hostname, int port) {
                LogUtils.i("start connect,hostname:" + hostname + ",port:" + port);
                HandleServiceNotify.notifyStartConnectHandleService(ConnectionService.this, bindPkgNameMap, hostname, port);
            }

            @Override
            public void onConnected(byte[] socketSecretKey, String hostname, int port) {
                LogUtils.i("connect to server success,hostname:" + hostname + ",port:" + port);
                LogUtils.v("on thread:" + Thread.currentThread().getName());
                sendTaskExecutor.start();
                HandleServiceNotify.notifyConnectedHandleService(ConnectionService.this, bindPkgNameMap, hostname, port);
                ConnectionService.this.hostname = hostname;
                ConnectionService.this.port = port;
                ConnectionProcessContext.getInstance().setSecretKey(socketSecretKey);

                HeartBeatScheduler.start(ConnectionService.this);
                HeartBeatScheduler.startShortHeartbeat(ConnectionService.this);

                LogUtils.i("预埋IP信息:" + HostInfoManager.getInstance().toString());
            }

            @Override
            public void onDisconnected(boolean reconnect) {
                LogUtils.w("connection is disconnected");
                LogUtils.v("on thread:" + Thread.currentThread().getName());
                sendTaskExecutor.stop();
                serviceHandler.removeMessages(HEART_BEAT_TIMEOUT); // 取消心跳超时检测
                syncRegistInfo.setLogin(false);
                StoreUtil.saveRegistInfo(store, syncRegistInfo); // 把设备登录状态设置为未登录
                ConnectionProcessContext.getInstance().setEncrypt(false); // 数据加密状态设置为未加密

                handleHeartbeatOnDisconnected();

                HandleServiceNotify.notifyDisconnectedHandleService(ConnectionService.this, bindPkgNameMap);

                LogUtils.w("预埋IP信息:" + HostInfoManager.getInstance().toString());
            }

            @Override
            public void onFailed(ConnectException error, String hostname, int port) {
                LogUtils.e("connect fail,error msg:" + error.toString() + ",hostname:" + hostname + ",port:" + port);
                LogUtils.v("on thread:" + Thread.currentThread().getName());
                LogUtils.e("预埋IP信息:" + HostInfoManager.getInstance().toString());
                String errorMsg = "[" + hostname + ":" + port + "] connect failed:" + error.toString();
                HandleServiceNotify.notifyConnectFailedHandleService(ConnectionService.this, bindPkgNameMap, errorMsg, hostname, port);
            }
        });
    }

    private void initOnDataListenerForConnection(TCPConnection tcpConnection) {
        tcpConnection.setOnDataListener(new ReadAndWriteDataThread.OnDataListener() {
            @Override
            public void onWrite(int writeByte) {
                LogUtils.i("write [" + writeByte + "] bytes.");
                sweepSendRequestSuccessList(0);
            }

            @Override
            public void onWriteError(String errorMsg) {
                LogUtils.e("write error:" + errorMsg);
                sweepSendRequestSuccessList(0);
            }

            @Override
            public void onRead(byte[] data) {
                LogUtils.i("read [" + data.length + "] bytes");
                ResponseEntity responseEntity = null;
                try {
                    responseEntity = DataCoderUtil.decodeData(ConnectionService.this, data);
                    if (responseEntity != null) {
                        deliveResponseEntity(responseEntity);
                        sweepSendRequestSuccessList(responseEntity.getRID());
                    } else {
                        LogUtils.e("responseEntity is null.");
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

    private void handleHeartbeatOnDisconnected() {
        if (HeartBeatScheduler.isRedundancyHeartbeat()) {
            HeartBeatScheduler.setIsRedundancyHeartbeat(false);
        } else if (HeartBeatScheduler.isProbeHeartbeat()) {
            HeartBeatScheduler.probeHeartbeat(false);
        } else if (HeartBeatScheduler.isStableHeartbeat()) {
            HeartBeatScheduler.stableHeartbeat(this, false);
        }
        HeartBeatScheduler.resetHeartbeatInfo();
        HeartBeatScheduler.cancel(this);
    }

    private void sweepSendRequestSuccessList(int rid) {
        synchronized (sendSuccessTaskList) {
            Iterator<SendTask> iterator = sendSuccessTaskList.iterator();
            while (iterator.hasNext()) {
                SendTask sendTask = iterator.next();
                RequestEntity requestEntity = sendTask.getRequestEntity();
                if (requestEntity.getRID() == rid) {
                    iterator.remove();
                    LogUtils.i("remove a request entity:" + requestEntity + ",size:" + sendSuccessTaskList.size());
                } else if (sendTask.isTimeout()) {
                    iterator.remove();
                    LogUtils.i("remove a timeout request entity:" + requestEntity + ",size:" + sendSuccessTaskList.size());
                }
            }
        }
    }

    private void deliveResponseEntity(ResponseEntity responseEntity) {
        LogUtils.i("receive response entity:" + responseEntity);
        boolean filter = filterResponseEntity(responseEntity);
        if (filter) {
            return;
        }
        List<Context> contextList = ContextSelector.selectReveiveContexts(this, responseEntity,bindPkgNameMap);
        HandleServiceNotify.notifyMessageHandleService(this, responseEntity, contextList);
    }

//    /**
//     * 同一个进程中可以采用此方式来实现广播发送，避免注册静态广播
//     *
//     * @param context
//     * @param intent
//     */
//    private void sendBroadcastImpl(Context context, Intent intent) {
//        PackageManager packageManager = context.getPackageManager();
//        List<ResolveInfo> list = packageManager.queryBroadcastReceivers(intent, PackageManager.GET_INTENT_FILTERS);
//        LogUtils.i("List<ResolveInfo>:" + list);
//        ActivityInfo targetReceiver = null;
//        for (ResolveInfo resolveInfo : list) {
//            ActivityInfo activityInfo = resolveInfo.activityInfo;
//            if (activityInfo != null && activityInfo.packageName.equals(context.getPackageName())) {
//                targetReceiver = activityInfo;
//                break;
//            }
//        }
//
//        if (targetReceiver != null) {
//            String receiverName = targetReceiver.name;
//            try {
//                BroadcastReceiver receiver = (BroadcastReceiver) Class.forName(receiverName).newInstance();
//                receiver.onReceive(context, intent);
//            } catch (InstantiationException e) {
//                LogUtils.e(e);
//            } catch (IllegalAccessException e) {
//                LogUtils.e(e);
//            } catch (ClassNotFoundException e) {
//                LogUtils.e(e);
//            }
//        }
//    }

    private boolean filterResponseEntity(ResponseEntity responseEntity) {
        int command = responseEntity.getCommand();
        if (command == Command.HEART_BEAT_RESPONSE) {   // 处理心跳包,心跳包数据不往外发
            handleHeartbeatResponseSuccess();
            return true;
        } else {
            if (command != Command.SEND_ERROR_RESPONSE) {
                // 把除了本地发送失败的任意请求回馈(无论成功还是失败)包当作心跳包的响应
                serviceHandler.removeMessages(HEART_BEAT_TIMEOUT);
                HeartBeatScheduler.resetHeartbeat(this);
            }
        }

        if (command == Command.ENCRYPT_SET_RESPONSE) {
            handleEncryptSetResponse(responseEntity);
        } else if (command == Command.PUBLICKEY_RESPONSE) {
            handlePublicKeyResponse(responseEntity);
        } else if (command == Command.REGIST_RESPONSE) {
            handleRegistResponse(responseEntity);
        } else if (command == Command.LOGIN_RESPONSE) {
            handleLoginResponse(responseEntity);
        }
        return false;
    }

    private void handleHeartbeatResponseSuccess() {
        serviceHandler.removeMessages(HEART_BEAT_TIMEOUT);
        HeartBeatScheduler.setHeartbeatSuccessTime(System.currentTimeMillis());
        BaseHandleService.WakefulBroadcastReceiver.completeWakefulIntent(wakefulIntent);
        if (HeartBeatScheduler.isRedundancyHeartbeat()) { // 冗余心跳
            HeartBeatScheduler.setIsRedundancyHeartbeat(false);
        } else if (HeartBeatScheduler.isShortHeartbeat()) { // 短心跳
            HeartBeatScheduler.increaseShortHeartbeatSuccessCount();
        } else if (HeartBeatScheduler.isProbeHeartbeat()) { // 探测心跳
            // 这里比较特殊，第一个探测心跳可能会和最后一个短心跳搞乱掉，因此用下面的方法把这两种心跳的临界区分开来
            int shortHeartbeatSuccessCount = HeartBeatScheduler.getShortHeartbeatSuccessCount();
            int maxShortHeartbeatSuccessCount = HeartBeatScheduler.getMaxShortHeartSuccessCount();
            if (shortHeartbeatSuccessCount == maxShortHeartbeatSuccessCount) {
                HeartBeatScheduler.increaseShortHeartbeatSuccessCount();
            } else if (shortHeartbeatSuccessCount > maxShortHeartbeatSuccessCount) {
                HeartBeatScheduler.probeHeartbeat(true);
            }
        } else if (HeartBeatScheduler.isStableHeartbeat()) { // 稳定心跳
            // 这里比较特殊，第一个稳定心跳可能会和最后一个短心跳搞乱掉，因此用下面的方法把这两种心跳的临界区分开来
            int shortHeartbeatSuccessCount = HeartBeatScheduler.getShortHeartbeatSuccessCount();
            int maxShortHeartbeatSuccessCount = HeartBeatScheduler.getMaxShortHeartSuccessCount();
            if (shortHeartbeatSuccessCount == maxShortHeartbeatSuccessCount) {
                HeartBeatScheduler.increaseShortHeartbeatSuccessCount();
            } else if (shortHeartbeatSuccessCount > maxShortHeartbeatSuccessCount) {
                HeartBeatScheduler.stableHeartbeat(this, true);
            }
        } else {
            LogUtils.w("heartbeat type is error!");
        }
        LogUtils.d("heartbeat response on time:" + DateFormatUtil.format("HH:mm:ss", HeartBeatScheduler.getHeartbeatSuccessTime()));
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

    private void handleRegistResponse(ResponseEntity responseEntity) {
        RegistResponseEntity registResponseEntity = (RegistResponseEntity) responseEntity;
        if (registResponseEntity.getCode() == Response.Code.SUCCESS) {
            long registerId = registResponseEntity.getRegistId();
            syncRegistInfo.setRegistId(registerId);
            syncRegistInfo.setRegistToken(registResponseEntity.getRegistToken());
            StoreUtil.saveRegistInfo(store, syncRegistInfo);
        } else {
            LogUtils.e("regist fail:" + registResponseEntity);
        }
    }

    private void handleLoginResponse(ResponseEntity responseEntity) {
        LoginResponseEntity loginResponseEntity = (LoginResponseEntity) responseEntity;
        int code = loginResponseEntity.getCode();
        if (code == Response.Code.SUCCESS) {
            syncRegistInfo.setLogin(true);
            StoreUtil.saveRegistInfo(store, syncRegistInfo);
            HandleServiceNotify.notifyOnLoginHandleService(this, bindPkgNameMap, syncRegistInfo.getRegistId());
        } else if (code == Response.Code.UNKNOW_REGISTID || code == Response.Code.REGIST_TOKEN_INVALID) {
            syncRegistInfo = SyncRegistInfo.EMPTY_SYNC_REGISTINFO;
            StoreUtil.clearRegistInfo(store);
            LogUtils.e("login fail,unknow registId or registToken is invalid!");
        } else {
            syncRegistInfo.setLogin(false);
            StoreUtil.saveRegistInfo(store, syncRegistInfo);
            LogUtils.e("login fail:" + loginResponseEntity);
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
            LogUtils.d("data sent successfully.");
        } catch (WriteDataException error) {
            code = StatusCode.WRITEDATA_ERROR;
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

    public static String getRunningSetvicePkgName(Context context) {
        String runningServicePkgName = null;
        if (AppUtil.isServiceRunning(context,
                SettingStoreUtil.getHostPackgName(context) ,
                ConnectionService.class.getName())) {
            runningServicePkgName = SettingStoreUtil.getHostPackgName(context);
        } else if (AppUtil.isServiceRunning(context, context.getPackageName(), ConnectionService.class.getName())) {
            runningServicePkgName = context.getPackageName();
        }
        return runningServicePkgName;
    }

    public static void getHostServiceInfo(Context context, HostServiceInfo hostServiceInfo) {
        String runningServicePkgName = getRunningSetvicePkgName(context);
        if (TextUtils.isEmpty(runningServicePkgName)) {
            LogUtils.w("runningServicePkgName is null.");
            return;
        }
        String localPkgName = context.getPackageName();
        if (runningServicePkgName.equals(SettingStoreUtil.getHostPackgName(context))) {
            if (localPkgName.equals(SettingStoreUtil.getHostPackgName(context))) {
                hostServiceInfo.setServicePkgName(runningServicePkgName);
            }
        } else if (runningServicePkgName.equals(context.getPackageName())) {
            hostServiceInfo.setServicePkgName(runningServicePkgName);
        } else {
            LogUtils.w("getHostServiceInfo failed,runningServicePkgName:" + runningServicePkgName + ", current pkgName:" + context.getPackageName());
        }
    }
}
