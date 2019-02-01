package com.eebbk.bfc.im.push.response.handler.init;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;

/**
 * 加密设置处理类
 */
public class EncryptSetHandler extends SyncHandler {

    public EncryptSetHandler(SyncApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request == null) {
            return;
        }

        if (response.isSuccess()) {
            // 数据加密后开始初始化
            LogUtils.d("data is encrypted,to init...");
            cancelRetry(request.getCommand());
            init(app);
        } else {
            LogUtils.e("set encrypt error:" + response.getResponseEntity());
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
    public static void init(SyncApplication app) {
        if (app.isRegisted()) {
            LogUtils.d("you has registed,continue login.");
            if(app.isLogined()){
                // TODO: 2016/10/11 别名标签 
                app.setAliasAndTagRequest(null,null,null);
                return;
            }
            app.login();
            return;
        }
        LogUtils.d("you has not registed,to regist.");
        app.regist();
    }
}
