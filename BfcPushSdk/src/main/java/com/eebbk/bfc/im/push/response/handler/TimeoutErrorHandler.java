package com.eebbk.bfc.im.push.response.handler;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.init.EncryptSetHandler;
import com.eebbk.bfc.im.push.util.LogUtils;

/**
 * 超时请求处理，主要是做超时请求重试
 */
public class TimeoutErrorHandler extends SyncHandler {
    private static final String TAG = "TimeoutErrorHandler";

    public TimeoutErrorHandler(PushApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        // 这里对超时请求错误进行统一处理
        if (request != null) {
            LogUtils.e(TAG, "timeout request:" + request.getRequestEntity());
            handleRequestTimeout(request);
        } else {
            LogUtils.e(TAG, "request is null!");
        }
    }

    private void handleRequestTimeout(Request request) {
        LogUtils.d("handleRequestTimeout...");
        startRetry(request);
        if (request.isOnRetry()) {
            return;
        }
        int cmd = request.getCommand();
        /**
         * 如果获取公钥请求或者加密设置请求超时，在连接正常的
         * 情况下可直接跑注册登录流程，增加推送初始化的成功率
         */
        if (cmd == Command.PUBLIC_KEY_REQUEST || cmd == Command.ENCRYPT_SET_REQUEST) {
            if (app.isClosed()) {
                return;
            }
            if (!app.isLogin()) {
                EncryptSetHandler.init(app);
            }
        }
    }
}
