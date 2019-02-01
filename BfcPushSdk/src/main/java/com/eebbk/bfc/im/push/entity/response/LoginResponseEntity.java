package com.eebbk.bfc.im.push.entity.response;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;

/**
 * 登录反馈
 */
@CommandValue(Command.LOGIN_RESPONSE)
public class LoginResponseEntity extends ResponseEntity {
	
	@TagValue(1)
	private int RID;
	
	@TagValue(2)
	private int code;
	
	@TagValue(3)
	private String desc;
	
	@TagValue(10)
	private String encryptKey;
	
	@TagValue(11)
	private int encryptType;
	
	@TagValue(12)
	private long serverTime;

	public int getRID() {
		return RID;
	}

	public void setRID(int rID) {
		RID = rID;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int respCode) {
		this.code = respCode;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String respDesc) {
		this.desc = respDesc;
	}

	public String getEncryptKey() {
		return encryptKey;
	}

	public void setEncryptKey(String encryptKey) {
		this.encryptKey = encryptKey;
	}

	public int getEncryptType() {
		return encryptType;
	}

	public void setEncryptType(int encryptType) {
		this.encryptType = encryptType;
	}

	public long getServerTime() {
		return serverTime;
	}

	public void setServerTime(long serverTime) {
		this.serverTime = serverTime;
	}
}
