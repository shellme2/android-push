package com.eebbk.bfc.im.push.response.handler;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.util.LogUtils;

public class SendErrorHandler extends SyncHandler {
    private static final String TAG = "SendErrorHandler";

    public SendErrorHandler(PushApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request != null) {
            LogUtils.e( TAG, "local send error,retry send request:" + request.getRequestEntity());
            handleSendErrorRequest(request);
        } else {
            LogUtils.e( TAG, "local send error,request is null!");
        }
    }

    private void handleSendErrorRequest(Request request) {
        LogUtils.d("handleSendErrorRequest");
        startRetry(request);
    }
}
