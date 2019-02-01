package com.eebbk.bfc.im.push.exception;

/**
 * 同步错误异常
 */
public class SyncException extends Exception {

    public SyncException() {
        super();
    }

    public SyncException(String errorMsg) {
        super(errorMsg);
    }

    public SyncException(Throwable throwable) {
        super(throwable);
    }

    public SyncException(String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
    }
}
