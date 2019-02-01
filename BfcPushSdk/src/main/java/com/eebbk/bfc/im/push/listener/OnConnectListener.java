package com.eebbk.bfc.im.push.listener;


import com.eebbk.bfc.im.push.exception.ConnectException;

/**
 * TCP连接监听
 */
public interface OnConnectListener {

	/**
	 * 连接成功
	 * @param socketSecretKey 每次连接的随机uuid
     */
	void onConnected(byte[] socketSecretKey, String hostname, int port);

	/**
	 * 开始尝试连接
     */
	void onStartConnect(String hostname, int port);

	/**
	 * 连接断开
	 */
	void onDisconnected(boolean reconnect);

	/**
	 * 连接失败
     */
	void onFailed(ConnectException error, String hostname, int port);
}
