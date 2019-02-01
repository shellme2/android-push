package com.eebbk.bfc.im.push.service.host.httpdns;

import android.content.Context;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.IpAddressUtil;

import java.io.IOException;
import java.net.URL;

public class HttpDnsClient {

	private static HttpDNS httpDnsService = HttpDNS.getInstance();
	
	public static final String tag = "http dns";

	public static String httpDns(Context context, String hostname) {
		if (IpAddressUtil.isIPv4Address(hostname)) {
			LogUtils.i("the hostname [" + hostname + "] is ipv4 format,do not need to do http dns");
			return hostname;
		}
		try {
			String ip = getIpByHost(context, "http://" + hostname);
            LogUtils.i("http dns ip:" + ip);
            LogUtils.i("http dns", ip + " is ip type:" + IpAddressUtil.isIPv4Address(ip));
            LogUtils.i("http dns", hostname + " is ip type:" + IpAddressUtil.isIPv4Address(hostname));
            return ip;
		} catch (Exception e) {
			LogUtils.e(e);
		}
        return null;
	}

    private static String getIpByHost(Context context, String urlString) throws IOException {
		URL url = new URL(urlString);
		String dstIp = httpDnsService.getIpByHost(context, url.getHost());
		if (!TextUtils.isEmpty(dstIp)) {
			LogUtils.d(tag, "Get IP from HttpDNS, " + url.getHost() + ": " + dstIp);
			return dstIp;
		} else {
			LogUtils.d(tag, "Degrade to local DNS.");
            int index = urlString.indexOf("//");
			return urlString.substring(index + 2, urlString.length());
		}
	}

	public static String getCacheIp(Context context, String hostName) {
		return httpDnsService.getCacheIp(context, hostName);
	}

	public static void remove(String host) {
		if (TextUtils.isEmpty(host)) {
			return;
		}
		httpDnsService.removeCacheByHost(host);
	}

	public static void clear() {
		httpDnsService.clearCache();
	}

	public static int getHttpDnsCallSuccessCount() {
		return httpDnsService.getHttpDnsCallSuccessCount();
	}
}
