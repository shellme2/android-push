package com.eebbk.bfc.im.push.response;

import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.SyncApplication;

/**
 * 请求响应基类
 */
public class Response {

    /**
     * 响应码定义
     */
    public interface Code {

        /**
         * 响应超时
         */
        int TIME_OUT = 1;

        /**
         * 连接服务报错
         */
        int CONNECTION_SERVICE_ERROR = 2;

        int OFFLINE = 3;

        int LOGOUT = 4;

        int AIDL_REMOTE_SERVICE_ERROR = 5;

        int REQUEST_THREAD_POOL_SHUTDOWN = 6;

        int SEND_ERROR = 7;

        /**
         * 成功
         */
        int SUCCESS = 200;

        int UNKNOW_REGISTID = 3200;

        int REGIST_TOKEN_INVALID = 4200;

        int PUBLICKEY_EXPIRE = 5200;

    }


    private ResponseEntity responseEntity;

    private SyncApplication app;

    public Response(SyncApplication app, ResponseEntity responseEntity) {
        this.app = app;
        this.responseEntity = responseEntity;
    }

    public ResponseEntity getResponseEntity() {
        return responseEntity;
    }

    public void setResponseEntity(ResponseEntity responseEntity) {
        this.responseEntity = responseEntity;
    }

    public int getCommand() {
        return responseEntity.getCommand();
    }

    public int getCode() {
        return responseEntity.getCode();
    }

    public String getDesc() {
        return responseEntity.getDesc();
    }

    public int getRID() {
        return responseEntity.getRID();
    }

    public boolean isSuccess() {
        int command = responseEntity.getCommand();
        if (command == Command.PUSH_SYNC_INFORM
                || command == Command.HEART_BEAT_RESPONSE) {
            return true;
        }
        return responseEntity.getCode() == Code.SUCCESS;
    }

    public boolean isRegistTokenInvalid() {
        int command = responseEntity.getCommand();
        if (command == Command.LOGIN_RESPONSE) {
            return responseEntity.getCode() == Code.REGIST_TOKEN_INVALID;
        }
        return false;
    }

    public boolean isUnknowRegistId() {
        int command = responseEntity.getCommand();
        if (command == Command.LOGIN_RESPONSE) {
            return responseEntity.getCode() == Code.UNKNOW_REGISTID;
        }
        return false;
    }

    /**
     * 公钥过期
     * @return
     */
    public boolean isPublicKeyExpire() {
        int command = responseEntity.getCommand();
        if (command == Command.ENCRYPT_SET_RESPONSE) {
            return responseEntity.getCode() == Code.PUBLICKEY_EXPIRE;
        }
        return false;
    }

    public boolean isTimeout() {
        int command = responseEntity.getCommand();
        if ( command == Command.PUSH_SYNC_INFORM
                || command == Command.HEART_BEAT_RESPONSE) {
            return false;
        }
        return responseEntity.getCode() == Code.TIME_OUT;
    }

}
