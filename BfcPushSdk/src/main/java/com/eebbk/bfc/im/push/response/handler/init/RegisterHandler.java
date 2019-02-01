package com.eebbk.bfc.im.push.response.handler.init;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;
import com.eebbk.bfc.im.push.util.LogUtils;

/**
 * 注册响应内部处理
 */
public class RegisterHandler extends SyncHandler {
    private static final String TAG = "RegisterHandler";

    public RegisterHandler(PushApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request == null) {
            return;
        }

        if (response == null) {
            LogUtils.e( TAG, "response is null.");
            return;
        }

        if (response.isSuccess()) {
            cancelRetry(request.getCommand());
            LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_REGISTER,"init register success,start login next !!! response: "+ response.getResponseEntity());
            app.login();
        } else {
            LogUtils.w(TAG, LogTagConfig.LOG_TAG_FLOW_REGISTER,"init register error,retry next !!! response:" + response.getResponseEntity());
            LogUtils.w(TAG,LogTagConfig.LOG_TAG_ERROR_KEY,"Register Error"," urlMode设置和App配置的SYNC_APP_KEY不一致!!! 测试环境和正式环境应该保持一致!!!");
            startRetry(request);
        }
    }
}
