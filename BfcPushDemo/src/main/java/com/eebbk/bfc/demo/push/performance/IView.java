package com.eebbk.bfc.demo.push.performance;

import com.eebbk.bfc.im.push.debug.DebugEventInfo;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/21 18:10
 * Email:  zengjingfang@foxmail.com
 */
public interface IView {

    void onEvent(DebugEventInfo debugEventInfo);

    void onDebugModeUpdate(String state);

    void onSendUpdate(int sendSuccessCount, int sendFailedCount, int receivedCount,int receivedTimeoutCount);

    void onTcpConnStateUpdate(String connState);
}
