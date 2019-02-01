package com.eebbk.bfc.im.push.util;

import android.text.TextUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtil {

    //构造函数私有，防止恶意新建
    private IPUtil(){}

    public interface OnParseIPListener {
        void onParse(String ip);
    }

    public static String parseIP(String domain) {
        String ip = null;
        LogUtils.i("before parse:" + domain);
        if (TextUtils.isEmpty(domain)) {
            return null;
        }
        try {
            InetAddress inetAddress = InetAddress.getByName(domain);
            ip = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            LogUtils.e(e);
        }
        LogUtils.i("after parse:" + ip);
        return ip;
    }

}
