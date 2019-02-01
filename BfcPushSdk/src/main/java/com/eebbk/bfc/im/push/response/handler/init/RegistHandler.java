package com.eebbk.bfc.im.push.response.handler.init;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;

/**
 * 注册响应内部处理
 */
public class RegistHandler extends SyncHandler {

    public RegistHandler(SyncApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request == null) {
            return;
        }

        if (response == null) {
            LogUtils.e("response is null.");
            return;
        }

        if (response.isSuccess()) {
            cancelRetry(request.getCommand());
            LogUtils.d("init regist success.");
            app.login();
        } else {
            LogUtils.d("init regist error:" + response.getResponseEntity());
            startRetry(request);
        }
    }
}
