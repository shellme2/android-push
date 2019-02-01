package com.eebbk.bfc.im.push.communication;

import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushInterface;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.ResponseCreator;
import com.eebbk.bfc.im.push.response.ResponseDispatcher;
import com.eebbk.bfc.im.push.util.TLVObjectUtil;

/**
 * 推送消息处理service
 */
public class MessageHandleService extends BaseHandleService {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public MessageHandleService() {
        super("MessageHandleService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        super.onHandleIntent(intent);
        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }

        PushInterface.getSyncApplicationSafely(new OnGetCallBack<SyncApplication>() {
            @Override
            public void onGet(SyncApplication app) {
                if (action.equals(SyncAction.READ_DATA_ACTION)) {
                    handleMessage(app, intent);
                }
            }
        });
    }

    /**
     * 处理接收到数据的广播
     * @param app
     * @param intent
     */
    private void handleMessage(SyncApplication app, Intent intent) {
        byte[] data = intent.getByteArrayExtra("data");
        if (data == null || data.length <= 0) {
            LogUtils.w("receive data is empty.");
            return;
        }

        ResponseDispatcher dispatcher = app.getDispatcher();

        ResponseEntity responseEntity = null;
        try {
            responseEntity = TLVObjectUtil.parseResponseEntity(data);
        } catch (Exception e) {
            LogUtils.e(e);
        }
        if (responseEntity == null) {
            LogUtils.e("parse response entity is null.");
            return;
        }

        Response response = ResponseCreator.createResponse(app, responseEntity);
        dispatcher.dispatch(response);
    }
}
