package com.eebbk.bfc.im.push.listener;

/**
 * 连接断开监听，当连接断开后会调用此监听
 */
public interface OnConnectInterruptListener {
    void onInterrupt(boolean reconnect);
}
