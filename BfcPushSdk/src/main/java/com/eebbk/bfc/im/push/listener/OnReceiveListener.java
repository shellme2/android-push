package com.eebbk.bfc.im.push.listener;

import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

/**
 * 发送数据请求的回调
 */
public interface OnReceiveListener {
    void onReceive(Request request, Response response);
}
