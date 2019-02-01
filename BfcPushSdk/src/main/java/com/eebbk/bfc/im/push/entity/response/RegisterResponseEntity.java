package com.eebbk.bfc.im.push.entity.response;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;

/**
 * 注册响应实体
 */
@CommandValue(Command.REGISTER_RESPONSE)
public class RegisterResponseEntity extends ResponseEntity {
	
	@TagValue(1)
	private int RID;
	
	@TagValue(2)
	private int code;
	
	@TagValue(3)
	private String desc;
	
	@TagValue(10)
	private long registerId;
	
	@TagValue(11)
	private String registerToken;

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
}
