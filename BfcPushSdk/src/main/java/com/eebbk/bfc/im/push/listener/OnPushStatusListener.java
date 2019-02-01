package com.eebbk.bfc.im.push.listener;

/**
 * 返回当前推送状态给上层
 * 2017/11/7
 * @author hesn
 */

public interface OnPushStatusListener {

    /**
     * 返回推送状态
     * @param status {@link Status#RECEIVE 接收到推送消息}
     * @param values
     */
    void onPushStatus(int status, Object...values);

    interface Status{
        int RECEIVE = 1;
        int LOG = 2;
        int ERROR = 3;
        int CONNECTED = 4;
        int DISCONNECTED = 5;
    }
}
