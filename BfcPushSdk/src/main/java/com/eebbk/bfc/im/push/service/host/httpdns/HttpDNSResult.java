package com.eebbk.bfc.im.push.service.host.httpdns;

import java.util.List;

public class HttpDNSResult {

    private String host;

    private List<String> ips;

    private long ttl;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return "HttpDNSResult{" +
                "host='" + host + '\'' +
                ", ips=" + ips +
                ", ttl=" + ttl +
                '}';
    }
}
