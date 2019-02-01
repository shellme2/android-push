package com.eebbk.bfc.im.push.entity.response;

public class ResponseCode {

	/**
	 * 返回正确
	 */
	public static final int CODE_SUCCESS = 20000;
	
	/**
	 * 密码错误
	 */
	public static final int CODE_WRONG_PWD = 20001;
	
	/**
	 * appkey和packname不匹配
	 */
	public static final int CODE_AK_NOT_MATCH_PCK = 20001;
	
	/**
	 * pckname不存在
	 */
	public static final int CODE_PCK_NOT_EXIST = 20002;
	
	
}
