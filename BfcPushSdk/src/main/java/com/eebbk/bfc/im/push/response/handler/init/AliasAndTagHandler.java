package com.eebbk.bfc.im.push.response.handler.init;

import com.eebbk.bfc.im.push.bean.AliasAndTagsInfo;
import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;

public class AliasAndTagHandler extends SyncHandler {

    public AliasAndTagHandler(SyncApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request == null) {
            return;
        }

        if (response == null) {
            LogUtils.e("AliasAndTag response is null.");
            return;
        }

        if (response.isSuccess()) {
            cancelRetry(request.getCommand());
            LogUtils.d("init AliasAndTag success.");

            storAliasAndTags(request);
            app.sendPushSyncTrigger(null);
        } else {
            LogUtils.d("init AliasAndTag error:" + response.getResponseEntity());
            startRetry(request);
        }
    }

    private void storAliasAndTags(Request request){
        AliasAndTagsRequestEntity entity=(AliasAndTagsRequestEntity)request.getRequestEntity();
        AliasAndTagsInfo aliasAndTagsInfo=new AliasAndTagsInfo();
        aliasAndTagsInfo.setTags(entity.getTag());
        aliasAndTagsInfo.setAlias(entity.getAlias());
        aliasAndTagsInfo.setSet(true);
        StoreUtil.saveAliasAndTag(new PhoneStore(app.getContext()),aliasAndTagsInfo);
        LogUtils.d("saveAliasAndTag success....");
    }

}
