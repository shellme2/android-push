package com.eebbk.bfc.im.push.request;

import com.eebbk.bfc.im.push.bean.SyncRegistInfo;
import com.eebbk.bfc.im.push.entity.request.LoginRequestEntity;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.util.RandomUtil;
import com.eebbk.bfc.im.push.util.TimeFormatUtil;

public class LoginRequest extends Request {

    public LoginRequest(SyncApplication app, LoginRequestEntity loginRequestEntity, OnReceiveListener onReceiveListener) {
        super(app, loginRequestEntity);
        this.isNeedResponse = true;
        // 无限重试
        this.isNeedRetry = true;
//        this.maxRetryCount = Integer.MAX_VALUE;
        this.onReceiveListener = onReceiveListener;
    }

    @Override
    public void retry() {
        app.getSyncRegistInfoSafely(new OnGetCallBack<SyncRegistInfo>() {
            @Override
            public void onGet(SyncRegistInfo syncRegistInfo) {
                LoginRequestEntity entity = app.getRequestEntityFactory().createLoginRequestEntity(syncRegistInfo.getRegistId(), syncRegistInfo.getRegistToken());
                requestEntity = entity;
                LogUtils.d("to retry login,entity:" + requestEntity);
                send();
            }
        });
    }

    @Override
    protected void computeDelay() {
        int start = (int) (Math.pow(2, retryDelayTag.get()) * 1000);
        int end = (int) (Math.pow(2, retryDelayTag.get() + 1) * 1000);
        retryDelayTime = (long) RandomUtil.getRandom(start, end);
        if (retryDelayTime >= timeout) {
            retryDelayTime = timeout;
        } else {
            retryDelayTag.incrementAndGet();
            LogUtils.i("to increment the retryPeriodTag:" + retryDelayTag.get() + ",retryPeriod:" + retryDelayTime);
        }
        LogUtils.i("retry period is [" + TimeFormatUtil.format(retryDelayTime) + "]");
    }
}
