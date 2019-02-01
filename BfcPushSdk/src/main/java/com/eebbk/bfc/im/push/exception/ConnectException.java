package com.eebbk.bfc.im.push.exception;

/**
 * 连接错误
 */
public class ConnectException extends SyncException {

    public ConnectException() {
        super();
    }

    public ConnectException(String errorMsg) {
        super(errorMsg);
    }
}
