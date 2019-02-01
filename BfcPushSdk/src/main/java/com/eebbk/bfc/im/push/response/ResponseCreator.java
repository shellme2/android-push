package com.eebbk.bfc.im.push.response;

import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.entity.response.TimeoutErrorResponseEntity;
import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.request.Request;

public class ResponseCreator {

    public static Response createResponse(PushApplication app, ResponseEntity responseEntity) {
        return new Response(app, responseEntity);
    }

    public static Response createTimeoutResponse(PushApplication app, Request request) {
        TimeoutErrorResponseEntity timeoutErrorResponseEntity = new TimeoutErrorResponseEntity();
        timeoutErrorResponseEntity.setRID(request.getRID());
        timeoutErrorResponseEntity.setCode(Response.Code.TIME_OUT);
        timeoutErrorResponseEntity.setDesc("timeout request,entity:" + request.getRequestEntity());
        timeoutErrorResponseEntity.setRequestEntity(request.getRequestEntity());
        return new Response(app, timeoutErrorResponseEntity);
    }


}
