package com.eebbk.bfc.im.push.listener;

import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

import java.util.List;

public abstract class OnAliasAndTagsListener implements OnReceiveListener {

    @Override
    public void onReceive(Request request, Response response) {
        if(request==null||response==null){
            onFail(null,null,"未知错误");
            return;
        }
        AliasAndTagsRequestEntity entity=(AliasAndTagsRequestEntity)request.getRequestEntity();
        if(response.isSuccess()){
            onSuccess(entity.getAlias(),entity.getTagsList());
        }else {
            onFail(entity.getAlias(),entity.getTagsList(),response.getDesc());
        }
    }

    public abstract void onSuccess(String alias, List<String> tags);
    public abstract void onFail(String alias, List<String> tags,String errorMsg);
}
