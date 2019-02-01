package com.eebbk.bfc.im.push.response.handler.init;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.entity.response.LoginResponseEntity;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.RandomUtil;
import com.eebbk.bfc.im.push.util.TimeFormatUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class LoginHandler extends SyncHandler {

    private static final String TAG = "LoginHandler";

    private long reInitDelay;

    private AtomicInteger reInitDelayTag = new AtomicInteger(0);

    private Runnable reInitTask;

    public LoginHandler(final PushApplication app) {
        super(app);
        reInitTask = new Runnable() {
            @Override
            public void run() {
                app.register();
            }
        };
    }

    @Override
    public void handle(Request request, Response response) {
        if (request == null) {
            return;
        }

        if (response == null) {
            LogUtils.e(TAG, "response is null.");
            return;
        }

        if (response.isSuccess()) {
            cancelRetry(request.getCommand());
            retryHandler.removeCallbacks(reInitTask);
            reInitDelayTag.set(0);
            LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_LOGIN,"sync login success.");
            app.setInitCalled(false);
            app.callOnInitSuccessListener();
            
        } else {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_LOGIN, "sync login fail,error:" + response.getResponseEntity());
            if (response.isUnKnowRegisterId() || response.isRegisterTokenInvalid()) {
                app.clearSyncRegisterInfo();

                LogUtils.e(TAG, "IM服务器切换中信息日志,hostname:" + app.getHostname() + ",port:" + app.getPort());
                // 注册失败如果是提示registerId不存在就重新注册并且清除本地所有数据
                reInit(request);
            } else {
                startRetry(request);
                LogUtils.w("retry send login request...");
                LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_LOGIN, "IM登录失败,正在尝试重新登录,hostname:" + app.getHostname() + ",port:" + app.getPort());
            }
        }
    }

    private void reInit(Request request) {
        reInitDelay(request.getTimeout());
        retryHandler.removeCallbacks(reInitTask);
        retryHandler.postDelayed(reInitTask, reInitDelay);
    }

    private void reInitDelay(int timeout) {
        int start = (int) (Math.pow(2, reInitDelayTag.get()) * 1000);
        int end = (int) (Math.pow(2, reInitDelayTag.get() + 1) * 1000);
        reInitDelay = (long) RandomUtil.getRandom(start, end);
        if (reInitDelay >= timeout) {
            reInitDelay = timeout;
        } else {
            reInitDelayTag.incrementAndGet();
            LogUtils.i("to increment the reInitDelayTag:" + reInitDelayTag.get() + ",reInitDelay:" + reInitDelay);
        }
        LogUtils.i("retry period is [" + TimeFormatUtil.format(reInitDelay) + "]");
    }

}
