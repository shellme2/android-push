package com.eebbk.bfc.im.push.listener;

public interface OnSpecialConnectListener {

    void onConnectSuccess(String hostname, int port);

    void onConnectFailed(String hostname, int port, String errorMsg);

    void onError(String error);
}
