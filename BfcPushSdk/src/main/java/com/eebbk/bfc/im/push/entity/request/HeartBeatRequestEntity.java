package com.eebbk.bfc.im.push.entity.request;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.entity.Command;

/**
 * 心跳包请求
 */
@CommandValue(Command.HEART_BEAT_REQUEST)
public class HeartBeatRequestEntity extends RequestEntity {


    @Override
    public String toString() {
        return "HeartBeatRequestEntity{"+"Command=" + this.getCommand() + "}";
    }
}
