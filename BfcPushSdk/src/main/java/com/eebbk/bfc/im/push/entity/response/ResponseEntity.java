package com.eebbk.bfc.im.push.entity.response;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.entity.Entity;

/**
 * 响应实体基类
 */
@CommandValue(0)
public abstract class ResponseEntity extends Entity {

	public int getRID() {
		return 0;
	}

	public void setRID(int rID) {

	}

	public int getCode() {
		return 0;
	}

	public void setCode(int code) {

	}

	public String getDesc() {
		return null;
	}

	public void setDesc(String desc) {

	}
}
