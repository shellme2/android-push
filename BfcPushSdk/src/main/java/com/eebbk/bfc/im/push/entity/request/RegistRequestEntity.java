package com.eebbk.bfc.im.push.entity.request;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;

/**
 * 注册请求
 */
@CommandValue(Command.REGIST_REQUEST)
public class RegistRequestEntity extends RequestEntity {

	@TagValue(1)
	private int RID; //流水号
	
	@TagValue(10)
	private int platform; // 平台类型

	@TagValue(11)
	private String appKey; // app标识

	@TagValue(12)
	private String pkgName; // app包名

	@TagValue(13)
	private String deviceId; // 设备唯一识别码

	@TagValue(14)
	private int sdkVerison; // 系统sdk版本号

	@TagValue(15)
	private String sysName; // 系统名

	@TagValue(16)
	private String sysVersion; // 系统版本

	@TagValue(17)
	private String imei; // 移动设备国际身份码

	@TagValue(18)
	private String imsi; // 国际移动用户识别码

	@TagValue(19)
	private String mac; // 硬件地址

	@TagValue(20)
	private String modelNumber; // 手机型号

	@TagValue(21)
	private String basebandVersion; // 基带版本号

	@TagValue(22)
	private String buildNumber; // 编译号

	@TagValue(23)
	private String resolution; // 分辨率

	public int getRID() {
		return RID;
	}

	public void setRID(int rID) {
		RID = rID;
	}

	public int getPlatform() {
		return platform;
	}

	public void setPlatform(int platform) {
		this.platform = platform;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getPkgName() {
		return pkgName;
	}

	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getSdkVerison() {
		return sdkVerison;
	}

	public void setSdkVerison(int sdkVerison) {
		this.sdkVerison = sdkVerison;
	}

	public String getSysName() {
		return sysName;
	}

	public void setSysName(String sysName) {
		this.sysName = sysName;
	}

	public String getSysVersion() {
		return sysVersion;
	}

	public void setSysVersion(String sysVersion) {
		this.sysVersion = sysVersion;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getModelNumber() {
		return modelNumber;
	}

	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}

	public String getBasebandVersion() {
		return basebandVersion;
	}

	public void setBasebandVersion(String basebandVersion) {
		this.basebandVersion = basebandVersion;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
}
