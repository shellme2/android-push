package com.eebbk.bfc.im.push.response.handler;

import android.text.TextUtils;

import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncInformResponseEntity;
import com.eebbk.bfc.im.push.listener.OnReceiveFinishListener;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.request.PushSyncRequest;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.SyncKeyManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * 推送同步通知处理
 */
public class PushSyncInformHandler extends SyncHandler {

    private static String TAG = "PushSyncInformHandler";

    private static final int FOLLOW = 1;

    private static Map<String, Queue<Response>> taskMap = new HashMap<>();

    private static Map<String, Boolean> lockMap = new HashMap<>();

    public PushSyncInformHandler(PushApplication app) {
        super(app);
    }

    @Override
    protected void recycle() {
        taskMap.clear();
        lockMap.clear();
    }

    @Override
    public synchronized void handle(Request request, Response response) {
        final PushSyncInformResponseEntity entity = (PushSyncInformResponseEntity) response.getResponseEntity();
        if (entity == null) {
            LogUtils.e(TAG, "entity is null");
            return;
        }

        SyncKeyManager syncKeyManager = app.getSyncKeyManager();
        long informSyncKey = entity.getSyncKey();
        long serverSyncKey = syncKeyManager.getPushServerSyncKey(entity.getPkgName(), entity.getAlias());
        if (informSyncKey <= serverSyncKey) {
            LogUtils.w(TAG, "the push sync inform has been deal,informSyncKey::serverSyncKey=="
                    + informSyncKey + "::" + serverSyncKey);
            return;
        }
        syncKeyManager.putPushServerSyncKey(entity.getPkgName(), entity.getAlias(), informSyncKey);
        long localSyncKey = syncKeyManager.getPushLocalSyncKey(entity.getPkgName(), entity.getAlias());
        if (informSyncKey <= localSyncKey) {
            LogUtils.w(TAG, "the local syncKey(" + localSyncKey + ") >= response syncKey(" + informSyncKey + ")");
            return;
        }

        List<Request> list = app.getRequestManager().search(Command.PUSH_SYNC_REQUEST);
        LogUtils.i(TAG, "sync inform list size:" + list.size());
        for (Request r : list) {
            PushSyncRequestEntity pushSyncRequestEntity = (PushSyncRequestEntity) r.getRequestEntity();
            if (TextUtils.equals(pushSyncRequestEntity.getAlias(), entity.getAlias())
                    && pushSyncRequestEntity.getSyncKey() >= informSyncKey) {
                LogUtils.w(TAG, "the push sync inform is on deal,syncKey:" + informSyncKey);
                return;
            }
        }

        if (isLock(entity.getAlias())) {
            addTask(entity.getAlias(), response);
            return;
        }

        LogUtils.w(TAG, "PushSyncInform receiver success!!!");

        sendPushSyncRequest(app, entity.getAlias(), localSyncKey, null);
    }

    private void addTask(String alias, Response response) {
        Queue<Response> queue = taskMap.get(alias);
        if (queue == null) {
            queue = new LinkedList<>();
            queue.offer(response);
            taskMap.put(alias, queue);
        } else {
            queue.offer(response);
        }
    }

    private static boolean isLock(String alias) {
        Boolean lock = lockMap.get(alias);
        if (lock == null) {
            return false;
        }
        return lock;
    }

    private static void lock(String alias) {
        lockMap.put(alias, true);
    }

    private static void unLock(String alias) {
        lockMap.put(alias, false);
    }

    private static void scheduleNext(PushApplication app, String alias) {
        Queue<Response> queue = taskMap.get(alias);
        if (queue == null) {
            return;
        }

        Response response;
        if ((response = queue.poll()) != null) {
            PushSyncInformResponseEntity entity = (PushSyncInformResponseEntity) response.getResponseEntity();
            SyncKeyManager syncKeyManager = app.getSyncKeyManager();
            long localSyncKey = syncKeyManager.getPushLocalSyncKey(entity.getPkgName(), entity.getAlias());
            long informSyncKey = entity.getSyncKey();
            if (informSyncKey <= localSyncKey) {
                LogUtils.w(TAG, "scheduleNext the local syncKey(" + localSyncKey + ") >= response syncKey(" + informSyncKey + ")");
                return;
            }
            sendPushSyncRequest(app, entity.getAlias(), localSyncKey, null);
        }
    }

    /**
     * 发出推送同步请求
     */
    public static void sendPushSyncRequest(PushApplication app, String alias, long syncKey, OnReceiveFinishListener listener) {
        sendPushSyncRequest(app, alias, syncKey, FOLLOW, 7, listener);
    }

    public static void sendPushSyncRequest(final PushApplication app, final String alias, long syncKey, int mode, int pageSize, final OnReceiveFinishListener listener) {
        lock(alias);
        long registerId = app.getSyncRegisterInfo().getRegisterId();
        PushSyncRequestEntity pushSyncRequestEntity = app.getRequestEntityFactory().createPushSyncRequestEntity(alias, syncKey, mode, pageSize, registerId);
        PushSyncRequest pushSyncRequest = new PushSyncRequest(app, pushSyncRequestEntity, new OnReceiveFinishListener() {
            @Override
            public void onFinish() {
                unLock(alias);
                scheduleNext(app, alias);
                if (listener != null) {
                    listener.onFinish();
                }
            }
        });
        pushSyncRequest.send();
    }
}
