package com.eebbk.bfc.demo.push.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.eebbk.bfc.http.BfcHttp;
import com.eebbk.bfc.http.config.BfcHttpConfigure;
import com.eebbk.bfc.http.error.BfcHttpError;
import com.eebbk.bfc.http.toolbox.IBfcErrorListener;
import com.eebbk.bfc.http.toolbox.StringCallBack;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.HashMap;

/**
 * @author liuyewu
 * @company EEBBK
 * @function ReportYunbaPushUtils
 * @date 2016/12/19
 */
public class ReportYunbaPushUtils {
    private static final String TAG = "ReportYunbaPushUtils";
    private static final String PUSH_REPORT_URL = "http://push.eebbk.net/push/PushCommon2/getPlatformIdAndInterval";

    private ReportYunbaPushUtils(){}

    public static String reportYunbaPush(Context context) {
        String tag = System.currentTimeMillis()+"";
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("machineId", Build.SERIAL);
        hashMap.put("appKey", getAppKeyFromMetaData(context, "YUNBA_APPKEY"));
        hashMap.put("packageName", context.getPackageName());
        hashMap.put("appName", getAppName(context));
        hashMap.put("ip", "172.28.10.220");
        hashMap.put("platformId", "3");
        hashMap.put("devicePlatform", "YUNBA");
        hashMap.put("deviceModel", Build.VERSION.RELEASE);
        hashMap.put("osVersion", Build.VERSION.RELEASE);
        hashMap.put("interval", "YUNBA");
        hashMap.put("versionCode", getVersionCode(context));

        //初始化配置
        BfcHttpConfigure.init(context);

        BfcHttp.post(context, PUSH_REPORT_URL, hashMap, null, new StringCallBack() {
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

    private static String getVersionCode(Context context) {
        try {
            int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            return String.valueOf(versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "1";
        }
    }

    private static String getAppKeyFromMetaData(Context context, String key) {
        ApplicationInfo appInfo;
        String appKey = "";
        try {
            appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            appKey = appInfo.metaData.getString(key);
            LogUtils.i(TAG, "meta-data appkay:" + appKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appKey;
    }

    private static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
