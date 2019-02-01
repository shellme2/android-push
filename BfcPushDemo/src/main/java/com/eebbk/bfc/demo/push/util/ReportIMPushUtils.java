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
 * @function
 * @date 2016/12/19
 */
public class ReportIMPushUtils {
    private static final String TAG = "ReportIMPushUtils";
    private static final String PUSH_REPORT_URL = "http://push.eebbk.net/im_push/api/push/uploadRegisterInfo";

    private ReportIMPushUtils(){}

    public static String reportBfcPush(Context context) {
        String tag = System.currentTimeMillis()+"";
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("machineId", Build.SERIAL);
        hashMap.put("appKey", getAppKeyFromMetaData(context, "YUNBA_APPKEY"));
        hashMap.put("packageName", context.getPackageName());
        hashMap.put("appName", getAppName(context));
        hashMap.put("ip", "172.28.10.220");
        hashMap.put("platformId", "3");
        hashMap.put("devicePlatform", "YUNBA");

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
