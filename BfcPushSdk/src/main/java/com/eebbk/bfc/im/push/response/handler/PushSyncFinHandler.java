package com.eebbk.bfc.im.push.response.handler;

import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.entity.request.RequestEntityFactory;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncFinResponseEntity;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;

/**
 * 第三方同步响应结束处理
 */
public class PushSyncFinHandler extends SyncHandler {

    public PushSyncFinHandler(SyncApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (!response.isSuccess()) {
            LogUtils.e("response is error:" + response.getResponseEntity());
            return;
        } else {
            if (request != null) {
                LogUtils.d("PushSyncFinHandler request " + request.getCommand());
                cancelRetry(request.getCommand());
            }
        }

        PushSyncFinResponseEntity entity = (PushSyncFinResponseEntity)response.getResponseEntity();
        if (entity == null) {
            LogUtils.e("entity is null.");
            return;
        }

        // 发送响应应答
        sendAck(app, entity.getAlias(), entity.getSyncKey());

//        // 默认直接顺序把未读取完
//        long serverSyncKey = app.getSyncKeyManager().getPushServerSyncKey(entity.getPkgName(), entity.getAlias());
//        if (serverSyncKey > entity.getSyncKey()) {
//            PushSyncInformHandler.sendPushSyncRequest(app, entity.getAlias(), entity.getSyncKey(), null);
//        }
    }

    /**
     * 发送消息响应应答
     */
    public static void sendAck(SyncApplication app, String alias, long syncKey) {
        RequestEntityFactory requestEntityFactory = app.getRequestEntityFactory();
        long registId = app.getmSyncRegistInfo().getRegistId();
        RequestEntity requestEntity = requestEntityFactory.createPushSyncFinAckRequestEntity(alias, syncKey, registId);
        Request ackRequest = Request.createRequest(app, requestEntity);
        ackRequest.setNeedResponse(false);
        ackRequest.send();
    }
}
