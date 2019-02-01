package com.eebbk.bfc.im.push.util.platform;

/**
 * 设备信息获取
 */
public interface Device {

    String DEVICE_ID_KEY = "device_id";

    /**
     * 获取手机型号/固件版本号
     * @return
     */
    String getModelNumber();

    /**
     * 获取SDK版本号
     * @return
     */
    int getSysSDKVersion();

    /**
     * 获取android系统版本(固件版本)
     * @return
     */
    String getAndroidOsVersion();

    /**
     * 获取屏幕分辨率
     * @return
     */
    String getScreenResolution();

    /**
     * 获取wifi的mac地址，有时候会获取不到
     * @return
     */
    String getMacAddress();

    /**
     * 获取自定义的deviceId(mac+imei或者uuid)
     * @return
     */
    String getDeviceId();

    /**
     * 获取手机imei号
     * @return
     */
    String getImei();

    /**
     * 获取手机imsi号
     * @return
     */
    String getImsi();

    /**
     * 获取android系统名：手机型号+ 系统版本号
     * @return
     */
    String getAndroidSysName();

    /**
     * 获取基带版本号
     * @return
     */
    String getBasebandVersion();

    /**
     * 获取编译号
     * @return
     */
    String getBuildNumber();

    String getAppKeyFromMetaData();

    Integer getRidTagFromMetaData();
}
