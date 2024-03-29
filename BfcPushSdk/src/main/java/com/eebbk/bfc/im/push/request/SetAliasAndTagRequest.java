package com.eebbk.bfc.im.push.request;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.bean.SyncRegisterInfo;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.RandomUtil;
import com.eebbk.bfc.im.push.util.TimeFormatUtil;

import java.util.List;

public class SetAliasAndTagRequest extends Request {

    public SetAliasAndTagRequest(PushApplication app, RequestEntity requestEntity, OnReceiveListener onReceiveListener) {
        super(app, requestEntity);
        this.isNeedResponse = true;
        // 无限重试
        this.isNeedRetry = true;
//        this.maxRetryCount = Integer.MAX_VALUE;
        this.onReceiveListener = onReceiveListener;
    }

    @Override
    public void retry() {
        final AliasAndTagsRequestEntity aliasAndTagsRequestEntity = (AliasAndTagsRequestEntity) requestEntity;
        final String alias = aliasAndTagsRequestEntity.getAlias();
        final List<String> tag = aliasAndTagsRequestEntity.getTagsList();

        app.getSyncRegisterInfoSafely(new OnGetCallBack<SyncRegisterInfo>() {
            @Override
            public void onGet(SyncRegisterInfo syncRegisterInfo) {
                if(syncRegisterInfo == null){
                    LogUtils.e("SetAliasAndTagRequest","syncRegisterInfo is null ,just stop login !!! ");
                    return;
                }
                AliasAndTagsRequestEntity entity = app.getRequestEntityFactory().createAliasAndTagRequestEntity(alias, tag, syncRegisterInfo.getRegisterId());
                requestEntity = entity;
                LogUtils.d("to retry set alias and tag,entity:" + requestEntity);
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
