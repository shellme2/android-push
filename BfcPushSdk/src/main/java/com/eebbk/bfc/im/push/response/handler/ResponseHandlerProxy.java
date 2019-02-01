package com.eebbk.bfc.im.push.response.handler;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应处理
 */
public class ResponseHandlerProxy {

    private Map<Integer, SyncHandler> handlerMap = new HashMap<>();

    public void regist(int command, SyncHandler syncHandler) {
        handlerMap.put(command, syncHandler);
    }

    public void handle(Request request, Response response) {
        if (response == null) {
            LogUtils.e("response is null");
            return;
        }

        int command = response.getCommand();
        SyncHandler syncHandler = handlerMap.get(command);
        if (syncHandler != null) {
            syncHandler.handle(request, response);
        } else {
            LogUtils.w("syncHandler is null,you must first regist the syncHandler:" + response.getResponseEntity());
        }
    }

    public void stop() {
        SyncHandler.cancelAllRetry();
        for (Map.Entry<Integer, SyncHandler> entry : handlerMap.entrySet()) {
            entry.getValue().recycle();
        }
    }
}
