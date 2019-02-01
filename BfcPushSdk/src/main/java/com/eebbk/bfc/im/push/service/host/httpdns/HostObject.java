package com.eebbk.bfc.im.push.service.host.httpdns;

public class HostObject {

    @Override
    public String toString() {
        return "HostObject [hostName=" + hostName + ", ip=" + ip + ", ttl=" + ttl + ", queryTime="
                + queryTime + "]";
    }

    private String hostName;
    private String ip;
    private long ttl;
    private long queryTime;
    private String netTag;

    public boolean isExpired() {
//        return queryTime + ttl < System.currentTimeMillis() / 1000;
        return false; // 解析的ip永远有效
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public long getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(long queryTime) {
        this.queryTime = queryTime;
    }

    public String getNetTag() {
        return netTag;
    }

    public void setNetTag(String netTag) {
        this.netTag = netTag;
    }
}
