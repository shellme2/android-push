package com.eebbk.bfc.im.push.request;

import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.listener.OnReceiveFinishListener;
import com.eebbk.bfc.im.push.SyncApplication;

public class PushSyncRequest extends Request {

    public PushSyncRequest(SyncApplication app, PushSyncRequestEntity pushSyncRequestEntity, OnReceiveFinishListener onReceiveFinishListener) {
        super(app, pushSyncRequestEntity);
        this.isMutiResponse = true;
        if (onReceiveFinishListener != null) {
            this.onReceiveFinishListener = onReceiveFinishListener;
        }
    }
}
