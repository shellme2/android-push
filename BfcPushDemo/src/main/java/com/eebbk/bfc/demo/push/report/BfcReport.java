package com.eebbk.bfc.demo.push.report;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.eebbk.bfc.http.BfcHttp;
import com.eebbk.bfc.http.config.BfcHttpConfigure;
import com.eebbk.bfc.http.error.BfcHttpError;
import com.eebbk.bfc.http.toolbox.IBfcErrorListener;
import com.eebbk.bfc.http.toolbox.StringCallBack;
import com.eebbk.bfc.im.push.util.JsonUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.HashMap;
import java.util.UUID;

/**
 * Desc:  App业务上报给到im_push业务后台
 * Author: ZengJingFang
 * Time:   2017/4/18 11:10
 * Email:  zengjingfang@foxmail.com
 */

public class BfcReport {

    /**
     * im push 平台
     */
    private static final int PLATFORM_PUSH_IM = 4;

    /**
     * 上报im_push业务后台 测试环境
     */
    public static final String PUSH_REPORT_TEST = "http://test.eebbk.net/im_push/api/push/uploadRegisterInfo";

    /**
     * 上报im_push业务后台 测试环境
     */
    public static final String PUSH_REPORT_RELEASE = "http://push.eebbk.net/im_push/api/push/uploadRegisterInfo";



    private static final String TAG = "BfcReport";

    private BfcReport() {
        //私有化构造
    }


    /**
     * 上报给到im_push业务后台
     * 该方案App参考使用
     * 接口负责人：杨一中
     */
    public static String reportToAppImPush(Context context) {
        String tag = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        DeviceInfoPojo dviceInfoPojo = new DeviceInfoPojo();
        dviceInfoPojo.setMachineId(Build.SERIAL);
        dviceInfoPojo.setAppKey(getAppKeyFromMetaData(context, "SYNC_APP_KEY"));
        dviceInfoPojo.setPackageName(context.getPackageName()/*"推送注册的包名"*/);
        dviceInfoPojo.setAppName("推送注册的英文名");
        dviceInfoPojo.setOsVersion(Build.VERSION.RELEASE);
        dviceInfoPojo.setIp(getIP(context));
        dviceInfoPojo.setPlatformId(PLATFORM_PUSH_IM);
        dviceInfoPojo.setDevicePlatform("android");

        HashMap hashMap = new HashMap();
        hashMap.put("data", JsonUtil.toJson(dviceInfoPojo));
        // dviceInfoPojo 转为Json 上报给im_push后台  key:data  接口负责人：杨一中
        // 注意环境切换  测试PUSH_REPORT_TEST 或者 正式PUSH_REPORT_RELEASE
        // 请求返回：resultCode=101002 标识成功  resultCode=101001 标识失败
        //初始化配置
        BfcHttpConfigure.init(context);

        BfcHttp.post(context, PUSH_REPORT_TEST, hashMap, null, new StringCallBack() {
            @Override
            public void onResponse(String response) {
                LogUtils.i(TAG,response);
            }
        }, new IBfcErrorListener() {
            @Override
            public void onError(BfcHttpError error) {
                LogUtils.i(TAG,error.getMessage());
            }
        }, tag);
        BfcHttp.post(context, PUSH_REPORT_RELEASE, hashMap, null, new StringCallBack() {
            @Override
            public void onResponse(String response) {
                LogUtils.i(TAG,response);
            }
        }, new IBfcErrorListener() {
            @Override
            public void onError(BfcHttpError error) {
                LogUtils.i(TAG,error.getMessage());
            }
        }, tag);

        return tag;
    }
    private static String getAppKeyFromMetaData(Context context, String key) {
        ApplicationInfo appInfo;
        String appKey = "";
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            appKey = appInfo.metaData.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appKey;
    }
    private static String getIP(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // 判断wifi是否开启  >>> 不允许自动打开WiFi
            /*if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }*/
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            return intToIp(ipAddress);
        } catch (Exception e) {
            Log.e("Error:", e.toString());
            return "127.0.0.1";
        }

    }
    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }
}
