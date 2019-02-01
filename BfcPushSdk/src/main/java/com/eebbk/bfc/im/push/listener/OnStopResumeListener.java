package com.eebbk.bfc.im.push.listener;

import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

public abstract class OnStopResumeListener implements OnReceiveListener {

    @Override
    public void onReceive(Request request, Response response) {
        if(request==null||response==null){
            onFail("未知错误");
            return;
        }
        AliasAndTagsRequestEntity entity=(AliasAndTagsRequestEntity)request.getRequestEntity();
        if(response.isSuccess()){
            onSuccess();
        }else {
            onFail(response.getDesc());
        }
    }

    public abstract void onSuccess();
    public abstract void onFail(String errorMsg);
}
