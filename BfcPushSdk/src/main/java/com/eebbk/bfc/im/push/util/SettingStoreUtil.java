package com.eebbk.bfc.im.push.util;


import android.content.Context;
import android.provider.Settings;

public class SettingStoreUtil {

    //构造函数私有，防止恶意新建
    private SettingStoreUtil(){}

    private static final String HOST_PACKAGE_NAME="com.eebbk.bfc.im.host_package_name";

    public static boolean putHostPackgName(Context context,String value){
        return Settings.Global.putString(context.getContentResolver(),HOST_PACKAGE_NAME,value);
    }

    public static String getHostPackgName(Context context){
        return Settings.Global.getString(context.getContentResolver(),HOST_PACKAGE_NAME);
    }
}
