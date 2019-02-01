package com.eebbk.bfc.im.push.bean;

import android.content.Context;
import android.os.Build;

import com.eebbk.bfc.im.push.SDKVersion;
import com.eebbk.bfc.im.push.service.host.httpdns.HttpDnsClient;
import com.eebbk.bfc.im.push.service.host.HostInfoManager;
import com.eebbk.bfc.im.push.util.DateFormatUtil;
import com.eebbk.bfc.im.push.util.JsonUtil;
import com.eebbk.bfc.im.push.util.NetUtil;
import com.eebbk.bfc.im.push.util.TimeFormatUtil;

public class PushCollectInfo {

    /**
     * service是否被销毁
     */
    private boolean serviceDestroyed;

    /**
     * httpdns成功调用次数
     */
    private int httpDnsCallSuccessCount;

    /**
     * 服务创建时间
     */
    private long serviceCreateTime;

    private String serviceCreateTimeFormat;

    /**
     * 服务创建时长
     */
    private long serviceCreateDuration;

    private String serviceCreateDurationFormat;

    /**
     * 连接时间
     */
    private long connectedTime;

    private String connectedTimeFormat;

    /**
     * 连接断开时间
     */
    private long disconnectedTime;

    private String disconnectedTimeFormat;

    /**
     * 记录时间
     */
    private long recordTime;

    private String recortTimeFormat;

    /**
     * 连接持续时长
     */
    private long connectedDuration;

    private String connectedDurationFormat;

    /**
     * 推送连接状态
     */
    private boolean pushConnected;

    /**
     * 设备登录状态
     */
    private boolean pushLogin;

    /**
     * 网络连接状态
     */
    private boolean netConnected;

    private String networkTag;

    private String networkType;

    private String simType;

    private int sdkVersionCode;

    private String sdkVersionName;

    /**
     * 推送的registerId
     */
    private long registerId;

    private String hostname;

    private int port;

    /**
     * 是否在连接过程中
     */
    private boolean connecting;

    private String heartbeatInfo;

    /**
     * 当前预埋ip
     */
    private String hostInfoStr;

    private String deviceInfo;

    public PushCollectInfo() {
        this.hostInfoStr = HostInfoManager.getInstance().toString();
        this.sdkVersionCode = SDKVersion.getSDKInt();
        this.sdkVersionName = SDKVersion.getVersionName();
        this.deviceInfo = Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE;
        this.recordTime = System.currentTimeMillis();
        this.recortTimeFormat = DateFormatUtil.format(DateFormatUtil.FORMAT_1, recordTime);
    }

    public PushCollectInfo(Context context) {
        this();
        this.netConnected = NetUtil.isConnectToNet(context);
        this.networkTag = NetUtil.getNetworkTag(context);
        this.networkType = NetUtil.getNetworkType(context);
        this.simType = NetUtil.getSIMType(context);
    }

    public PushCollectInfo(Context context, long serviceCreateTime, boolean serviceDestroyed, long connectedTime, long disconnectedTime, long connectedDuration) {
        this(context);
        this.serviceCreateTime = serviceCreateTime;
        this.serviceCreateTimeFormat = DateFormatUtil.format(DateFormatUtil.FORMAT_1, serviceCreateTime);
        this.serviceDestroyed = serviceDestroyed;
        if (!serviceDestroyed) {
            this.serviceCreateDuration = System.currentTimeMillis() - serviceCreateTime;
            this.serviceCreateDurationFormat = TimeFormatUtil.format(serviceCreateDuration);
        } else {
            this.serviceCreateDuration = 0;
            this.serviceCreateDurationFormat = null;
        }
        this.httpDnsCallSuccessCount = HttpDnsClient.getHttpDnsCallSuccessCount();
        this.connectedTime = connectedTime;
        this.connectedTimeFormat = DateFormatUtil.format(DateFormatUtil.FORMAT_1, connectedTime);
        this.disconnectedTime = disconnectedTime;
        this.disconnectedTimeFormat = DateFormatUtil.format(DateFormatUtil.FORMAT_1, disconnectedTime);
        this.connectedDuration = connectedDuration;
        this.connectedDurationFormat = TimeFormatUtil.format(connectedDuration);
    }

    public boolean isServiceDestroyed() {
        return serviceDestroyed;
    }

    public void setServiceDestroyed(boolean serviceDestroyed) {
        this.serviceDestroyed = serviceDestroyed;
    }

    public int getHttpDnsCallSuccessCount() {
        return httpDnsCallSuccessCount;
    }

    public void setHttpDnsCallSuccessCount(int httpDnsCallSuccessCount) {
        this.httpDnsCallSuccessCount = httpDnsCallSuccessCount;
    }

    public long getServiceCreateTime() {
        return serviceCreateTime;
    }

    public void setServiceCreateTime(long serviceCreateTime) {
        this.serviceCreateTime = serviceCreateTime;
    }

    public String getServiceCreateTimeFormat() {
        return serviceCreateTimeFormat;
    }

    public void setServiceCreateTimeFormat(String serviceCreateTimeFormat) {
        this.serviceCreateTimeFormat = serviceCreateTimeFormat;
    }

    public long getServiceCreateDuration() {
        return serviceCreateDuration;
    }

    public void setServiceCreateDuration(long serviceCreateDuration) {
        this.serviceCreateDuration = serviceCreateDuration;
    }

    public String getServiceCreateDurationFormat() {
        return serviceCreateDurationFormat;
    }

    public void setServiceCreateDurationFormat(String serviceCreateDurationFormat) {
        this.serviceCreateDurationFormat = serviceCreateDurationFormat;
    }

    public long getConnectedTime() {
        return connectedTime;
    }

    public void setConnectedTime(long connectedTime) {
        this.connectedTime = connectedTime;
    }

    public String getConnectedTimeFormat() {
        return connectedTimeFormat;
    }

    public void setConnectedTimeFormat(String connectedTimeFormat) {
        this.connectedTimeFormat = connectedTimeFormat;
    }

    public long getDisconnectedTime() {
        return disconnectedTime;
    }

    public void setDisconnectedTime(long disconnectedTime) {
        this.disconnectedTime = disconnectedTime;
    }

    public String getDisconnectedTimeFormat() {
        return disconnectedTimeFormat;
    }

    public void setDisconnectedTimeFormat(String disconnectedTimeFormat) {
        this.disconnectedTimeFormat = disconnectedTimeFormat;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }

    public String getRecortTimeFormat() {
        return recortTimeFormat;
    }

    public void setRecortTimeFormat(String recortTimeFormat) {
        this.recortTimeFormat = recortTimeFormat;
    }

    public long getConnectedDuration() {
        return connectedDuration;
    }

    public void setConnectedDuration(long duration) {
        this.connectedDuration = duration;
    }

    public String getConnectedDurationFormat() {
        return connectedDurationFormat;
    }

    public void setConnectedDurationFormat(String connectedDurationFormat) {
        this.connectedDurationFormat = connectedDurationFormat;
    }

    public boolean isPushConnected() {
        return pushConnected;
    }

    public void setPushConnected(boolean pushConnected) {
        this.pushConnected = pushConnected;
    }

    public boolean isPushLogin() {
        return pushLogin;
    }

    public void setPushLogin(boolean pushLogin) {
        this.pushLogin = pushLogin;
    }

    public boolean isNetConnected() {
        return netConnected;
    }

    public void setNetConnected(boolean netConnected) {
        this.netConnected = netConnected;
    }

    public String getNetworkTag() {
        return networkTag;
    }

    public void setNetworkTag(String networkTag) {
        this.networkTag = networkTag;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getSimType() {
        return simType;
    }

    public void setSimType(String simType) {
        this.simType = simType;
    }

    public int getSdkVersionCode() {
        return sdkVersionCode;
    }

    public void setSdkVersionCode(int sdkVersionCode) {
        this.sdkVersionCode = sdkVersionCode;
    }

    public String getSdkVersionName() {
        return sdkVersionName;
    }

    public void setSdkVersionName(String sdkVersionName) {
        this.sdkVersionName = sdkVersionName;
    }

    public long getRegisterId() {
        return registerId;
    }

    public void setRegisterId(long registerId) {
        this.registerId = registerId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isConnecting() {
        return connecting;
    }

    public void setConnecting(boolean connecting) {
        this.connecting = connecting;
    }

    public String getHostInfoStr() {
        return hostInfoStr;
    }

    public void setHostInfoStr(String hostInfoStr) {
        this.hostInfoStr = hostInfoStr;
    }

    public String getHeartbeatInfo() {
        return heartbeatInfo;
    }

    public void setHeartbeatInfo(String heartbeatInfo) {
        this.heartbeatInfo = heartbeatInfo;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
