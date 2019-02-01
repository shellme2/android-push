package com.eebbk.bfc.im.push.communication;

import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushImplements;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.ResponseCreator;
import com.eebbk.bfc.im.push.response.ResponseDispatcher;
import com.eebbk.bfc.im.push.util.TLVObjectUtil;

/**
 * 推送消息处理service
 */
public class MessageHandleService extends BaseHandleService {

    private static final String TAG = "MessageHandleService";
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

        if(intent==null){
            LogUtils.w("receive intent is empty.");
            return;
        }

        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            LogUtils.e(TAG,"action == null");
            return;
        }

        PushImplements.getPushApplicationSafely(getApplicationContext() ,new OnGetCallBack<PushApplication>() {
            @Override
            public void onGet(PushApplication app) {
                if (action.equals(SyncAction.READ_DATA_ACTION)) {
                    handleMessage(app, intent);
                }
            }
        });
    }

    /**
     * 处理接收到数据的广播
     */
    private void handleMessage(PushApplication app, Intent intent) {
        byte[] data = intent.getByteArrayExtra("data");
        if (data == null || data.length <= 0) {
            LogUtils.w("receive data is empty.");
            return;
        }

        ResponseDispatcher dispatcher = app.getDispatcher();

        ResponseEntity responseEntity = null;
        try {
            responseEntity = TLVObjectUtil.parseResponseEntity(data);
        } catch (Throwable e) {
            LogUtils.e(e);
            Da.record(getApplicationContext(), new DaInfo().setFunctionName(Da.functionName.TLV_OOM)
                    .setTrigValue(e.toString() + " \ndata:" + new String(data)));
        }
        if (responseEntity == null) {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_PUSH_INIT,  "  parse response entity is null.");
            return;
        }

        Response response = ResponseCreator.createResponse(app, responseEntity);
        dispatcher.dispatch(response);
    }
}
