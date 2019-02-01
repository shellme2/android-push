package com.eebbk.bfc.im.push.service.tcp;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.IConnectCallback;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.host.HostInfoManager;
import com.eebbk.bfc.im.push.service.task.TaskExecutor;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.bean.HostInfo;
import com.eebbk.bfc.im.push.exception.ConnectException;
import com.eebbk.bfc.im.push.exception.WriteDataException;
import com.eebbk.bfc.im.push.listener.OnConnectInterruptListener;
import com.eebbk.bfc.im.push.listener.OnConnectListener;
import com.eebbk.bfc.im.push.service.task.TaskType;
import com.eebbk.bfc.im.push.util.IDUtil;
import com.eebbk.bfc.im.push.util.RandomUtil;
import com.eebbk.bfc.im.push.util.TimeFormatUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 维护一个TCP长连接，IO实
 */
public class TCPConnection implements Connection {

    private Context context;

    /**
     * 客户端Socket
     */
    private Socket socket;
    private InputStream is;
    private OutputStream os;

    /**
     * Socket数据流读写处理线程
     */
    private ReadAndWriteDataThread readWriteThread;

    /**
     * 主机信息管理
     */
    private HostInfoManager hostInfoManager;

    /**
     * 当前可用的主机信息
     */
    private volatile HostInfo canUseHostInfo;

    /**
     * TCP连接监听
     */
    private OnConnectListener onConnectListener;

    /**
     * 数据流读写监听
     */
    private ReadAndWriteDataThread.OnDataListener onDataListener;

    /**
     * TCP连接超时时间
     */
    private int timeout = 20000;

    /**
     * TCP是否连接标志，true表示已连接
     */
    private volatile boolean isConnected;

    private TaskExecutor connectTaskExecutor;

    /**
     * 两次的连接间隔的标志，计算连接间隔时间用
     */
    private AtomicInteger connectPeriodTag = new AtomicInteger(0);

    /**
     * 两次连接的时间间隔
     */
    private volatile float connectTaskPeriod;

    private AtomicInteger reconnectPeriodTag = new AtomicInteger(0);

    /**
     * 重连时间间隔
     */
    private volatile float reconnectTaskPeriod;

    /**
     * 记录上一次连接的时间
     */
    private long lastConnectTime;

    /**
     * 是否存在alarm唤醒重连任务
     */
    private volatile boolean alarmed = false;

    private long connectedTime;

    private long disconnectedTime;

    public TCPConnection(Context context, String hostname, int port) {
        this.context = context.getApplicationContext() == null ? context : context.getApplicationContext();
        initVar(hostname, port);
        LogUtils.d(this.getClass().getSimpleName() + " created...");
    }

    private void initVar(String hostname, int port) {
        connectTaskExecutor = new TaskExecutor(128, true);
        connectTaskExecutor.start();
        connectTaskPeriod = RandomUtil.getRandom(0, 1000);
        isConnected = false;
        hostInfoManager = HostInfoManager.getInstance();
        canUseHostInfo = new HostInfo(hostname, port);
        hostInfoManager.add(canUseHostInfo);
    }

    public Context getContext() {
        return context;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setLastConnectTime(long lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }

    public long getLastConnectTime() {
        return lastConnectTime;
    }

    public void setAlarmed(boolean alarmed) {
        this.alarmed = alarmed;
    }

    public boolean isAlarmed() {
        return alarmed;
    }

    /**
     * 添加连接监听
     *
     * @param onConnectListener
     */
    public void setOnConnectListener(OnConnectListener onConnectListener) {
        this.onConnectListener = onConnectListener;
    }

    /**
     * 添加数据发送和接收监听
     *
     * @param onDataListener
     */
    public void setOnDataListener(ReadAndWriteDataThread.OnDataListener onDataListener) {
        if (readWriteThread != null) {
            readWriteThread.setOnDataListener(onDataListener);
        } else {
            this.onDataListener = onDataListener;
        }
    }

    public void shutdownExecutor() {
        connectTaskExecutor.shutdown();
    }

    @Override
    public void connect(String hostname, int port) {
        connect(hostname, port, !alarmed, null);
    }

    @Override
    public void connect(String hostname, int port, boolean isAlarm, IConnectCallback iConnectCallback) {
        if (TextUtils.isEmpty(hostname) || port <= 0 || port > 65535) {
            LogUtils.e("hostname or port is error,hostname:" + hostname + ",port:" + port);
            LogUtils.test("hostname or port is error,hostname:" + hostname + ",port:" + port);
            return;
        }
        HostInfo hostInfo = hostInfoManager.get(hostname, port);
        if (hostInfo == null) {
            hostInfo = new HostInfo(hostname, port);
        }
        enqueueConnectTask(hostInfo, isAlarm, iConnectCallback);
    }

    @Override
    public void cancelConnect() {
        cancelAlarm(createPendingIntent(canUseHostInfo));
        reset();
    }

    @Override
    public void send(byte[] data) throws WriteDataException {
        if (readWriteThread != null) {
            readWriteThread.write(data);
        } else {
            LogUtils.e("readWriteThread is null.");
            throw new WriteDataException("readWriteThread is null.");
        }
    }

    /**
     * 中断长连接并释放TCP连接所占的资源
     */
    @Override
    public void close() {
        synchronized (this) {
            isConnected = false;
            if (readWriteThread != null) {
                readWriteThread.shutdown();
            }
            connectTaskExecutor.cancelAll();
            hostInfoManager.clear();
            closeSocket();
        }
        cancelAlarm(createPendingIntent(canUseHostInfo));
        LogUtils.d("connection has close.");
    }

    @Override
    public void releaseConnection() {
        synchronized (this) {
            isConnected = false;
            if (readWriteThread != null) {
                readWriteThread.shutdown();
            }
            closeSocket();
        }
        LogUtils.d("connection has release.");
    }

    /**
     * 把一个连接任务放入连接队列
     * @param hostInfo 主机信息
     */
    private void enqueueConnectTask(HostInfo hostInfo, boolean isAlarm, IConnectCallback iConnectCallback) {
        ConnectTask task = new ConnectTask(hostInfo, isAlarm, this);
        task.setConnectCallback(iConnectCallback);
        connectTaskExecutor.execute(task);
    }

    private void afterConnected() {
        connectedTime = System.currentTimeMillis();
        isConnected = true;
        reset();
    }

    /**
     * 把一切连接过程中的改变状态全部重置为初始状态
     */
    private void reset() {
        connectTaskExecutor.cancelAll();
        connectPeriodTag.set(0); //连接上后把连接间隔清零
        LogUtils.i("to set the connectPeriodTag as 0:" + connectPeriodTag.get() + ",connectTaskPeriod:" + connectTaskPeriod);
    }

    protected void resetConnectPeriodTag() {
        connectPeriodTag.set(0);
    }

    /**
     * 开启一个数据读写线程
     * @param in
     * @param out
     * @param hostInfo
     */
    private void startReadAndWriteThread(InputStream in, OutputStream out, final HostInfo hostInfo) {
        if (readWriteThread != null && readWriteThread.isAlive()) {
            readWriteThread.shutdown();
        }
        readWriteThread = new ReadAndWriteDataThread(in, out,
                new OnConnectInterruptListener() {
                    @Override
                    public void onInterrupt(final boolean reconnect) {
                        LogUtils.d("reconnect:" + reconnect);
                        isConnected = false;
                        hostInfo.increaseDisconnectedCount();
                        computeConnectedDuration();
                        callOnDisconnected(reconnect);
                        if (reconnect) {
                            // 如果连接异常中断则进行重连，如果是调用close()方法则不进行重连
                            reconnect();
                        } else {
                            LogUtils.d("close connection successfully.");
                        }
                    }
                });
        if (onDataListener != null) {
            readWriteThread.setOnDataListener(onDataListener);
        }
        readWriteThread.setName("Socket data read and write threat");
        readWriteThread.start();
    }

    private void computeConnectedDuration() {
        disconnectedTime = System.currentTimeMillis();
        long connectedDuration = disconnectedTime - connectedTime;
        LogUtils.w("connection is disconnected,connected time is:" + connectedTime
                + ",disconnected time is:" + disconnectedTime
                + ",connected time of duration is:" + connectedDuration
                + ",connectedDuration format:" + TimeFormatUtil.format(connectedDuration));
    }

    public long getCurrentConnectedDuration() {
        long duration = 0;
        if (isConnected()) {
            duration = System.currentTimeMillis() - connectedTime;
        } else {
            duration = disconnectedTime - connectedTime;
        }
        return duration;
    }

    public long getConnectedTime() {
        return connectedTime;
    }

    public long getDisconnectedTime() {
        return disconnectedTime;
    }

    /**
     * 开始连接
     * @param hostInfo
     * @throws IOException
     */
    public synchronized void connectImpl(HostInfo hostInfo) throws IOException {
        if (hostInfo == null) {
            LogUtils.e("hostinfo is null.");
            throw new IOException("hostinfo is null.");
        }
        LogUtils.d("to close the previous socket before create a new socket...");
        releaseConnection();
        callOnStartConnect(hostInfo.getHostname(), hostInfo.getPort());
        Throwable myError = null;
        hostInfo.inscreaseConnectCount();
        socket = new Socket();
        try {
            long start = System.currentTimeMillis();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(hostInfo.getHostname(), hostInfo.getPort());
            long end = System.currentTimeMillis();
            LogUtils.e("inetSocketAddress时间间隔(ms):" + (end - start));

            long startConn = System.currentTimeMillis();
            socket.connect(inetSocketAddress, timeout);
            long endConn = System.currentTimeMillis();
            LogUtils.e("connect时间间隔(ms):" + (endConn - startConn));

            is = socket.getInputStream();
            os = socket.getOutputStream();
            afterConnected();
            startReadAndWriteThread(is, os, hostInfo);
            callOnConnected(IDUtil.getUUID().getBytes(), hostInfo.getHostname(), hostInfo.getPort());
            LogUtils.i("connect to server success {remoteAddress=" + hostInfo.getHostname() + ",remotePort=" + hostInfo.getPort() + "}");
        } catch (IOException e) {
            myError = e;
            throw e;
        } catch (Throwable e) {
            myError = e;
            LogUtils.e(e);
        } finally {
            if (myError == null) {
                if (isConnected()) {
                    hostInfo.increaseSuccessCount();
                    this.canUseHostInfo = hostInfo;
                    cancelAlarm(createPendingIntent(canUseHostInfo));
                }
            } else {
                hostInfo.increaseFailCount();
            }
        }
    }

    public synchronized void callOnConnected(byte[] socketSecretKey, String hostname, int port) {
        if (onConnectListener != null) {
            onConnectListener.onConnected(socketSecretKey, hostname, port);
        }
    }

    public synchronized void callOnStartConnect(String hostname, int port) {
        if (onConnectListener != null) {
            onConnectListener.onStartConnect(hostname, port);
        }
    }

    public synchronized void callOnDisconnected(boolean reconnect) {
        if (onConnectListener != null) {
            onConnectListener.onDisconnected(reconnect);
        }
    }

    public synchronized void callOnConnectFailed(ConnectException error, String hostname, int port) {
        if (onConnectListener != null) {
            onConnectListener.onFailed(error, hostname, port);
        }
    }

    public void startRetryAlarm(HostInfo hostInfo) {
        randomConnectPeriod();
        PendingIntent pendingIntent = createPendingIntent(hostInfo);
        startAlarm(pendingIntent, (long) connectTaskPeriod);
        LogUtils.d("start retry alarm");
    }

    private void startReconnectAlarm(HostInfo hostInfo) {
        randomReconnectPeriod();
        PendingIntent pendingIntent = createPendingIntent(hostInfo);
        startAlarm(pendingIntent, (long) reconnectTaskPeriod);
        LogUtils.d("start reconnect alarm");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void startAlarm(PendingIntent pendingIntent, long period) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + period, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + period, pendingIntent);
        }
        LogUtils.i("start alarm period is [" + TimeFormatUtil.format(period) + "]");
    }

    private void cancelAlarm(PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmed = false;
        LogUtils.w("cancel alarm.");
    }

    private PendingIntent createPendingIntent(HostInfo hostInfo) {
        Intent intent = new Intent(context, ConnectionService.class);
        intent.setPackage(context.getPackageName());
        intent.putExtra(TaskType.TAG, TaskType.ALARM_CONNECT);
        if (hostInfo != null) {
            intent.putExtra("hostname", hostInfo.getHostname());
            intent.putExtra("port", hostInfo.getPort());
        }
        return PendingIntent.getService(context, 1516, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 根据固定算法生成连接时间间隔
     */
    private void randomConnectPeriod() {
        int start = (int) (Math.pow(2, connectPeriodTag.get()) * 1000);
        int end = (int) (Math.pow(2, connectPeriodTag.get() + 1) * 1000);
        connectTaskPeriod = RandomUtil.getRandom(start, end);
        if (connectTaskPeriod >= 20 * 60 * 1000) {
            connectTaskPeriod = 20 * 60 * 1000;
        } else {
            connectPeriodTag.incrementAndGet();
            LogUtils.i("to increment the connectPeriodTag:" + connectPeriodTag.get() + ",connectTaskPeriod:" + connectTaskPeriod);
        }
        LogUtils.i("connect task period is [" + TimeFormatUtil.format((long) connectTaskPeriod) + "]");
    }

    private void randomReconnectPeriod() {
        int start = (int) (Math.pow(2, reconnectPeriodTag.get()) * 1000);
        int end = (int) (Math.pow(2, reconnectPeriodTag.get() + 1) * 1000);
        reconnectTaskPeriod = RandomUtil.getRandom(start, end);
        if (reconnectTaskPeriod >= 20 * 60 * 1000) {
            reconnectTaskPeriod = 20 * 60 * 1000;
        } else {
            reconnectPeriodTag.incrementAndGet();
            LogUtils.i("to increment the reconnectPeriodTag:" + reconnectPeriodTag.get() + ",reconnectTaskPeriod:" + reconnectTaskPeriod);
        }
        LogUtils.i("reconnect task period is [" + TimeFormatUtil.format((long) reconnectTaskPeriod) + "]");
    }

    /**
     * 重新连接到IM服务器
     */
    private void reconnect() {
        LogUtils.d("reconnect...");
        long lastConnectPeriod = System.currentTimeMillis() - lastConnectTime;
        LogUtils.i("lastConnectPeriod:" + lastConnectPeriod);

        // 如果上一个主机连接断开则切换下一个主机地址进行重连
        HostInfo hostInfo = hostInfoManager.switchNextHost(canUseHostInfo);
        // 避免连接连接成功后又断开,然后又进行重连导致短时间内重连频率太高而给服务器带来的并发压力
        if (lastConnectPeriod < 1000) {
            // 用闹钟来唤醒重连
            startReconnectAlarm(hostInfo);
        } else {
            reconnectPeriodTag.set(0);
            connect(hostInfo.getHostname(), hostInfo.getPort(), true, null);
        }
    }


    /**
     * 是否已经连接
     * @return true表示已经连接
     */
    @Override
    public boolean isConnected() {
        if (socket == null) {
            return false;
        }
        if (socket.isClosed()) {
            return false;
        }
        if (!socket.isConnected()) {
            return false;
        }
        return isConnected;
    }

    private void closeSocket() {
        try {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            LogUtils.e(e);
        }
    }

    @Override
    public String getHostname() {
        return canUseHostInfo.getHostname();
    }

    @Override
    public int getPort() {
        return canUseHostInfo.getPort();
    }
}
