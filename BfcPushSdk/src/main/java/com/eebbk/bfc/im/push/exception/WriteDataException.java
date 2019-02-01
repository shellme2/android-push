package com.eebbk.bfc.im.push.exception;

/**
 * 数据写出错误
 */
public class WriteDataException extends SyncException {

    public WriteDataException() {
        super();
    }

    public WriteDataException(String errorMsg) {
        super(errorMsg);
    }
}
