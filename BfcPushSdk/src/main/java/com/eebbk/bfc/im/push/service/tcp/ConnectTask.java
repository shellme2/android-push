package com.eebbk.bfc.im.push.service.tcp;

import android.content.Context;
import android.os.RemoteException;

import com.eebbk.bfc.im.push.IConnectCallback;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.service.host.HostInfoManager;
import com.eebbk.bfc.im.push.service.task.Task;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.bean.HostInfo;
import com.eebbk.bfc.im.push.exception.ConnectException;
import com.eebbk.bfc.im.push.service.host.httpdns.HttpDnsClient;
import com.eebbk.bfc.im.push.util.NetUtil;
import com.eebbk.bfc.im.push.util.WakeLockUtil;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectTask extends Task {

    private static String TAG = "ConnectTask";

    private boolean isAlarm; // 是否是定时唤醒的任务

    private HostInfo hostInfo;

    private TCPConnection tcpConnection;

    private IConnectCallback connectCallback;

    private static final AtomicInteger retryTime = new AtomicInteger(0);

    public ConnectTask(HostInfo hostInfo, boolean isAlarm, TCPConnection tcpConnection) {
        this.hostInfo = hostInfo;
        this.isAlarm = isAlarm;
        this.tcpConnection = tcpConnection;
    }

    public IConnectCallback getConnectCallback() {
        return connectCallback;
    }

    public void setConnectCallback(IConnectCallback connectCallback) {
        this.connectCallback = connectCallback;
    }

    @Override
    public void run() {
        System.out.println("LOG_TAG : run connect task !!! ");
        if (tcpConnection.isConnected()) {
            callConnectCallback(true, tcpConnection.getHostname(), tcpConnection.getPort(), null);
            LogUtils.w(TAG,"connection had connected.");
            return;
        }
        if (isAlarm) {
            tcpConnection.setAlarmed(true);
        }
        LogUtils.d(TAG,"run connect task...");

        Context context = tcpConnection.getContext();
        WakeLockUtil.acquire(context, tcpConnection.getTimeout());
        IOException ioException = null;
        try {
            // 由于这里持锁，每次等连接超时要20秒。在认证网络环境下，会一直失败重试，持锁时间长，导致功耗问题。
            // 所以如果前两次连接失败了，就通过ping判断网络是否可用，减少持锁时间
            // 为了减少由于ping导致加重对服务器压力，只有两次连续失败才ping
            if(retryTime.incrementAndGet() > 2){
                String pingHost = hostInfo.getHostname();
                LogUtils.e(TAG,"start ping " + pingHost);
                if(!pingIpAddress(pingHost)){
                    throw new IOException("ping " + pingHost + " fail");
                }else {
                    LogUtils.e(TAG,"ping " + pingHost + " success");
                }
            }
            tcpConnection.connectImpl(hostInfo);
            resetRetryTime();
            LogUtils.i(TAG,"connect success,network type:" + NetUtil.getNetworkType(context));
        } catch (IOException e) {
            ioException = e;
            LogUtils.e(TAG,e);
            dealIOException(context, e);
        } finally {
            tcpConnection.setLastConnectTime(System.currentTimeMillis());
            WakeLockUtil.release();
            callConnectCallback(tcpConnection.isConnected(), hostInfo.getHostname(), hostInfo.getPort(), ioException);
        }
    }

    public static void resetRetryTime(){
        retryTime.set(0);
    }

    private boolean pingIpAddress(String ipAddress) {
        try {
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 5 " + ipAddress);
            int status = process.waitFor();
            return status == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void callConnectCallback(boolean connected, String hostname, int port, IOException ioException) {
        if (connectCallback == null) {
            return;
        }
        try {
            if (connected) {
                connectCallback.onConnected(hostname, port);
            } else {
                connectCallback.onFailed(hostname, port, ioException == null ? "connect error!" : ioException.toString());
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG,e);
        } catch (RuntimeException e) {
            LogUtils.e(TAG,e);
        }
    }

    private void dealIOException(Context context, IOException e) {
        tcpConnection.callOnConnectFailed(new ConnectException(e.toString()), hostInfo.getHostname(), hostInfo.getPort());
        if (isAlarm) {
            if (!NetUtil.isConnectToNet(context)) { // 断网情况下不进行重连操作,省电
                LogUtils.e(TAG,"network is unreachable,stop connect alarming task!");
                return;
            }
            LogUtils.i(TAG,"network is reachable,network type:" + NetUtil.getNetworkType(context));
            caseIOException(context, e);
            tcpConnection.startRetryAlarm(hostInfo);
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_TCP,"startRetryAlarm");
        } else {
            LogUtils.w(TAG,"the connect task is not a alarm task.  isAlarm="+isAlarm);
        }
    }

    private void caseIOException(Context context, IOException e) {
        HostInfoManager hostInfoManager = HostInfoManager.getInstance();
        if (isDNSError(e)) {
            handleDNSError(hostInfoManager, context, e);
        } else {
            handleOtherError(hostInfoManager, e);
        }
    }

    /**
     * 对于SocketTimeoutException异常有可能是一些敏感端口被禁用导致一直连接超时
     */
    private boolean isDNSError(IOException e) {
        if (e instanceof SocketTimeoutException || e instanceof UnknownHostException) {
            return true;
        }
        return false;
    }

    private void handleDNSError(HostInfoManager hostInfoManager, Context context, IOException e) {
        LogUtils.e(TAG,"DNS域名解析失败问题信息,异常类型:" + e.getClass().getSimpleName() + ",hostname:"
                + hostInfo.getHostname() + ",port:" + hostInfo.getPort());

        if (hostInfoManager.isLast(hostInfo)) {
            // 如果是域名类型并且未曾用该域名进行过http dns解析则进行http dns解析
            String httpDNSIP = HttpDnsClient.httpDns(context, hostInfo.getHostname());
            HostInfo httpDNSHostInfo = new HostInfo(httpDNSIP, hostInfo.getPort());
            if (hostInfoManager.add(httpDNSHostInfo)) {
                tcpConnection.resetConnectPeriodTag();
                hostInfo = httpDNSHostInfo;
            } else {
                hostInfo = hostInfoManager.switchNextHost(hostInfo);
            }
            LogUtils.i(TAG,"HTTP_DNS hostInfoManager:" + hostInfoManager);
        } else {
            LogUtils.d(TAG,"the host is not the last,continue switch...");
            hostInfo = hostInfoManager.switchNextHost(hostInfo);
            if (!hostInfo.isIPv4Format()) {
                LogUtils.d(TAG,"the host is not ipv4 format,continue switch...");
                handleDNSError(hostInfoManager, context, e);
            }
        }
        LogUtils.e(TAG,"DNS域名解析失败问题信息,切换IP服务器后hostname:" + hostInfo.getHostname() + ",port:" + hostInfo.getPort());
    }

    private void handleOtherError(HostInfoManager hostInfoManager, IOException e) {
        LogUtils.e(TAG,"其他连接失败,异常类型:" + e.getClass().getSimpleName() + ",hostname:" + hostInfo.getHostname()
                + ",port:" + hostInfo.getPort());

        hostInfo = hostInfoManager.switchNextHost(hostInfo);

        LogUtils.e(TAG,"其他连接失败,切换IP服务器后hostname:" + hostInfo.getHostname() + ",port:" + hostInfo.getPort());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{hostInfo:" + hostInfo + ",isAlarm:" + isAlarm + "}";
    }

    @Override
    public int compareTo(Object another) {
        ConnectTask connectTask = (ConnectTask) another;
        HostInfo anotherHostInfo = connectTask.hostInfo;
        if (hostInfo.isIPv4Format() && !anotherHostInfo.isIPv4Format()) {
            return -1;
        } else {
            return 1;
        }
    }
}
