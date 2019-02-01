package com.eebbk.bfc.im.push.request;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.listener.OnReceiveFinishListener;

public class PushSyncRequest extends Request {

    public PushSyncRequest(PushApplication app, PushSyncRequestEntity pushSyncRequestEntity, OnReceiveFinishListener onReceiveFinishListener) {
        super(app, pushSyncRequestEntity);
        this.isMutiResponse = true;
        if (onReceiveFinishListener != null) {
            this.onReceiveFinishListener = onReceiveFinishListener;
        }
    }
}
