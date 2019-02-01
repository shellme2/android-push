package com.eebbk.bfc.im.push.util.platform;

/**
 * 设备信息获取
 */
public interface Device {

    String DEVICE_ID_KEY = "device_id";

    /**
     * 获取手机型号/固件版本号
     */
    String getModelNumber();

    /**
     * 获取SDK版本号
     */
    int getSysSDKVersion();

    /**
     * 获取android系统版本(固件版本)
     */
    String getAndroidOsVersion();

    /**
     * 获取屏幕分辨率
     */
    String getScreenResolution();

    /**
     * 获取wifi的mac地址，有时候会获取不到
     */
    String getMacAddress();

    /**
     * 获取自定义的deviceId(mac+imei或者uuid)
     */
    String getDeviceId();

    /**
     * 获取手机imei号
     */
    String getImei();

    /**
     * 获取手机imsi号
     */
    String getImsi();

    /**
     * 获取android系统名：手机型号+ 系统版本号
     */
    String getAndroidSysName();

    /**
     * 获取基带版本号
     */
    String getBaseBandVersion();

    /**
     * 获取编译号
     */
    String getBuildNumber();

    String getAppKeyFromMetaData();

    Integer getRidTagFromMetaData();
}
