// IConnectCallback.aidl
package com.eebbk.bfc.im.push;

// Declare any non-default types here with import statements

interface IConnectCallback {

    void onConnected(String hostname, int port);

    void onFailed(String hostname, int port, String errorMsg);
}
