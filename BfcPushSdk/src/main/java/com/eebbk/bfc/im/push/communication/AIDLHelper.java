package com.eebbk.bfc.im.push.communication;

import android.os.DeadObjectException;
import android.os.RemoteException;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.IConnectionService;
import com.eebbk.bfc.im.push.util.AsyncExecutorUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AIDL接口封装，负责专门处理AIDL接口调用
 */
public class AIDLHelper {

    private static final String TAG = "AIDLHelper";

    private volatile IConnectionService iConnectionService;
    private volatile String servicePackageName;

    private ConnectionServiceManager connectionServiceManager;

    private ReentrantLock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    public AIDLHelper(ConnectionServiceManager connectionServiceManager) {
        this.connectionServiceManager = connectionServiceManager;
    }

    public void setIConnectionService(IConnectionService iConnectionService, String servicePackageName) {
        this.iConnectionService = iConnectionService;
        this.servicePackageName = servicePackageName;
        if (iConnectionService != null) {
            signalAll();
        }
    }

    public IConnectionService getiConnectionService() {
        return iConnectionService;
    }

    public boolean isCurrConnectionService(String packageName){
        LogUtils.e(TAG, "packageName:" + packageName + ",servicePackageName:" + servicePackageName);
        if(TextUtils.isEmpty(servicePackageName)){
            return false;
        }
        return TextUtils.equals(packageName, servicePackageName);
    }

    /**
     * 有返回值的aidl接口调用
     */
    public  <T> T call(AIDLTaskImpl<T> task) {
        if (connectionServiceManager == null) {
            return null;
        }

        if (connectionServiceManager.isShutdown()) {
            return null;
        }
        T t = null;
        try {
            if (iConnectionService != null) {
                t = task.submit(iConnectionService);
            } else {
                retryStart(false);
                LogUtils.e(TAG, "call back when init is failed iConnectionService is null.");
            }
        } catch (RemoteException e) {
            LogUtils.e(e);
            dealDeadObjectException(e);
        } catch (RuntimeException e) {
            LogUtils.e(e);
        }
        return t;
    }

    /**
     * 有返回值的aidl接口调用
     */
    public  <T> T callOnInit(AIDLTaskImpl<T> task) {
        if (connectionServiceManager == null) {
            return null;
        }

        if (connectionServiceManager.isShutdown()) {
            return null;
        }
        T t = null;
        try {
            //如果为null 也直接返回为null 就不进行重试连接处理
            t = task.submit(iConnectionService);
        } catch (RemoteException e) {
            LogUtils.e(e);
            dealDeadObjectException(e);
        } catch (RuntimeException e) {
            LogUtils.e(e);
        }
        return t;
    }


    /**
     * 没有返回值的aidl接口调用
     */
    public  void run(AIDLTaskImpl task) {
        if (connectionServiceManager.isShutdown()) {
            return;
        }
        try {
            if (iConnectionService != null) {
                task.execute(iConnectionService);
            } else {
                retryStart(false);
                LogUtils.e(TAG, "call back when init is failed ,iConnectionService is null.");
            }
        } catch (RemoteException e) {
            LogUtils.e(e);
            dealDeadObjectException(e);
        } catch (RuntimeException e) {
            LogUtils.e(e);
        }
    }

    /**
     * 异步执行aidl接口
     */
    public  void waitForRun(final AIDLTaskImpl task) {
        if (connectionServiceManager.isShutdown()) {
            return;
        }
        if (iConnectionService != null) {
            toRun(task);
        } else {
            AsyncExecutorUtil.runOnBackground(new Runnable() {
                @Override
                public void run() {
                    if (connectionServiceManager.isShutdown()) {
                        return;
                    }
                    try {
                        waiting();
                        toRun(task);
                    } catch (InterruptedException e) {
                        LogUtils.e(e);
                    }
                }
            });
            retryStart(false);
        }
    }

    private  void toRun(AIDLTaskImpl task) {
        try {
            if (iConnectionService != null) {
                task.execute(iConnectionService);
            }
        } catch (RemoteException e) {
            LogUtils.e(e);
            dealDeadObjectException(e);
        } catch (RuntimeException e) {
            LogUtils.e(e);
        }
    }

    private void retryStart(boolean reconnect) {
        if (!connectionServiceManager.isStarting() && !connectionServiceManager.isShutdown()&&!connectionServiceManager.isBind()) {
            connectionServiceManager.startConnect(reconnect);
        }
    }

    private void dealDeadObjectException(RemoteException e) {
        if (e instanceof DeadObjectException) {
            if (iConnectionService == null) {
                retryStart(false);
            } else {
                retryStart(true);
            }
        }
    }

    private void waiting() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        LogUtils.d("wait for iConnectionService to init...");
        try {
            condition.await();
            LogUtils.d("iConnectionService init success...");
        } finally {
            lock.unlock();
        }
    }

    private void signalAll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
            LogUtils.d("signal all iConnectionService init waiting...");
        }
    }

}
