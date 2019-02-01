package com.eebbk.bfc.im.push.response;


import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncFinResponseEntity;
import com.eebbk.bfc.im.push.response.handler.init.AliasAndTagHandler;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.request.RequestManager;
import com.eebbk.bfc.im.push.response.handler.ResponseHandlerProxy;
import com.eebbk.bfc.im.push.response.handler.SendErrorHandler;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;
import com.eebbk.bfc.im.push.response.handler.PushSyncFinHandler;
import com.eebbk.bfc.im.push.response.handler.PushSyncInformHandler;
import com.eebbk.bfc.im.push.response.handler.PushSyncResponseHandler;
import com.eebbk.bfc.im.push.response.handler.TimeoutErrorHandler;
import com.eebbk.bfc.im.push.response.handler.init.EncryptSetHandler;
import com.eebbk.bfc.im.push.response.handler.init.GetPublicKeyHandler;
import com.eebbk.bfc.im.push.response.handler.init.LoginHandler;
import com.eebbk.bfc.im.push.response.handler.init.RegistHandler;

import java.util.List;

/**
 * 响应派遣器
 */
public class ResponseDispatcher {

    private SyncApplication app;

    private ResponseHandlerProxy responseHandlerProxy;


    public ResponseDispatcher(SyncApplication app) {
        this.app = app;

        SyncHandler setAliasAndTagSyncHandler=new AliasAndTagHandler(app);
        SyncHandler registSyncHandler = new RegistHandler(app);
        SyncHandler loginSyncHandler = new LoginHandler(app);
        SyncHandler getPublicKeySyncHandler = new GetPublicKeyHandler(app);
        SyncHandler encryptSetSyncHandler = new EncryptSetHandler(app);

        /**
         * 推送协议请求处理
         */
        SyncHandler pushSyncFinHandler = new PushSyncFinHandler(app);
        SyncHandler pushSyncInformHandler = new PushSyncInformHandler(app);
        SyncHandler pushSyncResponseHandler = new PushSyncResponseHandler(app);

        SyncHandler timeoutErrorHandler = new TimeoutErrorHandler(app);
        SyncHandler sendErrorHandler = new SendErrorHandler(app);

        responseHandlerProxy = new ResponseHandlerProxy();
        responseHandlerProxy.regist(Command.PUSH_ALIAS_AND_TAG_RESPONSE, setAliasAndTagSyncHandler);
        responseHandlerProxy.regist(Command.REGIST_RESPONSE, registSyncHandler);
        responseHandlerProxy.regist(Command.LOGIN_RESPONSE, loginSyncHandler);
        responseHandlerProxy.regist(Command.PUBLICKEY_RESPONSE, getPublicKeySyncHandler);
        responseHandlerProxy.regist(Command.ENCRYPT_SET_RESPONSE, encryptSetSyncHandler);

        responseHandlerProxy.regist(Command.PUSH_SYNC_FIN, pushSyncFinHandler);
        responseHandlerProxy.regist(Command.PUSH_SYNC_INFORM, pushSyncInformHandler);
        responseHandlerProxy.regist(Command.PUSH_SYNC_RESPONSE, pushSyncResponseHandler);

        responseHandlerProxy.regist(Command.TIMEOUT_ERROE_RESPONSE, timeoutErrorHandler);
        responseHandlerProxy.regist(Command.SEND_ERROR_RESPONSE, sendErrorHandler);
    }

    public void stop() {
        responseHandlerProxy.stop();
    }

    /**
     * 处理收到的推送数据
     */
    public void dispatch(Response response) {
        if (response.isSuccess()) {
            LogUtils.i("dispatch response,entity:" + response.getResponseEntity());
        } else {
            LogUtils.e("dispatch response,entity:" + response.getResponseEntity());
            if (response.isTimeout()) {
                // 超时请求就立即触发心跳包
                app.heartbeat();
                LogUtils.d("timeout request,trigger a heartbeat request to test the connection...");
            }
        }

        RequestManager requestManager = app.getRequestManager();
        Request request = requestManager.find(response.getRID());
        responseHandlerProxy.handle(request, response);
        int respCommand = response.getCommand();
        if (request != null) {
            clearRequest(response, request, requestManager);
            if (respCommand == Command.PUSH_SYNC_RESPONSE) {
                LogUtils.w("request is PUSH_SYNC_RESPONSE");
                dealSyncResponseAndPushResponse(request, response);
            } else {
                dealOtherResponse(request, response);
            }
        } else {
            LogUtils.w("request is null:" + response.getResponseEntity());
           if (respCommand == Command.PUSH_SYNC_FIN) {
                dealPushSyncFinResponse(response, requestManager);
            }
        }

    }

    private void clearRequest(Response response, Request request, RequestManager requestManager) {
        int respCommand = response.getCommand();
        if (respCommand == Command.PUSH_SYNC_FIN) {
//            requestManager.remove(request);
            dealPushSyncFinResponse(response, requestManager);
        }  else {
            requestManager.removeOnceResponse(request);
        }
    }

    private void dealSyncResponseAndPushResponse(Request request, Response response) {
        if (response.isSuccess()) {
            return;
        }
        dealFailResponse(request, response);
    }

    private void dealOtherResponse(Request request, Response response) {
        if (response.isSuccess()) {
            dealSuccessResponse(request, response);
        } else {
            dealFailResponse(request, response);
        }
    }

    private void dealSuccessResponse(Request request, Response response) {
        request.getInnerListener().onReceive(request, response);
    }

    private void dealFailResponse(Request request, Response response) {
        if (request.isNeedRetry()) {
            if (!request.isOnRetry()) {
                request.getInnerListener().onReceive(request, response);
            }
        } else {
            request.getInnerListener().onReceive(request, response);
        }
    }

    private void dealPushSyncFinResponse(Response response, RequestManager requestManager) {
        LogUtils.d("dealPushSyncFinResponse...");
        PushSyncFinResponseEntity pushSyncFinResponseEntity = (PushSyncFinResponseEntity) response.getResponseEntity();
        List<Request> requests = requestManager.search(Command.PUSH_SYNC_REQUEST);
//        LogUtils.d("dealPushSyncFinResponse  requests=="+requests.size());
        for (Request req : requests) {
            PushSyncRequestEntity pushSyncRequestEntity = (PushSyncRequestEntity) req.getRequestEntity();

//            LogUtils.d("dealPushSyncFinResponse  pushSyncRequestEntity.getAlias()=="+pushSyncRequestEntity.getAlias());
//            LogUtils.d("dealPushSyncFinResponse  pushSyncFinResponseEntity.getAlias()=="+pushSyncFinResponseEntity.getAlias());
//            LogUtils.d("dealPushSyncFinResponse  pushSyncRequestEntity.getSyncKey()=="+pushSyncRequestEntity.getSyncKey());
//            LogUtils.d("dealPushSyncFinResponse  pushSyncFinResponseEntity.getSyncKey()=="+pushSyncFinResponseEntity.getSyncKey());

            if (pushSyncRequestEntity.getAlias().equals(pushSyncFinResponseEntity.getAlias())
                    && pushSyncRequestEntity.getSyncKey() <= pushSyncFinResponseEntity.getSyncKey()) {
                requestManager.remove(req);
            }
        }
    }
}
