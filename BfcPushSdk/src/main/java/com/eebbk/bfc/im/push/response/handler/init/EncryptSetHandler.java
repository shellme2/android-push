package com.eebbk.bfc.im.push.response.handler.init;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;
import com.eebbk.bfc.im.push.util.LogUtils;

/**
 * 加密设置处理类
 */
public class EncryptSetHandler extends SyncHandler {

    private static final java.lang.String TAG = "EncryptSetHandler";

    public EncryptSetHandler(PushApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request == null) {
            return;
        }

        if (response.isSuccess()) {
            // 数据加密后开始初始化
            LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_SET_ENCRYPT,"ResponseSuccess","set encrypt success ,then init  !!!");

            cancelRetry(request.getCommand());
            init(app);
        } else {
            LogUtils.e(TAG, "set encrypt error:" + response.getResponseEntity());
            if (response.isPublicKeyExpire()) {
                app.requestPublicKey(null);
            } else {
                startRetry(request);
            }
            // 这里是数据无加密直接进行初始化
            LogUtils.w("data is not encrypted,but init...");
            init(app);
        }
    }

    /**
     * 初始化
     */
    public static void init(PushApplication app) {
        if (app.isRegistered()) {
            if(app.isLogin()){
                LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_SET_ENCRYPT,"isLogin","is login now,so just set alias and tag request  !!!");
                app.setAliasAndTagRequest(null,null,null);
                return;
            }
            LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_SET_ENCRYPT,"isRegistered","is Registered now,so just set alias and tag request  !!!");
            app.login();
            return;
        }
        LogUtils.d(TAG,LogTagConfig.LOG_TAG_FLOW_SET_ENCRYPT,"NotRegister","you has not register,to register.");
        app.register();
    }
}
