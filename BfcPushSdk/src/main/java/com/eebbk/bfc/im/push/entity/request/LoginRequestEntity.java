package com.eebbk.bfc.im.push.entity.request;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;

/**
 * 登录请求
 */
@CommandValue(Command.LOGIN_REQUEST)
public class LoginRequestEntity extends RequestEntity {

	@TagValue(1)
	private int RID;

	@TagValue(10)
	private long registerId;

	@TagValue(11)
	private String registerToken;
	
	@TagValue(12)
	private long sdkVersion;

	@TagValue(13)
	private long imAccountId;// TODO: 2016/10/11  用户信息是否可以去掉

	@TagValue(14)
	private String accountToken;//

	@TagValue(15)
	private long apnsType; // 苹果推送类型

	@TagValue(16)
	private String apnsToken;

	@TagValue(17)
	private String deviceToken;

	@TagValue(18)
	private String imSdkVersionName; // im SDK版本信息

	@TagValue(19)
	private int pushType;

	public int getRID() {
		return RID;
	}

	public void setRID(int rID) {
		RID = rID;
	}

	public long getRegisterId() {
		return registerId;
	}

	public void setRegisterId(long registerId) {
		this.registerId = registerId;
	}

	public String getRegisterToken() {
		return registerToken;
	}

	public void setRegisterToken(String registerToken) {
		this.registerToken = registerToken;
	}

	public long getSdkVersion() {
		return sdkVersion;
	}

	public void setSdkVersion(long sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	public long getImAccountId() {
		return imAccountId;
	}

	public void setImAccountId(long imAccountId) {
		this.imAccountId = imAccountId;
	}

	public String getAccountToken() {
		return accountToken;
	}

	public void setAccountToken(String accountToken) {
		this.accountToken = accountToken;
	}

	public String getApnsToken() {
		return apnsToken;
	}

	public LoginRequestEntity setApnsToken(String apnsToken) {
		this.apnsToken = apnsToken;
		return this;
	}

	public long getApnsType() {
		return apnsType;
	}

	public LoginRequestEntity setApnsType(long apnsType) {
		this.apnsType = apnsType;
		return this;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getImSdkVersionName() {
		return imSdkVersionName;
	}

	public void setImSdkVersionName(String imSdkVersionName) {
		this.imSdkVersionName = imSdkVersionName;
	}

	public int getPushType() {
		return pushType;
	}

	public void setPushType(int pushType) {
		this.pushType = pushType;
	}
}
