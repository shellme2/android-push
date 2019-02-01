package com.eebbk.bfc.im.push.debug;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/20 17:00
 * Email:  zengjingfang@foxmail.com
 */
public class DebugBasicInfo {
    private String machineId;

    private String macAddress;

    private String imei;

    private String deviceId;

    private String hostAppName;

    private String hostSdkInfo;

    private String hostIp;

    private String netWorkState;

    public DebugBasicInfo(String machineId, String macAddress, String imei, String deviceId, String hostAppName, String hostSdkInfo, String hostIp,
                          String netWorkState) {
        this.machineId = machineId;
        this.macAddress = macAddress;
        this.imei = imei;
        this.deviceId = deviceId;
        this.hostAppName = hostAppName;
        this.hostSdkInfo = hostSdkInfo;
        this.hostIp = hostIp;
        this.netWorkState = netWorkState;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getHostAppName() {
        return hostAppName;
    }

    public void setHostAppName(String hostAppName) {
        this.hostAppName = hostAppName;
    }

    public String getHostSdkInfo() {
        return hostSdkInfo;
    }

    public void setHostSdkInfo(String hostSdkInfo) {
        this.hostSdkInfo = hostSdkInfo;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getNetWorkState() {
        return netWorkState;
    }

    public void setNetWorkState(String netWorkState) {
        this.netWorkState = netWorkState;
    }

    @Override
    public String toString() {
        return "machineId: "+machineId+"\n hostAppName: "+hostAppName+"\n hostSdkInfo: "+hostSdkInfo+"\n hostIp: "+"\n netWorkState: "+netWorkState;
    }
}
