package com.eebbk.bfc.im.push.response;


import android.text.TextUtils;

import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncFinResponseEntity;
import com.eebbk.bfc.im.push.response.handler.init.AliasAndTagHandler;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.PushApplication;
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
import com.eebbk.bfc.im.push.response.handler.init.RegisterHandler;

import java.util.List;

/**
 * 响应派遣器
 */
public class ResponseDispatcher {

    private static final String TAG = "ResponseDispatcher";

    private PushApplication app;

    private ResponseHandlerProxy responseHandlerProxy;


    public ResponseDispatcher(PushApplication app) {
        this.app = app;

        SyncHandler setAliasAndTagSyncHandler=new AliasAndTagHandler(app);
        SyncHandler registerSyncHandler = new RegisterHandler(app);
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
        responseHandlerProxy.register(Command.PUSH_ALIAS_AND_TAG_RESPONSE, setAliasAndTagSyncHandler);
        responseHandlerProxy.register(Command.REGISTER_RESPONSE, registerSyncHandler);
        responseHandlerProxy.register(Command.LOGIN_RESPONSE, loginSyncHandler);
        responseHandlerProxy.register(Command.PUBLIC_KEY_RESPONSE, getPublicKeySyncHandler);
        responseHandlerProxy.register(Command.ENCRYPT_SET_RESPONSE, encryptSetSyncHandler);

        responseHandlerProxy.register(Command.PUSH_SYNC_FIN, pushSyncFinHandler);
        responseHandlerProxy.register(Command.PUSH_SYNC_INFORM, pushSyncInformHandler);
        responseHandlerProxy.register(Command.PUSH_SYNC_RESPONSE, pushSyncResponseHandler);

        responseHandlerProxy.register(Command.TIMEOUT_ERROR_RESPONSE, timeoutErrorHandler);
        responseHandlerProxy.register(Command.SEND_ERROR_RESPONSE, sendErrorHandler);
    }

    public void stop() {
        responseHandlerProxy.stop();
    }

    /**
     * 处理收到的推送数据
     */
    public void dispatch(Response response) {
        if (response.isSuccess()) {
            LogUtils.i(TAG,"dispatch response,entity:" + response.getResponseEntity());
        } else {
            LogUtils.e(TAG," 看看是不是appkey和测试正式环境对应吗？dispatch response,entity:" + response.getResponseEntity());
            if (response.isTimeout()) {
                // 超时请求就立即触发心跳包
                app.heartbeat();
                LogUtils.d(TAG,"timeout request,trigger a heartbeat request to test the connection...");
            }
        }

        RequestManager requestManager = app.getRequestManager();
        Request request = requestManager.find(response.getRID());
        responseHandlerProxy.handle(request, response);
        int respCommand = response.getCommand();
        if (request != null) {
            clearRequest(response, request, requestManager);
            if (respCommand == Command.PUSH_SYNC_RESPONSE) {
                LogUtils.w(TAG,"request is PUSH_SYNC_RESPONSE");
                dealSyncResponseAndPushResponse(request, response);
            } else {
                dealOtherResponse(request, response);
            }
        } else {
            LogUtils.w(TAG,"request is null:" + response.getResponseEntity());
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
        LogUtils.d(TAG,"deal PushSyncFinResponse...");
        PushSyncFinResponseEntity pushSyncFinResponseEntity = (PushSyncFinResponseEntity) response.getResponseEntity();
        List<Request> requests = requestManager.search(Command.PUSH_SYNC_REQUEST);
        for (Request req : requests) {
            PushSyncRequestEntity pushSyncRequestEntity = (PushSyncRequestEntity) req.getRequestEntity();
            if (TextUtils.equals(pushSyncRequestEntity.getAlias(), pushSyncFinResponseEntity.getAlias())
                    && pushSyncRequestEntity.getSyncKey() <= pushSyncFinResponseEntity.getSyncKey()) {
                requestManager.remove(req);
            }
        }
    }
}
