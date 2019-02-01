package com.eebbk.bfc.im.push.listener;

import com.eebbk.bfc.im.push.error.ErrorCode;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

public abstract class OnResultListener implements OnReceiveListener {

    @Override
    public void onReceive(Request request, Response response) {
        if(request==null){
            onFail("request is null,please check!", ErrorCode.EC_REQUEST_NULL);
            return;
        }
        if(response==null){
            onFail("response is null,please check!", ErrorCode.EC_RESPONSE_NULL);
            return;
        }
        if(response.isSuccess()){
            onSuccess();
        }else {
            onFail(response.getDesc(), ErrorCode.EC_RESPONSE_ERROR);
        }
    }

    public abstract void onSuccess();
    public abstract void onFail(String errorMsg, String errorCode);
}
