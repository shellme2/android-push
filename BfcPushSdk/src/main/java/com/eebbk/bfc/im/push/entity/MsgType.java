package com.eebbk.bfc.im.push.entity;

/**
 * 请求消息类型
 */
public interface MsgType {

    /**
     * 普通文本消息
     */
    int TEXT = 1;

    /**
     * 语音描述消息
     */
    int VOICE_DESC = 2;

    /**
     * 设置类消息
     */
    int SET = 3;

    /**
     * 语音切片消息
     */
    int VOICE_SLICE = 50;
}
