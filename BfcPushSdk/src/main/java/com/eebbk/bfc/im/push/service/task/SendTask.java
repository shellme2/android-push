package com.eebbk.bfc.im.push.service.task;

import android.content.Intent;

import com.eebbk.bfc.im.push.debug.da.Da;
import com.eebbk.bfc.im.push.debug.da.DaInfo;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.EncryptSetRequestEntity;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.tcp.ConnectionProcessContext;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.service.dispatcher.StatusCode;
import com.eebbk.bfc.im.push.code.RSAUtil;
import com.eebbk.bfc.im.push.util.TLVObjectUtil;

import java.util.Arrays;

public class SendTask extends Task {

    private static final String TAG = "SendTask";

    /**
     * 请求发送的时间
     */
    private long sendTime;

    /**
     * 请求超时时间
     */
    private int timeout;

    private ConnectionService service;

    /**
     * 请求数据
     */
    private RequestEntity requestEntity;

    private Intent intent;

    private InnerSendListener innerSendListener;

    public interface InnerSendListener {
        void onSended(int code);
    }

    public SendTask(ConnectionService service, long sendTime, byte[] data, int timeout) {
        this.service = service;
        this.sendTime = sendTime;
        this.timeout = timeout;
        try {
            if(data==null){
                LogUtils.i(TAG,"requestEntity data is null---");
            }else{
                LogUtils.i(TAG,"data::"+ Arrays.toString(data));
            }

            this.requestEntity = TLVObjectUtil.parseRequestEntity(data);

            if(requestEntity==null){
                LogUtils.i(TAG,"requestEntity is null---");
            }else {
                LogUtils.i(TAG,requestEntity.toString());
            }

            checkRequestEntity(requestEntity);
        } catch (Throwable e) {
            LogUtils.e(TAG,e);
            Da.record(service.getApplicationContext(), new DaInfo().setFunctionName(Da.functionName.TLV_OOM)
                    .setTrigValue(e.toString() + " \ndata:" + new String(data)));
        }
    }

    private void checkRequestEntity(RequestEntity requestEntity) {
        if (requestEntity == null) {
            LogUtils.e(TAG,"checkRequestEntity error:requestEntity is null.");
            return;
        }
        int command = requestEntity.getCommand();
        if (command == Command.ENCRYPT_SET_REQUEST) {
            ConnectionProcessContext connectionProcessContext = ConnectionProcessContext.getInstance();
            EncryptSetRequestEntity encryptSetRequestEntity = (EncryptSetRequestEntity) requestEntity;
            byte[] secretKey = connectionProcessContext.getSecretKey();
            byte[] publicKey = connectionProcessContext.getPublicKey();
            if (secretKey != null && secretKey.length > 0 && publicKey != null && publicKey.length > 0) {
                byte[] encryptKey = RSAUtil.encryptByPublicKey(secretKey, publicKey);
                encryptSetRequestEntity.setEncryptKey(encryptKey);
            } else {
                LogUtils.e(TAG,"secretKey or publicKey is null.");
            }
        }
    }

    public RequestEntity getRequestEntity() {
        return requestEntity;
    }

    public int getCommand() {
        return requestEntity.getCommand();
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - sendTime > timeout;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }
    public void setInnerSendListener(InnerSendListener innerSendListener) {
        this.innerSendListener = innerSendListener;
    }
    private void executeSend() {
        if (requestEntity == null) {
            LogUtils.e(TAG,"requestEntity is null,stop send.");
            return;
        }

        if (isTimeout()) {
            LogUtils.e(TAG,"task is timeout,maybe the task happened long long ago:" + requestEntity);
            Da.record(service.getApplicationContext(), new DaInfo().setFunctionName(Da.functionName.TIME_OUT)
                    .setTrigValue("task is timeout,maybe the task happened long long ago:" + requestEntity));
            return;
        }

        LogUtils.d(TAG,"execute the send task...");
        if (filter()) {
            return;
        }

        int code =  service.send(requestEntity);
        if (code == StatusCode.SEND_SUCCESS) {
            service.handleSendTaskSuccess(this);
            LogUtils.e(TAG,"send data success,data:" + requestEntity);
            Da.record(service.getApplicationContext(), new DaInfo().setFunctionName(Da.functionName.REQUEST)
                    .setTrigValue("success:" + requestEntity.toString()));
        } else {
            LogUtils.e(TAG,"send data fail,code:" + code + ",data:" + requestEntity);
            service.handleSendTaskError(code, this);
            // 这里可以加入重试，当前由于把请求重试做在了上层，所以这里暂时不加重试
            Da.record(service.getApplicationContext(), new DaInfo().setFunctionName(Da.functionName.REQUEST)
                    .setTrigValue("fail:" + requestEntity.toString()));
        }
    }

    /**
     * 这里会导致接收消息效率变低
     */
    private boolean filter() {
        int command = requestEntity.getCommand();

        if (command == Command.PUSH_SYNC_REQUEST) {
            if (service.hasSamePushSyncRequestTask(this)) {
                LogUtils.w(TAG,"the third sync request is executed...");
                return true;
            }
        }

        return false;
    }

    public void startTask() {
        super.startTask();
        executeSend();
    }

    public boolean execute() {
        int command = getCommand();
        if (command == Command.PUBLIC_KEY_REQUEST || command == Command.ENCRYPT_SET_REQUEST
                || command == Command.REGISTER_REQUEST || command == Command.LOGIN_REQUEST
                || command == Command.HEART_BEAT_REQUEST) {
            startTask();
        } else {
            if (isTimeout()) {
                startTask();
            } else if (service.isLogin()) {
                startTask();
                service.getSendTaskExecutor().executeCacheTasks();
            } else {
                // do nothing 因为设备未登录成功，所以不发任何除了初始化类型的消息
            }
        }
        return super.execute();
    }

    @Override
    public void run() {
        if (!execute()) {
            service.getSendTaskExecutor().cacheTask(this);
        }
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }
}
