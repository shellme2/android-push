package com.eebbk.bfc.im.push.response.handler;

import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.init.EncryptSetHandler;

/**
 * 超时请求处理，主要是做超时请求重试
 */
public class TimeoutErrorHandler extends SyncHandler {

    public TimeoutErrorHandler(SyncApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        // 这里对超时请求错误进行统一处理
        if (request != null) {
            LogUtils.e("timeout request:" + request.getRequestEntity());
            handleRequestTimeout(request);
        } else {
            LogUtils.e("request is null!");
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
        if (cmd == Command.PUBLICKEY_REQUEST || cmd == Command.ENCRYPT_SET_REQUEST) {
            if (app.isClosed()) {
                return;
            }
            if (!app.isLogined()) {
                EncryptSetHandler.init(app);
            }
        }
    }
}
