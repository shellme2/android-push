package com.eebbk.bfc.im.push.bean;

import android.text.TextUtils;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.IpAddressUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接主机的信息
 */
public class HostInfo {

    /**
     * 主机名，可以是IP，也可以是域名
     */
    private String hostname;

    /**
     * 主机端口
     */
    private int port;

    /**
     * 这个主机连接成功的次数
     */
    private AtomicInteger connectedSuccessCount;

    /**
     * 这个主机连接失败的次数
     */
    private AtomicInteger connectedFailCount;

    /**
     * 这个主机连接断开的次数
     */
    private AtomicInteger disconnectedCount;

    /**
     * 这个主机的连接次数
     */
    private AtomicInteger connectCount;

    private long start;

    private boolean isIPv4Format;

    /**
     * 最后更新主机信息的时间点
     */
    private long update;

    public HostInfo(String hostname, int port) {
        start = System.currentTimeMillis();
        this.hostname = hostname;
        this.port = port;
        isIPv4Format = IpAddressUtil.isIPv4Address(hostname);
        connectedSuccessCount = new AtomicInteger(0);
        connectedFailCount = new AtomicInteger(0);
        disconnectedCount = new AtomicInteger(0);
        connectCount = new AtomicInteger(0);
    }

    /**
     * 获取主机名
     */
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * 获取端口
     */
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void increaseSuccessCount() {
        connectedSuccessCount.incrementAndGet();
        update = System.currentTimeMillis();
    }

    public int getSuccessCount() {
        return connectedSuccessCount.get();
    }

    public void increaseFailCount() {
        connectedFailCount.incrementAndGet();
        update = System.currentTimeMillis();
    }

    public int getFailCount() {
        return connectedFailCount.get();
    }

    public void increaseDisconnectedCount() {
        disconnectedCount.incrementAndGet();
        update = System.currentTimeMillis();
    }

    public int getDisconnectedCount() {
        return disconnectedCount.get();
    }

    public void increaseConnectCount() {
        connectCount.incrementAndGet();
        update = System.currentTimeMillis();
    }

    public int getConnectCount() {
        return connectCount.get();
    }

    private int computeCount(List<Long> target, long prePeriod) {
        long now = System.currentTimeMillis();
        int count = 0;
        for (Long l : target) {
            if (l == null) {
                continue;
            }
            if (now - prePeriod < l.longValue()) {
                count++;
            }
        }
        LogUtils.i("count:" + count);
        return count;
    }

    /**
     * 只要主机名和端口一直就任务这两个主机是一致的
     */
    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o == null) {
            return false;
        }
        if (o instanceof HostInfo) {
            HostInfo hostInfo = (HostInfo) o;
            if (!TextUtils.isEmpty(hostname) && port != 0) {
                equal =  hostname.equals(hostInfo.getHostname()) && port == hostInfo.getPort();
            }
        }
        return equal;
    }

    public void clear() {
        /*failTimeList.clear();
        connectTimeList.clear();
        disconnectedTimeList.clear();*/
    }

    public boolean isIPv4Format() {
        return isIPv4Format;
    }

    @Override
    public String toString() {
        return "HostInfo{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", connectedSuccessCount=" + connectedSuccessCount +
                ", connectedFailCount=" + connectedFailCount +
                ", disconnectedCount=" + disconnectedCount +
                ", connectCount=" + connectCount +
                '}';
    }
}
