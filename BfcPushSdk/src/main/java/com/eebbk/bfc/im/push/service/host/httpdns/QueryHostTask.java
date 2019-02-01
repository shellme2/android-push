package com.eebbk.bfc.im.push.service.host.httpdns;

import android.content.Context;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.code.DesEncryptUtil;
import com.eebbk.bfc.im.push.util.NetUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryHostTask implements Callable<String> {

    private Context context;

    private String serverIp;

    private String accountId;

    private ConcurrentMap<String, HostObject> hostCache;

    private String hostName;

    private String encryptHostName;

    private AtomicInteger retryCount = new AtomicInteger(0);

    public QueryHostTask(Context context, String hostToQuery, String serverIp, String accountId, ConcurrentMap<String, HostObject> hostCache) {
        this.context = context;
        this.hostName = hostToQuery;
        this.serverIp = serverIp;
        this.accountId = accountId;
        this.hostCache = hostCache;
        this.encryptHostName = DesEncryptUtil.encrypt(hostName);
        LogUtils.i("httpdns 加密后结果:" + encryptHostName);
    }

    private HttpDNSResult convert(String decryptStr) {
        LogUtils.i("httpdns 解密结果:" + decryptStr);
        if (TextUtils.isEmpty(decryptStr)) {
            return null;
        }
        int lastCommaIndex = decryptStr.lastIndexOf(",");
        String ttlStr = decryptStr.substring(lastCommaIndex + 1, decryptStr.length());
        LogUtils.i("httpdns ttl:" + ttlStr);
        String[] ipArray = decryptStr.substring(0, lastCommaIndex).split(";");
        LogUtils.i("httpdns ips:" + Arrays.toString(ipArray));
        HttpDNSResult httpDNSResult = new HttpDNSResult();
        httpDNSResult.setHost(hostName);
        httpDNSResult.setIps(Arrays.asList(ipArray));
        httpDNSResult.setTtl(Long.parseLong(ttlStr));
        return httpDNSResult;
    }

    @Override
    public String call() {
        String resolveUrl = "http://" + serverIp + "d?dn=" + encryptHostName + "&id=" + accountId + "&ttl=" + 1;
        LogUtils.d("[QueryHostTask.call] - buildUrl: " + resolveUrl);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(resolveUrl).openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(10 * 1000);
            if (conn.getResponseCode() != 200) {
                LogUtils.w("[QueryHostTask.call] - response code: " + conn.getResponseCode());
            } else {
                InputStream in = conn.getInputStream();
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = streamReader.readLine()) != null) {
                    sb.append(line);
                }
                LogUtils.i("httpdns 解析结果:" + sb.toString());
                String decryptStr = new String(DesEncryptUtil.decrypt(sb.toString()));
                HttpDNSResult httpDNSResult = convert(decryptStr);
                if (httpDNSResult != null) {
                    LogUtils.i("HttpDNSResult:" + httpDNSResult);
                    String host = httpDNSResult.getHost();
                    long ttl = httpDNSResult.getTtl();
                    List<String> ips = httpDNSResult.getIps();
                    if (host != null) {
                        if (ttl == 0) {
                            // 如果有结果返回，但是ip列表为空，ttl也为空，那默认没有ip就是解析结果，并设置ttl为一个较长的时间
                            // 避免一直请求同一个ip冲击sever
                            ttl = 30;
                        }
                        HostObject hostObject = new HostObject();
                        String ip = (ips == null || ips.size() == 0) ? null : ips.get(0);
                        LogUtils.d("[QueryHostTask.call] - resolve host:" + host + " ip:" + ip + " ttl:" + ttl);
                        hostObject.setHostName(host);
                        hostObject.setTtl(ttl);
                        hostObject.setIp(ip);
                        hostObject.setQueryTime(System.currentTimeMillis() / 1000);
                        hostObject.setNetTag(NetUtil.getNetworkTag(context));
                        synchronized (hostCache) {
                            hostCache.put(host, hostObject);
                        }
                        LogUtils.i("httpdns ips:" + hostCache);
                        return ip;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        if (retryCount.incrementAndGet() <= 3) {
            long now = System.currentTimeMillis();
            while (System.currentTimeMillis() - now < 3000);
            return call();
        }
        return null;
    }
}
