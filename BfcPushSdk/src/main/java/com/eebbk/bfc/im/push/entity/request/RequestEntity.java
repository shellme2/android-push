package com.eebbk.bfc.im.push.entity.request;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.entity.Entity;

/**
 * 请求实体基类
 */
@CommandValue(0)
public abstract class RequestEntity extends Entity implements Cloneable {

	public int getRID() {
		return 0;
	}

	public void setRID(int rID) {

	}

	public RequestEntity cloneRequestEntity() throws CloneNotSupportedException {
		return (RequestEntity) clone();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
