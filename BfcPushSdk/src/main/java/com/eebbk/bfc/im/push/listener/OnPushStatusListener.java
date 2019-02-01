package com.eebbk.bfc.im.push.listener;

public interface OnPushStatusListener {

    int DISCONNECTED = 0;

    int CONNECTING = 1;

    int CONNECTED = 2;

    int CONNECT_FAILED = 3;

    /**
     * IM连接状态监听
     *
     * @param connectStatus true表示已连接，false表示未连接
     */
    void onConnectStatus(int connectStatus);

    /**
     * IM设备登录监听
     *
     * @param registerId
     */
    void onLogin(long registerId);
}
