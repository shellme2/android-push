package com.eebbk.bfc.im.push.response.handler.init;

import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.bean.AliasAndTagsInfo;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;

public class AliasAndTagHandler extends SyncHandler {

    private static final String TAG = "AliasAndTagHandler";

    public AliasAndTagHandler(PushApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request == null) {
            return;
        }

        if (response == null) {
            LogUtils.e(TAG,"AliasAndTag response is null.");
            return;
        }

        if (response.isSuccess()) {
            cancelRetry(request.getCommand());
            LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_SET_ALIAS,"init AliasAndTag success, Response:" + response.getResponseEntity());
            storAndSend(request);
        } else {
            LogUtils.d(TAG,LogTagConfig.LOG_TAG_FLOW_SET_ALIAS,"init AliasAndTag error, Response:" + response.getResponseEntity());
            startRetry(request);
        }
    }

    private void storAndSend(Request request){
        String alias = null;
        AliasAndTagsRequestEntity entity = null;
        try {
            entity = (AliasAndTagsRequestEntity) request.getRequestEntity();

            alias = entity.getAlias();
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        }

        if(TextUtils.isEmpty(alias)){
            LogUtils.e(TAG,LogTagConfig.LOG_TAG_FLOW_SET_ALIAS," Alias is empty ,then set stop push to be true !!! ");
            StoreUtil.saveIsStopPush(new PhoneStore(app.getContext()),true);
            return;
        }else {
            StoreUtil.saveIsStopPush(new PhoneStore(app.getContext()),false);
        }
        LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_SET_ALIAS," Set Alias success ,then send push sync trigger and save alias !!! ");
        app.sendPushSyncTrigger(alias,null);

        AliasAndTagsInfo aliasAndTagsInfo=new AliasAndTagsInfo();
        aliasAndTagsInfo.setTags(entity.getTag());
        aliasAndTagsInfo.setAlias(alias);
        aliasAndTagsInfo.setSet(true);
        StoreUtil.saveAliasAndTag(new PhoneStore(app.getContext()),aliasAndTagsInfo);
        LogUtils.d("saveAliasAndTag success....");


    }

}
