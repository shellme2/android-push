package com.eebbk.bfc.demo.push.report;

public class DeviceInfoPojo
{
    private long id;
    /** 机器条码（app注册到IM里的别名也存到这里） */
    private String machineId;
    /** 当前APPKEY */
    private String appKey;
    /** 当前应用包名 */
    private String packageName;
    /** 应用名 */
    private String appName;
    /** 操作系统平台 */
    private String devicePlatform;
    /** 操作系统版本号 */
    private String osVersion;
    /** 当前平台编号 */
    private int platformId;
    /** 小机ip */
    private String ip;
    /** 更新时间 */
    private String updateTime;

    public String getMachineId()
    {
        return this.machineId;
    }

    public void setMachineId(String machineId)
    {
        this.machineId = machineId;
    }

    public String getAppKey()
    {
        return this.appKey;
    }

    public void setAppKey(String appKey)
    {
        this.appKey = appKey;
    }

    public String getPackageName()
    {
        return this.packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public String getDevicePlatform()
    {
        return this.devicePlatform;
    }

    public void setDevicePlatform(String devicePlatform)
    {
        this.devicePlatform = devicePlatform;
    }

    public String getOsVersion()
    {
        return this.osVersion;
    }

    public void setOsVersion(String osVersion)
    {
        this.osVersion = osVersion;
    }

    public int getPlatformId()
    {
        return this.platformId;
    }

    public void setPlatformId(int platformId)
    {
        this.platformId = platformId;
    }

    public String getIp()
    {
        return this.ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public String getUpdateTime()
    {
        return this.updateTime;
    }

    public void setUpdateTime(String updateTime)
    {
        this.updateTime = updateTime;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getAppName()
    {
        return appName;
    }

    public void setAppName(String appName)
    {
        this.appName = appName;
    }

}

