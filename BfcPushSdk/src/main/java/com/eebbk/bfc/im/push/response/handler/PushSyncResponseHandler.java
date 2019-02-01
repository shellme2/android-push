package com.eebbk.bfc.im.push.response.handler;

import android.content.Context;
import android.content.Intent;

import com.eebbk.bfc.im.push.bean.SyncMessage;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncResponseEntity;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.SyncKeyManager;
import com.eebbk.bfc.im.push.util.GsonUtil;

/**
 * 推送同步响应处理
 */
public class PushSyncResponseHandler extends SyncHandler {

    public PushSyncResponseHandler(SyncApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (!response.isSuccess()) {
            LogUtils.e("response is error:" + response.getResponseEntity());
            if (request != null) {
                startRetry(request);
            }
            return;
        } else {
            if (request != null) {
                LogUtils.d("PushSyncResponseHandler request " + response.getCommand());
                cancelRetry(request.getCommand());
            }
        }

        PushSyncResponseEntity entity = (PushSyncResponseEntity)response.getResponseEntity();
        if (entity == null) {
            LogUtils.e("entity is null");
            return;
        }

        // 只要是本地对应的dialogId的syncKey比同步下来的小就更新syncKey
        SyncKeyManager syncKeyManager = app.getSyncKeyManager();
        long localSyncKey = syncKeyManager.getPushLocalSyncKey(entity.getPkgName(), entity.getAlias());
        long syncKey = entity.getSyncKey();
        if (localSyncKey < syncKey) {
            syncKeyManager.putPushLocalSyncKey(entity.getPkgName(), entity.getAlias(), syncKey);
        }

//        // 消息去重处理
//        String msgId = entity.getMsgId();
//        if (app.containsSyncRespMsgId(msgId)) {
//            LogUtils.w("this msg has received and deal,msgId:" + msgId);
//            return;
//        }
//        app.addSyncRespMsgId(msgId);

        // 把消息通知到sdk外部
        sendBroadcast(response);
    }

    private void sendBroadcast(Response response) {
        Context context = app.getContext();
        if (context != null) {
            Intent intent = new Intent();
            intent.setPackage(context.getPackageName());
            intent.setAction(SyncAction.SYNC_RESPONSE_ACTION);
            SyncMessage syncMessage = convertToSyncMessage(response.getResponseEntity());

//            intent.addCategory(syncMessage.getPkgName());

            String data = GsonUtil.toJSON(syncMessage);
            intent.putExtra("data", data);
            LogUtils.i("send data:" + data);
            context.sendBroadcast(intent);
            LogUtils.d("send broadcast successfully, action:" + intent.getAction());
        } else {
            LogUtils.w("context is null,send broadcast fail.");
        }
    }

    private SyncMessage convertToSyncMessage(ResponseEntity responseEntity) {
        PushSyncResponseEntity pushSyncResponseEntity = (PushSyncResponseEntity) responseEntity;
        SyncMessage syncMessage = new SyncMessage();
        syncMessage.setThirdSyncMsg(true);
        syncMessage.setCreateTime(pushSyncResponseEntity.getCreateTime());
        syncMessage.setMsg(pushSyncResponseEntity.getMessage());
        syncMessage.setMsgId(pushSyncResponseEntity.getMsgId());
        syncMessage.setSyncKey(pushSyncResponseEntity.getSyncKey());
        syncMessage.setPkgName(pushSyncResponseEntity.getPkgName());
        syncMessage.setAlias(pushSyncResponseEntity.getAlias());
        return syncMessage;
    }
}
