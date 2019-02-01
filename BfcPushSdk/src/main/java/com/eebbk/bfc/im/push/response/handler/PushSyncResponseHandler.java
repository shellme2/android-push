package com.eebbk.bfc.im.push.response.handler;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.bean.SyncMessage;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncResponseEntity;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.SyncKeyManager;
import com.eebbk.bfc.im.push.util.JsonUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

/**
 * 推送同步响应处理
 */
public class PushSyncResponseHandler extends SyncHandler {
    private static final String TAG = "PushSyncResponseHandler";

    public PushSyncResponseHandler(PushApplication app) {
        super(app);
    }

    @Override
    public synchronized void handle(Request request, Response response) {
        if (!response.isSuccess()) {
            LogUtils.e(TAG, "response is error:" + response.getResponseEntity());
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

        PushSyncResponseEntity entity = (PushSyncResponseEntity) response.getResponseEntity();
        if (entity == null) {
            LogUtils.e(TAG, "entity is null");
            return;
        }

        // 只要是本地对应的dialogId的syncKey比同步下来的小就更新syncKey
        SyncKeyManager syncKeyManager = app.getSyncKeyManager();
        long localSyncKey = syncKeyManager.getPushLocalSyncKey(entity.getPkgName(), entity.getAlias());
        long syncKey = entity.getSyncKey();
        if (localSyncKey < syncKey) {
            syncKeyManager.putPushLocalSyncKey(entity.getPkgName(), entity.getAlias(), syncKey);
        }

        // 消息去重处理
        String msgId = entity.getMsgId();
        String syncKeyStr = String.valueOf(syncKey);
        if (app.containsSyncRespMsgId(syncKeyStr)) {
            LogUtils.w("this msg has received and deal,msgId:" + msgId + " syncKeyStr:" + syncKeyStr);
            Da.record(app.getContext(), new DaInfo().setFunctionName(Da.functionName.DUPLICATE_MSG)
                    .setTrigValue("this msg has received and deal,msgId:" + msgId + " syncKeyStr:" + syncKeyStr));
            return;
        }
        app.addSyncRespMsgId(syncKeyStr);

        // 把消息通知到sdk外部

        sendBroadcast(response);
    }

    private void sendBroadcast(Response response) {
        PushApplication.getInstance().callBackPushStatus(OnPushStatusListener.Status.RECEIVE, convertToSyncMessage(response.getResponseEntity()));
        Context context = app.getContext();
        if (context != null) {
            Intent intent = new Intent();
            intent.setPackage(context.getPackageName());
            intent.setAction(SyncAction.SYNC_RESPONSE_ACTION);
            SyncMessage syncMessage = convertToSyncMessage(response.getResponseEntity());

            String module = syncMessage.getModule();
            if (TextUtils.isEmpty(module)) {
                intent.addCategory(syncMessage.getPkgName());
            } else {
                intent.addCategory(module);
            }

            String data = JsonUtil.toJson(syncMessage);
            intent.putExtra("data", data);
            LogUtils.i("send syncMessage:" + syncMessage.toString());
            context.sendBroadcast(intent);
            LogUtils.d("send broadcast successfully, action:" + intent.getAction());
            //TODO 同步响应 接收到数据
            LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_SEND_TRIGGER, "SyncTriggerResponse success","Then send broad cast ==>> : data=" + data + "  action=" +
                    intent.getAction());
        } else {
            LogUtils.w("context is null,send broadcast fail.");
        }
    }

    private SyncMessage convertToSyncMessage(ResponseEntity responseEntity) {
        PushSyncResponseEntity pushSyncResponseEntity = (PushSyncResponseEntity) responseEntity;
        SyncMessage syncMessage = new SyncMessage();
        syncMessage.setPushSyncMsg(true);
        syncMessage.setCreateTime(pushSyncResponseEntity.getCreateTime());
        syncMessage.setMsg(pushSyncResponseEntity.getMessage());
        syncMessage.setMsgId(pushSyncResponseEntity.getMsgId());
        syncMessage.setSyncKey(pushSyncResponseEntity.getSyncKey());
        syncMessage.setPkgName(pushSyncResponseEntity.getPkgName());
        syncMessage.setAlias(pushSyncResponseEntity.getAlias());
        syncMessage.setModule(pushSyncResponseEntity.getModule());
        return syncMessage;
    }
}
