package com.eebbk.bfc.im.push.service.dispatcher;

public interface StatusCode {

    int SEND_SUCCESS = 0;
    int CONNECT_ERROR = 1;
    int WRITEDATA_ERROR = 2;
    int DATA_SEND_NULL_ERROR = 3;
    int REMOTE_ERROR = 4;
    int SEND_WAITING = 5;
    int OTHER_ERROR = 6;
    int CONNECTED = 7;
    int DISCONNECTED = 8;
    int TIME_OUT = 9;
    int CONNECTION_OBJ_IS_NULL = 10;
}
