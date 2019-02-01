package com.eebbk.bfc.im.push.service.tcp;

import android.content.Context;
import android.os.RemoteException;

import com.eebbk.bfc.im.push.IConnectCallback;
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

public class ConnectTask extends Task {

    private boolean isAlarm; // 是否是定时唤醒的任务

    private HostInfo hostInfo;

    private TCPConnection tcpConnection;

    private IConnectCallback connectCallback;

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
        if (tcpConnection.isConnected()) {
            callConnectCallback(true, tcpConnection.getHostname(), tcpConnection.getPort(), null);
            LogUtils.w("connection had connected.");
            return;
        }
        if (isAlarm) {
            tcpConnection.setAlarmed(true);
        }
        LogUtils.d("run connect task...");

        Context context = tcpConnection.getContext();
        WakeLockUtil.acquire(context, tcpConnection.getTimeout());
        IOException ioException = null;
        try {
            tcpConnection.connectImpl(hostInfo);
            LogUtils.i("connect success,network type:" + NetUtil.getNetworkType(context));
        } catch (IOException e) {
            ioException = e;
            LogUtils.e(e);
            dealIOException(context, e);
        } finally {
            tcpConnection.setLastConnectTime(System.currentTimeMillis());
            WakeLockUtil.release();
            callConnectCallback(tcpConnection.isConnected(), hostInfo.getHostname(), hostInfo.getPort(), ioException);
        }
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
            LogUtils.e(e);
        } catch (RuntimeException e) {
            LogUtils.e(e);
        }
    }

    private void dealIOException(Context context, IOException e) {
        tcpConnection.callOnConnectFailed(new ConnectException(e.toString()), hostInfo.getHostname(), hostInfo.getPort());
        if (isAlarm) {
            if (!NetUtil.isConnectToNet(context)) { // 断网情况下不进行重连操作,省电
                LogUtils.e("network is unreachable,stop connect alarming task!");
                return;
            }
            LogUtils.i("network is reachable,network type:" + NetUtil.getNetworkType(context));
            caseIOException(context, e);
            tcpConnection.startRetryAlarm(hostInfo);
            LogUtils.w("the connect task is a alarm task.");
        } else {
            LogUtils.w("the connect task is not a alarm task.");
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
        LogUtils.e("DNS域名解析失败问题信息,异常类型:" + e.getClass().getSimpleName() + ",hostname:"
                + hostInfo.getHostname() + ",port:" + hostInfo.getPort());

        if (hostInfoManager.isLast(hostInfo)) {
            // 如果是域名类型并且未曾用该域名进行过httpdns解析则进行httpdns解析
            String httpDNSIP = HttpDnsClient.httpDns(context, hostInfo.getHostname());
            HostInfo httpDNSHostInfo = new HostInfo(httpDNSIP, hostInfo.getPort());
            if (hostInfoManager.add(httpDNSHostInfo)) {
                tcpConnection.resetConnectPeriodTag();
                hostInfo = httpDNSHostInfo;
            } else {
                hostInfo = hostInfoManager.switchNextHost(hostInfo);
            }
            LogUtils.i("HTTPDNS hostInfoManager:" + hostInfoManager);
        } else {
            LogUtils.d("the host is not the last,continue switch...");
            hostInfo = hostInfoManager.switchNextHost(hostInfo);
            if (!hostInfo.isIPv4Format()) {
                LogUtils.d("the host is not ipv4 format,continue switch...");
                handleDNSError(hostInfoManager, context, e);
            }
        }
        LogUtils.e("DNS域名解析失败问题信息,切换IP服务器后hostname:" + hostInfo.getHostname() + ",port:" + hostInfo.getPort());
    }

    private void handleOtherError(HostInfoManager hostInfoManager, IOException e) {
        LogUtils.e("其他连接失败,异常类型:" + e.getClass().getSimpleName() + ",hostname:" + hostInfo.getHostname()
                + ",port:" + hostInfo.getPort());

        hostInfo = hostInfoManager.switchNextHost(hostInfo);

        LogUtils.e("其他连接失败,切换IP服务器后hostname:" + hostInfo.getHostname() + ",port:" + hostInfo.getPort());
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
