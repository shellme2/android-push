package com.eebbk.bfc.im.push.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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
        ConnectivityManager cwjManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netWorkInfo = cwjManager.getActiveNetworkInfo();
        if (netWorkInfo != null && netWorkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getNetworkTag(Context context) {
        String tag = null;
        ConnectivityManager localConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo localNetworkInfo = localConnectivityManager.getActiveNetworkInfo();
        if ((localNetworkInfo != null) && (localNetworkInfo.isAvailable())) {
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
            tag = "unknow network tag";
        }
        LogUtils.i("Network tag:" + tag);
        return tag;
    }

    /**
     * 判断手机是否wifi联网
     */
    public static boolean isWifiNet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info;
        if (connectivity != null) {
            info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTypeName().equals("WIFI")
                            && info[i].isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断网络类型：2G，3G，4G，WIFI
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager localConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo localNetworkInfo = localConnectivityManager.getActiveNetworkInfo();
        if ((localNetworkInfo != null) && (localNetworkInfo.isAvailable())) {
            int i = localNetworkInfo.getType();
            if (i == ConnectivityManager.TYPE_WIFI) {
                return "WIFI";
            } else if (i == ConnectivityManager.TYPE_MOBILE) {
                TelephonyManager localTelephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                int networkType = localTelephonyManager.getNetworkType();
                switch (networkType) {
                    case 1:
                    case 2:
                    case 4:
                    case 7:
                    case 11:
                        return "2G";
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 10:
                    case 12:
                    case 14:
                    case 15:
                        return "3G";
                    case 13:
                        return "4G";
                    default:
                        return "unkonw network,network type:" + networkType;
                }
            }
            return "unkonw network,type:" + i;
        }
        return "no network";
    }

    public static String getSIMType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simType = "unknown sim type";
        String iNumeric = telephonyManager.getSimOperator();
        if (iNumeric != null) {
            if (iNumeric.equals("46000") || iNumeric.equals("46002")) {
                // 中国移动
                simType = "中国移动";
            } else if (iNumeric.equals("46001")) {
                // 中国联通
                simType = "中国联通";
            } else if (iNumeric.equals("46003")) {
                // 中国电信
                simType = "中国电信";
            }
        }
        return simType;
    }

    public static String getWifiIpAddress(Context context) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启  
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + (i >> 24 & 0xFF);
    }

    public static String getMobileIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr
                        .hasMoreElements(); ) {
                    InetAddress inetAddress = ipAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && InetAddressUtil.isIPv4Address(inetAddress.getHostAddress())) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }
}
