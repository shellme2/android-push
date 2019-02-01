package com.eebbk.bfc.im.push.entity;

import com.eebbk.bfc.im.push.entity.encrypt.EncryptWapper;
import com.eebbk.bfc.im.push.entity.request.EncryptSetRequestEntity;
import com.eebbk.bfc.im.push.entity.request.HeartBeatRequestEntity;
import com.eebbk.bfc.im.push.entity.request.LoginRequestEntity;
import com.eebbk.bfc.im.push.entity.request.PublicKeyRequestEntity;
import com.eebbk.bfc.im.push.entity.request.RegistRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncFinAckRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncTriggerRequestEntity;
import com.eebbk.bfc.im.push.entity.response.EncryptSetResponseEntity;
import com.eebbk.bfc.im.push.entity.response.HeartBeatResponseEntity;
import com.eebbk.bfc.im.push.entity.response.LoginResponseEntity;
import com.eebbk.bfc.im.push.entity.response.PublicKeyResponseEntity;
import com.eebbk.bfc.im.push.entity.response.RegistResponseEntity;
import com.eebbk.bfc.im.push.entity.response.SendErrorResponseEntity;
import com.eebbk.bfc.im.push.entity.response.TimeoutErrorResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.AliasAndTagsResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncFinResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncInformResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncTriggerResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * 维护request和response的关系，简称RRR
 */
public final class ReqRespRelationship {

    public static final Map<Integer,Class<?>> COMMAND_CLASS_MAP = new HashMap<>();

    public static final Map<Integer, Integer> REQUEST_RESPONSE_COMMAND_MAP = new HashMap<>();

    /**
     * 把所有协议请求和响应实体以及他们对应的Command都一一放进这个map以便今后进行遍历转换
     * 哥们智商低下，已经想不到第二种方式来避免敲这么一大段代码
     */
    static {
        // 设备注册
        COMMAND_CLASS_MAP.put(Command.REGIST_REQUEST, RegistRequestEntity.class);
        COMMAND_CLASS_MAP.put(Command.REGIST_RESPONSE, RegistResponseEntity.class);
        REQUEST_RESPONSE_COMMAND_MAP.put(Command.REGIST_REQUEST, Command.REGIST_RESPONSE);

        // 设备登录
        COMMAND_CLASS_MAP.put(Command.LOGIN_REQUEST, LoginRequestEntity.class);
        COMMAND_CLASS_MAP.put(Command.LOGIN_RESPONSE, LoginResponseEntity.class);
        REQUEST_RESPONSE_COMMAND_MAP.put(Command.LOGIN_REQUEST, Command.LOGIN_RESPONSE);

        // 心跳
        COMMAND_CLASS_MAP.put(Command.HEART_BEAT_REQUEST, HeartBeatRequestEntity.class);
        COMMAND_CLASS_MAP.put(Command.HEART_BEAT_RESPONSE, HeartBeatResponseEntity.class);
        REQUEST_RESPONSE_COMMAND_MAP.put(Command.HEART_BEAT_REQUEST, Command.HEART_BEAT_RESPONSE);

        // 获取公钥
        COMMAND_CLASS_MAP.put(Command.PUBLICKEY_REQUEST, PublicKeyRequestEntity.class);
        COMMAND_CLASS_MAP.put(Command.PUBLICKEY_RESPONSE, PublicKeyResponseEntity.class);
        REQUEST_RESPONSE_COMMAND_MAP.put(Command.PUBLICKEY_REQUEST, Command.PUBLICKEY_RESPONSE);

        // 加密实体
        COMMAND_CLASS_MAP.put(Command.ENCRYPT_WAPPER, EncryptWapper.class);

        // 加密设置
        COMMAND_CLASS_MAP.put(Command.ENCRYPT_SET_REQUEST, EncryptSetRequestEntity.class);
        COMMAND_CLASS_MAP.put(Command.ENCRYPT_SET_RESPONSE, EncryptSetResponseEntity.class);
        REQUEST_RESPONSE_COMMAND_MAP.put(Command.ENCRYPT_SET_REQUEST, Command.ENCRYPT_SET_RESPONSE);

        /**
         * 第三方推送协议包
         */
        // 别名标签设置
        COMMAND_CLASS_MAP.put(Command.PUSH_ALIAS_AND_TAG_REQUEST, AliasAndTagsRequestEntity.class);
        COMMAND_CLASS_MAP.put(Command.PUSH_ALIAS_AND_TAG_RESPONSE, AliasAndTagsResponseEntity.class);
        REQUEST_RESPONSE_COMMAND_MAP.put(Command.PUSH_ALIAS_AND_TAG_REQUEST, Command.PUSH_ALIAS_AND_TAG_RESPONSE);

        // 第三方同步请求
        COMMAND_CLASS_MAP.put(Command.PUSH_SYNC_REQUEST, PushSyncRequestEntity.class);
        COMMAND_CLASS_MAP.put(Command.PUSH_SYNC_RESPONSE, PushSyncResponseEntity.class);
        REQUEST_RESPONSE_COMMAND_MAP.put(Command.PUSH_SYNC_REQUEST, Command.PUSH_SYNC_RESPONSE);

        // 第三方同步触发
        COMMAND_CLASS_MAP.put(Command.PUSH_SYNC_TRIGGER_REQUEST, PushSyncTriggerRequestEntity.class);
        COMMAND_CLASS_MAP.put(Command.PUSH_SYNC_TRIGGER_RESPONSE, PushSyncTriggerResponseEntity.class);
        REQUEST_RESPONSE_COMMAND_MAP.put(Command.PUSH_SYNC_TRIGGER_REQUEST, Command.PUSH_SYNC_TRIGGER_RESPONSE);

        // 第三方同步响应完成应答
        COMMAND_CLASS_MAP.put(Command.PUSH_SYNC_FIN_ACK, PushSyncFinAckRequestEntity.class);

        // 第三方同步响应完成
        COMMAND_CLASS_MAP.put(Command.PUSH_SYNC_FIN, PushSyncFinResponseEntity.class);

        // 第三方同步通知
        COMMAND_CLASS_MAP.put(Command.PUSH_SYNC_INFORM, PushSyncInformResponseEntity.class);

        /**
         * 本地错误
         */
        COMMAND_CLASS_MAP.put(Command.TIMEOUT_ERROE_RESPONSE, TimeoutErrorResponseEntity.class);
        COMMAND_CLASS_MAP.put(Command.SEND_ERROR_RESPONSE, SendErrorResponseEntity.class);
    }
}
