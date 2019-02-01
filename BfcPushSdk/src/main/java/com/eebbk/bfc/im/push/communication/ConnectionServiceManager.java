package com.eebbk.bfc.im.push.communication;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.IConnectCallback;
import com.eebbk.bfc.im.push.IConnectionService;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.host.HostServiceElection;
import com.eebbk.bfc.im.push.service.task.TaskType;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.bean.SyncRegistInfo;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnServiceConnectionListener;
import com.eebbk.bfc.im.push.listener.OnSpecialConnectListener;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.util.StoreUtil;

/**
 * 对push进程维护连接的service提供aidl接口，原则上aidl接口的
 * 调用要越少越好，所以可以通过缓存的方式来降低aidl接口的调用次数
 */
public class ConnectionServiceManager {

    /**
     * 是否采用service复用(TCP多路复用)
     */
    public static boolean IS_PUBLIC_SERVICE = true;

    /**
     * SDK全局环境
     */
    private SyncApplication app;

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

    private volatile boolean shutdown;

    private OnServiceConnectionListener onServiceConnectionListener;

    private HostServiceElection hostServiceElection;

    public ConnectionServiceManager(SyncApplication app, OnServiceConnectionListener onServiceConnectionListener) {
        this.context = app.getContext();
        this.app = app;
        this.onServiceConnectionListener = onServiceConnectionListener;
        iConnServiceConnection = new IConnServiceConnection();
        initConnectionServiceIntent();
        aidlHelper = new AIDLHelper(this);
        hostServiceElection = new HostServiceElection(this,context);
    }

    private void initConnectionServiceIntent() {
        String packageName=app.getHostServicePackageName();
        if(packageName!=null){
            serviceIntent = ConnectionService.createIntent(context, packageName);
        }else{
            String temp=context.getPackageName();
            serviceIntent = ConnectionService.createIntent(context, temp);
            if(app.putHostServicePackageName(temp)){
                LogUtils.i("host service package name store success is " + temp);
            }else {
                LogUtils.e("host service package name store fail ,please check!!" );
            }
        }

        LogUtils.i("service intent component package:" + serviceIntent.getComponent().getPackageName());
    }

    /**
     * 开启IM连接
     */
    public void startConnect(boolean reconnect) {
        synchronized (this) {
            if (starting) {
                LogUtils.w("ConnectionServiceManager starting:" + starting);
                return;
            }
            starting = true;
        }
        shutdown = false;
        LogUtils.d("start connect to connection service...");
        if (startService()) {
            if (bindService(serviceIntent)) {
                if (reconnect) {
                    connectServer();
                }
            }
        }
        hostServiceElection.checkUpOwnService(context);
        hostServiceElection.checkUpHostService(context);
        starting = false;
    }

    public void electHostService() {
        hostServiceElection.electHostService(context);
    }

    public void checkUpHostService() {
        hostServiceElection.checkUpHostService(context);
    }

    private void setPushDebugMode() {
        Intent intent = new Intent(serviceIntent);
        intent.putExtra(TaskType.TAG, TaskType.DEBUG_MODE);
        intent.putExtra("debug_mode", LogUtils.isDebug());
        context.startService(intent);
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

    private boolean startService() {
        boolean started = false;
        ComponentName componentName = context.startService(serviceIntent);
        if (componentName != null) {
            started = true;
            LogUtils.i("start connection service successfully,componentName pkgName:" + componentName.getPackageName());
        } else {
            // 6.0系统禁止互相唤醒后启动远程服务会失败,所以只能启动本地服务
            LogUtils.e("start connection service fail,the device may forbid to start remote service,to start local service:" + context.getPackageName());
            serviceIntent = ConnectionService.createIntent(context, context.getPackageName());
            ComponentName cn = context.startService(serviceIntent);
            if (cn != null) {
                started = true;
                String temp=cn.getPackageName();
                LogUtils.d("start local connection service success:" + temp);
                if(app.putHostServicePackageName(temp)){
                    LogUtils.i("host service package name change success is " + temp);
                }else {
                    LogUtils.e("host service package name store fail ,please check!!" );
                }
            } else {
                LogUtils.e("start local connection service fail...");
            }
        }
        return started;
    }

    private boolean bindService(Intent intent) {
        boolean binded = context.bindService(intent, iConnServiceConnection, Service.BIND_AUTO_CREATE);
        ComponentName cn = intent.getComponent();
        String bindPkg = null;
        if (cn != null) {
            bindPkg = cn.getPackageName();
        }
        if (binded) {
            LogUtils.i("bind connection service successfully,bind pkgName:" + bindPkg);
        } else {
            LogUtils.e("bind connection service fail,the device may forbid to bind remote service");
        }
        return binded;
    }

    public SyncRegistInfo getSyncRegistInfo() {
        SyncRegistInfo syncRegistInfo = aidlHelper.call(new AIDLTaskImpl<SyncRegistInfo>() {
            @Override
            public SyncRegistInfo submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("submit getSyncRegisitInfo on aidl");
                return iConnectionService.getSyncRegisitInfo();
            }
        });
        if (syncRegistInfo == null) {
            syncRegistInfo = SyncRegistInfo.EMPTY_SYNC_REGISTINFO;
            LogUtils.w("use an empty SyncRegistInfo object.");
        }
        return syncRegistInfo;
    }

    public void getSyncRegistInfoSafely(final OnGetCallBack<SyncRegistInfo> onGetCallBack) {
        aidlHelper.waitForRun(new AIDLTaskImpl<SyncRegistInfo>() {
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("execute getSyncRegisitInfo on aidl");
                SyncRegistInfo syncRegistInfo = iConnectionService.getSyncRegisitInfo();
                if (syncRegistInfo == null) {
                    syncRegistInfo = SyncRegistInfo.EMPTY_SYNC_REGISTINFO;
                    LogUtils.w("use an empty sync regist info object.");
                }
                if (SyncApplication.checkRegistInfo(syncRegistInfo)) { // 如果是宿主app，此处会重复保存，因为在push进程已经保存过了
                    StoreUtil.saveRegistInfo(app.getPlatform().getStore(), syncRegistInfo);
                }
                onGetCallBack.onGet(syncRegistInfo);
            }
        });
    }


    public boolean isLogined() {
        Boolean isLoginedObj = aidlHelper.call(new AIDLTaskImpl<Boolean>() {
            @Override
            public Boolean submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("submit isLogined on aidl");
                return iConnectionService.isLogined();
            }
        });
        if(isLoginedObj == null) {
            isLoginedObj = Boolean.FALSE;
        }
        return isLoginedObj.booleanValue();
    }

    public boolean hasPublicKey() {
        Boolean hasPublicKeyObj = aidlHelper.call(new AIDLTaskImpl<Boolean>() {
            @Override
            public Boolean submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("execute hasPublicKey on aidl");
                return iConnectionService.hasPublicKey();
            }
        });
        if (hasPublicKeyObj == null) {
            hasPublicKeyObj = Boolean.FALSE;
        }
        return hasPublicKeyObj.booleanValue();
    }

    public void addBackupServerInfo(final String[] serverInfo, final boolean clearBefore) {
        aidlHelper.waitForRun(new AIDLTaskImpl() {
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException, RuntimeException {
                LogUtils.v("execute addBackupServerInfo on aidl");
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
                LogUtils.v("execute enqueueSendTask on aidl");
                iConnectionService.enqueueSendTask(request.getSendTime(), request.getRequestEntity().toByteArray(), request.getTimeout());
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
                LogUtils.v("execute heartbeat on aidl");
                iConnectionService.heartbeat();
            }
        });
    }

    /**
     * IM连接是否断开
     * @return true表示断开，false表示未断开
     */
    public boolean isClosed() {
        Boolean isClosedObj = aidlHelper.call(new AIDLTaskImpl<Boolean>(){
            @Override
            public Boolean submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("submit isClosed on aidl");
                return iConnectionService.isClosed();
            }
        });
        if (isClosedObj == null) {
            isClosedObj = Boolean.TRUE;
        }
        return isClosedObj.booleanValue();
    }

    public String getRealHostname() {
        String realHostname = aidlHelper.call(new AIDLTaskImpl<String>(){
            @Override
            public String submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("submit getHostname on aidl");
                return iConnectionService.getHostname();
            }
        });
        return realHostname;
    }

    public int getRealPort() {
        Integer realPortObj = aidlHelper.call(new AIDLTaskImpl<Integer>(){
            @Override
            public Integer submit(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("submit getPort on aidl");
                return iConnectionService.getPort();
            }
        });
        if (realPortObj == null) {
            return 0;
        }
        return realPortObj;
    }


    public void clearBackupServerInfo() {
        aidlHelper.run(new AIDLTaskImpl(){
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("execute clearServerInfo on aidl");
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

    public void specialConnectServer(final String hostname, final int port, final OnSpecialConnectListener onSpecialConnectListener) {
        aidlHelper.waitForRun(new AIDLTaskImpl(){
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("execute connect on aidl");
                if (TextUtils.isEmpty(hostname) || port <= 0 || port > 65535) {
                    LogUtils.test("connectServer error host and port:" + app.getHostname() + ":" + app.getPort());
                    return;
                }
                iConnectionService.connect(hostname, port, new IConnectCallback.Stub() {
                    @Override
                    public void onConnected(String hostname, int port) throws RemoteException {
                        if (onSpecialConnectListener != null) {
                            onSpecialConnectListener.onConnectSuccess(hostname, port);
                        }
                    }

                    @Override
                    public void onFailed(String hostname, int port, String errorMsg) throws RemoteException {
                        if (onSpecialConnectListener != null) {
                            onSpecialConnectListener.onConnectFailed(hostname, port, errorMsg);
                        }
                    }
                });
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
                LogUtils.v("execute connect on aidl");
                if (TextUtils.isEmpty(app.getHostname()) || app.getPort() <= 0 || app.getPort() > 65535) {
                    LogUtils.test("connectServer error host and port:" + app.getHostname() + ":" + app.getPort());
                    return;
                }
                iConnectionService.connect(app.getHostname(), app.getPort(), null);
            }
        });
    }

    private void closeConnection() {
        aidlHelper.run(new AIDLTaskImpl(){
            @Override
            public void execute(IConnectionService iConnectionService) throws RemoteException {
                LogUtils.v("execute close on aidl");
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
            LogUtils.w("reLogin is too frequently!");
        }
    }

    /**
     * 停止当前IM的连接服务并断开TCP连接
     */
    public void shutdown() {
        closeConnection();
        stopService(serviceIntent);
        shutdown = true;
    }

    /**
     * 停止连接服务
     */
    private void stopService(Intent intent) {
        if (iConnServiceConnection != null) {
            try {
                context.unbindService(iConnServiceConnection);
                iConnectionService = null;
                aidlHelper.setIConnectionService(null);
            } catch (Exception e) {
                LogUtils.e("unbindService error:" + e.toString());
            }
        } else {
            LogUtils.w("iConnServiceConnection is null.");
        }
        context.stopService(intent);
        LogUtils.d("stop the TCPConnection Service");
    }

    public final class IConnServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(final ComponentName name, IBinder service) {
            LogUtils.d(name.getPackageName() + ":connect to service successfully.");
            iConnectionService = IConnectionService.Stub.asInterface(service);
            aidlHelper.setIConnectionService(iConnectionService);
            if (onServiceConnectionListener != null) {
                onServiceConnectionListener.onServiceConnected();
            }
            if (isClosed()) {
                connectServer();
            } else {
                if (isLogined()) {
                    app.callOnPushLogin(app.getmSyncRegistInfo().getRegistId());
//                    app.callOnInitSuccessListener();
                    // TODO: 2016/10/11  别名标签
                    app.setAliasAndTagRequest(null,null,null);
                    LogUtils.d("is logined init into herrer");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.w(name.getPackageName() + " disconnect from service.");
            iConnectionService = null;
            aidlHelper.setIConnectionService(null);
            if (onServiceConnectionListener != null) {
                onServiceConnectionListener.onServiceDisconnected();
            }
        }
    }

}
