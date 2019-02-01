package com.eebbk.bfc.im.push.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.eebbk.bfc.common.devices.NetUtils;

/**
 * android网络检测工具类
 */
public class NetUtil {

    //构造函数私有，防止恶意新建
    private NetUtil(){}

    /**
     * 判断手机是否联网
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isConnectToNet(Context context) {
        return NetUtils.isConnected(context);
    }

    public static String getNetworkTag(Context context) {
        String tag = null;
        ConnectivityManager localConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo localNetworkInfo = localConnectivityManager.getActiveNetworkInfo();
        if (localNetworkInfo != null && localNetworkInfo.isAvailable()) {
            int type = localNetworkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiMgr.getConnectionInfo();
                tag = info != null ? info.getSSID() : null;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                tag = String.valueOf(localNetworkInfo.getSubtype());
            }
        }
        if (tag == null) {
            tag = "unKnow network tag";
        }
        LogUtils.i("Network tag:" + tag);
        return tag;
    }

    /**
     * 判断网络类型：2G，3G，4G，WIFI
     */
    public static String getNetworkType(Context context) {
        int type = NetUtils.getNetWorkType(context);
        switch (type){
            case NetUtils.NETWORK_WIFI:
                return "WIFI";
            case NetUtils.NETWORK_4G:
                return "4G";
            case NetUtils.NETWORK_3G:
                return "3G";
            case NetUtils.NETWORK_2G:
                return "2G";
            case NetUtils.NETWORK_NO:
                return "no network";
            case NetUtils.NETWORK_BLUETOOTH:
                return "BLUETOOTH";
            case NetUtils.NETWORK_UNKNOWN:
                return "unKnow network,network type:" + type;
            default:
                return "unKnow network,network type:" + type;
        }
    }


    public static String getSIMType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simType = "unknown sim type";
        String iNumeric = telephonyManager.getSimOperator();
        if (iNumeric != null) {
            if (iNumeric.equals("46000") || iNumeric.equals("46002")||iNumeric.equals("46007")) {
                // 中国移动
                simType = "中国移动";
            } else if (iNumeric.equals("46001")||iNumeric.equals("46006")) {
                // 中国联通
                simType = "中国联通";
            } else if (iNumeric.equals("46003")||iNumeric.equals("46005")||iNumeric.equals("46011")) {
                // 中国电信
                simType = "中国电信";
            }
        }
        return simType;
    }

    /**
     * 当前是否为中国电信2G网络
     * @param context
     * @return
     */
    public static boolean isDianXinAnd2GNet(Context context) {
        return getNetworkType(context).equalsIgnoreCase("2G") && getSIMType(context).equalsIgnoreCase("中国电信");
    }
}
