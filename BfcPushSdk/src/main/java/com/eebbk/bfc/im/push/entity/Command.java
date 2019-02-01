package com.eebbk.bfc.im.push.entity;

/**
 * 各请求类型的命令值
 */
public interface Command {

	/**
	 * 注册
	 */
	int REGISTER_REQUEST = 1;

	/**
	 * 注册响应
	 */
	int REGISTER_RESPONSE = 2;

	/**
	 * 登录
	 */
	int LOGIN_REQUEST = 3;

	/**
	 * 登录响应
	 */
	int LOGIN_RESPONSE = 4;

	/**
	 * 心跳
	 */
	int HEART_BEAT_REQUEST = 7;

	/**
	 * 心跳响应
	 */
	int HEART_BEAT_RESPONSE = 8;


	/**
	 * 获取公钥请求
	 */
	int PUBLIC_KEY_REQUEST = 22;

	/**
	 * 获取公钥响应
	 */
	int PUBLIC_KEY_RESPONSE = 23;

	/**
	 * 加密包装
	 */
	int ENCRYPT_WAPPER = 32;

	/**
	 * 加密设置请求
	 */
	int ENCRYPT_SET_REQUEST = 33;

	/**
	 * 加密设置响应
	 */
	int ENCRYPT_SET_RESPONSE = 34;

	/**
	 * 推送别名和标签设置请求
	 */
	int PUSH_ALIAS_AND_TAG_REQUEST = 100;

	/**
	 * 推送别名和标签设置响应
	 */
	int PUSH_ALIAS_AND_TAG_RESPONSE = 101;

	/**
	 * 推送别名和标签设置请求
	 */
	int PUSH_ALIAS_CHECK_REQUEST = 117;

	/**
	 * 推送别名和标签设置响应
	 */
	int PUSH_ALIAS_CHECK_RESPONSE = 118;

	/**
	 * 推送同步触发请求
	 */
	int PUSH_SYNC_TRIGGER_REQUEST = 110;

	/**
	 * 推送同步触发响应
	 */
	int PUSH_SYNC_TRIGGER_RESPONSE = 111;

	/**
	 * 推送同步通知
	 */
	int PUSH_SYNC_INFORM = 112;

	/**
	 * 推送同步请求
	 */
	int PUSH_SYNC_REQUEST = 113;

	/**
	 * 推送同步请求响应
	 */
	int PUSH_SYNC_RESPONSE = 114;

	/**
	 * 推送同步请求响应完成
	 */
	int PUSH_SYNC_FIN = 115;

	/**
	 * 推送同步请求响应完成应答
	 */
	int PUSH_SYNC_FIN_ACK = 116;

	/**
	 * 其他错误响应
	 */
	int TIMEOUT_ERROR_RESPONSE = 1000;

	int SEND_ERROR_RESPONSE = 1001;
}
