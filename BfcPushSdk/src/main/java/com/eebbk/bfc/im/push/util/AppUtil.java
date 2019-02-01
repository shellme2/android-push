package com.eebbk.bfc.im.push.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class AppUtil {

    //构造函数私有，防止恶意新建
    private AppUtil(){}

    public static boolean isAppActive(Context context, String pkgName) {
        boolean active = false;
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(pkgName
                    , PackageManager.GET_SHARED_LIBRARY_FILES | PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(e);
        }
        if (applicationInfo != null) {
            if (((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == 0)) {
                LogUtils.i(applicationInfo.packageName + " app is active");
                active = true;
            } else {
                LogUtils.i(applicationInfo.packageName + " app is stopped");
                active = false;
            }
        }
        return active;
    }

    public static boolean isAppRunOnBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        boolean isBackground = false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                /*
				 * BACKGROUND=400 EMPTY=500 FOREGROUND=100 GONE=1000
				 * PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
				 */
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    isBackground = true;
                } else {
                    isBackground = false;
                }
                break;
            }
        }
        return isBackground;
    }

    public static boolean isServiceRunning(Context mContext, String pkgName, String clsName) {
        boolean isRunning = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
        if (myList == null || myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            ComponentName componentName = myList.get(i).service;
            if (componentName.getClassName().equals(clsName) && componentName.getPackageName().equals(pkgName)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static List<ActivityManager.RunningServiceInfo> getRunningServiceList(Context context, String serviceName) {
        List<ActivityManager.RunningServiceInfo> list = new ArrayList<>();
        ActivityManager myAM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
        if (myList == null || myList.size() <= 0) {
            return null;
        }
        for (ActivityManager.RunningServiceInfo runningServiceInfo : myList) {
            if (runningServiceInfo.service.getClassName().equals(serviceName)) {
                list.add(runningServiceInfo);
            }
        }
        return list;
    }

    public static ActivityManager.RunningServiceInfo getRunningService(Context context, String serviceName) {
        ActivityManager.RunningServiceInfo  serviceInfo = null;
        ActivityManager myAM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
        if (myList == null || myList.size() <= 0) {
            return null;
        }
        for (ActivityManager.RunningServiceInfo runningServiceInfo : myList) {
            if (runningServiceInfo.service.getClassName().equals(serviceName)
                    && context.getPackageName().equals(runningServiceInfo.service.getPackageName())) {
                serviceInfo = runningServiceInfo;
            }
        }
        return serviceInfo;
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public static int getPidByName(Context context, String pName) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.processName.equals(pName)) {
                return appProcess.pid;
            }
        }
        return 0;
    }

    public static Integer getRIDTag(Context context) {
        ApplicationInfo appInfo = null;
        Integer ridTag = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData == null) {
                throw new IllegalArgumentException("you must set SYNC_RID_TAG in AndroidManifest.xml:" + context.getPackageName());
            }
            ridTag = appInfo.metaData.getInt("SYNC_RID_TAG");
            LogUtils.i("meta-data rid_tag:" + ridTag);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(e);
        }
        if (ridTag == null) {
            throw new IllegalArgumentException("you must set SYNC_RID_TAG in AndroidManifest.xml:" + context.getPackageName());
        }
        return ridTag;
    }

    public static String getAppKey(Context context) {
        ApplicationInfo appInfo = null;
        String appKey = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            appKey = appInfo.metaData.getString("SYNC_APPKEY");
            LogUtils.i("meta-data appkay:" + appKey);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(e);
        }
        if (appKey == null) {
            LogUtils.e("you must set SYNC_APPKEY in AndroidManifest.xml");
        }
        return appKey;
    }

    public static boolean checkRidTag(Context context, int rid) {
        Integer ridTag = getRIDTag(context);
        int check = rid / IDUtil.RID_BASE;
        if (check == ridTag) {
            return true;
        }
        return false;
    }
}
