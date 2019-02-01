package com.eebbk.bfc.im.push.util;

import android.os.Environment;

import com.eebbk.bfc.im.push.SDKVersion;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/5/11 9:29
 * Email:  zengjingfang@foxmail.com
 */
public class PushAnalyzeUtil {

    private static final String FILE_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+
            "/config/debug/bfc/push";

    public static void startAnalyze() {

        // push sdk 库的情况
        String nativeSdkInfo =  "SdkName: "+ SDKVersion.getLibraryName()
                            + "\nversion: " + SDKVersion.getVersionName()
                            + "\ncode: " + SDKVersion.getSDKInt()
                            + "\nbuild: " + SDKVersion.getBuildName();
//        PushApplication.initInstance();
    }
}
