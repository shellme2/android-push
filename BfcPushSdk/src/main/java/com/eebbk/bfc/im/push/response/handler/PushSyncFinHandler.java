package com.eebbk.bfc.im.push.response.handler;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.entity.request.RequestEntityFactory;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncFinResponseEntity;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.request.RequestManager;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.SyncKeyManager;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.List;

/**
 * 第三方同步响应结束处理
 */
public class PushSyncFinHandler extends SyncHandler {
    private static final String TAG = "PushSyncFinHandler";
    private long lastFinishSyncKey;

    public PushSyncFinHandler(PushApplication app) {
        super(app);
    }

    @Override
    public synchronized void handle(Request request, Response response) {
        if (!response.isSuccess()) {
            LogUtils.e(TAG, "response is error:" + response.getResponseEntity());
            return;
        } else {
            if (request != null) {
                LogUtils.d("PushSyncFinHandler request " + request.getCommand());
                cancelRetry(request.getCommand());
            }
        }

        PushSyncFinResponseEntity entity = (PushSyncFinResponseEntity)response.getResponseEntity();
        if (entity == null) {
            LogUtils.e(TAG, "entity is null.");
            return;
        }

        long syncKey = entity.getSyncKey();
        // 发送响应应答
        sendAck(app, entity.getAlias(), syncKey);

        // 默认直接顺序把未读取完,PushSyncInformHandler.java中有个请求队列taskMap，和这个地方可能会导致请求两次
        if (lastFinishSyncKey != syncKey) {
            long serverSyncKey = app.getSyncKeyManager().getPushServerSyncKey(entity.getPkgName(), entity.getAlias());
            if (serverSyncKey > syncKey) {
                SyncKeyManager syncKeyManager = app.getSyncKeyManager();
                long localSyncKey = syncKeyManager.getPushLocalSyncKey(entity.getPkgName(), entity.getAlias());
                LogUtils.d(TAG, "serverSyncKey:" + serverSyncKey + ",syncKey:" + syncKey + ",localSyncKey:" + localSyncKey + ",continue sending sync request");
                PushSyncInformHandler.sendPushSyncRequest(app, entity.getAlias(), localSyncKey, null);
            }
        } else {
            LogUtils.e(TAG, "lastFinishSyncKey:" + lastFinishSyncKey + ",syncKey:" + syncKey + ",do not send sync request");
        }
        lastFinishSyncKey = syncKey;
    }

    /**
     * 发送消息响应应答
     */
    public static void sendAck(PushApplication app, String alias, long syncKey) {
        RequestEntityFactory requestEntityFactory = app.getRequestEntityFactory();
        long registerId = app.getSyncRegisterInfo().getRegisterId();
        RequestEntity requestEntity = requestEntityFactory.createPushSyncFinAckRequestEntity(alias, syncKey, registerId);
        Request ackRequest = Request.createRequest(app, requestEntity);
        ackRequest.setNeedResponse(false);
        ackRequest.send();
    }
}
