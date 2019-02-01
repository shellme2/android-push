package com.eebbk.bfc.im.push.response.handler;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

public class SendErrorHandler extends SyncHandler {

    public SendErrorHandler(SyncApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request != null) {
            LogUtils.e("local send error,retry send request:" + request.getRequestEntity());
            handleSendErrorRequest(request);
        } else {
            LogUtils.e("local send error,request is null!");
        }
    }

    private void handleSendErrorRequest(Request request) {
        LogUtils.d("handleSendErrorRequest");
        startRetry(request);
    }
}
