package com.eebbk.bfc.im.push.listener;

import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.error.ErrorCode;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

import java.util.List;

public abstract class OnAliasAndTagsListener implements OnReceiveListener {

    @Override
    public void onReceive(Request request, Response response) {
        if(request==null){
            onFail(null,null,"request is null,please check!", ErrorCode.EC_REQUEST_NULL);
            return;
        }
        if(response==null){
            onFail(null,null,"response is null,please check!", ErrorCode.EC_RESPONSE_NULL);
            return;
        }

        RequestEntity requestEntity= request.getRequestEntity();
        if(requestEntity instanceof AliasAndTagsRequestEntity){
            AliasAndTagsRequestEntity entity=(AliasAndTagsRequestEntity)request.getRequestEntity();
            if(response.isSuccess()){
                onSuccess(entity.getAlias(),entity.getTagsList());
            }else {
                onFail(entity.getAlias(),entity.getTagsList(),response.getDesc(), ErrorCode.EC_RESPONSE_ERROR );
            }
        }

    }

    public abstract void onSuccess(String alias, List<String> tags);
    public abstract void onFail(String alias, List<String> tags, String errorMsg, String errorCode);
}
