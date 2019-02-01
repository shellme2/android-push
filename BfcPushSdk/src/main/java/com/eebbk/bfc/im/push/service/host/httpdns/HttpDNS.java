package com.eebbk.bfc.im.push.service.host.httpdns;

import android.content.Context;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NetUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpDNS {

    private static final String SERVER_IP = "119.29.29.29/";
    private static final String ACCOUNT_ID = "196";
    private static final int MAX_THREAD_NUM = 5;

    private ConcurrentMap<String, HostObject> hostCache = new ConcurrentHashMap<>();
    private AtomicInteger httpDnsCallSuccessCount = new AtomicInteger(0);
    private static HttpDNS instance = new HttpDNS();
    private ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_NUM);

    private HttpDNS() {
    }

    public static HttpDNS getInstance() {
        return instance;
    }

    protected String getIpByHost(Context context, String hostName) {
        String tag = NetUtil.getNetworkTag(context);
        HostObject host = hostCache.get(hostName);
        if (host == null) {
            LogUtils.d("no cache host");
            return getIp(context, hostName);
        } else {
            if (!TextUtils.equals(host.getNetTag(), tag) || !TextUtils.equals(host.getHostName(), hostName) || host.isExpired()) {
                if (host.isExpired()) {
                    synchronized (hostCache) {
                        hostCache.remove(host.getHostName());
                    }
                }
                return getIp(context, hostName);
            }
        }
        LogUtils.d("[getIpByHost] - fetch result from cache, host: " + hostName);
        LogUtils.i("hostCache:" + hostCache);
        return host.getIp();
    }

    protected String getCacheIp(Context context, String hostName) {
        String tag = NetUtil.getNetworkTag(context);
        HostObject host = hostCache.get(hostName);
        if (host != null) {
            if (TextUtils.equals(tag, host.getNetTag()) && TextUtils.equals(host.getHostName(), hostName) && !host.isExpired()) {
                return host.getIp();
            }
        }
        return null;
    }

    private String getIp(Context context, String hostName) {
        LogUtils.d("[getIpByHost] - fetch result from network, host: " + hostName);
        Future<String> future = pool.submit(new QueryHostTask(context, hostName, SERVER_IP, ACCOUNT_ID, hostCache));
        String ip = null;
        try {
            ip = future.get();
        } catch (Exception e) {
            LogUtils.e(e);
        } finally {
            if (!TextUtils.isEmpty(ip)) {
                int count = httpDnsCallSuccessCount.incrementAndGet();
                LogUtils.i("increase http dns call success count:" + count);
            }
        }
        return ip;
    }

    protected void clearCache() {
        hostCache.clear();
        httpDnsCallSuccessCount.set(0);
    }

    protected void removeCacheByHost(String host) {
        hostCache.remove(host);
    }

    protected int getHttpDnsCallSuccessCount() {
        return httpDnsCallSuccessCount.get();
    }
}
