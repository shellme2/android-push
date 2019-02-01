package com.eebbk.bfc.im.push;

import android.content.Context;

import com.eebbk.bfc.im.push.util.JsonUtil;
import com.eebbk.bfc.im.push.version.Build;

/**
 * @author liuyewu
 * @company EEBBK
 * @function version interface
 * @date 2016/10/22
 */
public class SDKVersion {

    /**
     * 获取库名称
     *
     * @return
     */
    public static String getLibraryName() {
        return Build.LIBRARY_NAME;
    }

    /**
     * 构建时的版本值，如：1, 2, 3, ...
     *
     * @return 返回版本值
     */
    public static int getSDKInt() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 版本名称，如：1.0.0, 2.1.2-alpha, ...
     *
     * @return 返回版本名称
     */
    public static String getVersionName() {
        return Build.VERSION.VERSION_NAME;
    }

    /**
     * 构建版本以及时间，主要从git获取,由GIT_TAG + "_" + GIT_SHA + "_" + BUILD_TIME组成
     *
     * @return 返回构建版本以及时间
     */
    public static String getBuildName() {
        return Build.BUILD_NAME;
    }

    /**
     * 构建时间
     *
     * @return 返回构建时间
     */
    public static String getBuildTime() {
        return Build.BUILD_TIME;
    }

    /**
     * 构建时的git 标签
     *
     * @return 返回标签
     */
    public static String getBuildTag() {
        return Build.GIT_TAG;
    }

    /**
     * 构建时的git HEAD值
     *
     * @return 返回HEAD值
     */
    public static String getBuildHead() {
        return Build.GIT_HEAD;
    }

    public static String getSdkInfo() {
        SdkInfo sdkInfo = new SdkInfo(SDKVersion.getLibraryName(), SDKVersion.getVersionName(), SDKVersion.getSDKInt(), SDKVersion.getBuildTime());
        return JsonUtil.toJson(sdkInfo);
    }

    public static String getSdkInfo(Context context) {
        SdkInfo sdkInfo = new SdkInfo(SDKVersion.getLibraryName(), SDKVersion.getVersionName(), SDKVersion.getSDKInt(), SDKVersion.getBuildTime());
        sdkInfo.setAppName(context.getPackageName());
        return JsonUtil.toJson(sdkInfo);
    }

    public static class SdkInfo{

        private String appName;
        private String sdkName;
        private String sdkVersionName;
        private int sdkVersionCode;
        private String sdkBuildTime;

        public SdkInfo(String sdkName, String sdkVersionName, int sdkVersionCode, String sdkBuildTime) {
            this.sdkName = sdkName;
            this.sdkVersionName = sdkVersionName;
            this.sdkVersionCode = sdkVersionCode;
            this.sdkBuildTime = sdkBuildTime;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getSdkName() {
            return sdkName;
        }

        public void setSdkName(String sdkName) {
            this.sdkName = sdkName;
        }

        public String getSdkVersionName() {
            return sdkVersionName;
        }

        public void setSdkVersionName(String sdkVersionName) {
            this.sdkVersionName = sdkVersionName;
        }

        public int getSdkVersionCode() {
            return sdkVersionCode;
        }

        public void setSdkVersionCode(int sdkVersionCode) {
            this.sdkVersionCode = sdkVersionCode;
        }

        public String getSdkBuildTime() {
            return sdkBuildTime;
        }

        public void setSdkBuildTime(String sdkBuildTime) {
            this.sdkBuildTime = sdkBuildTime;
        }
    }

}
