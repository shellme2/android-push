package com.eebbk.bfc.im.push.entity.response;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.entity.Command;

/**
 * 心跳包响应
 */
@CommandValue(Command.HEART_BEAT_RESPONSE)
public class HeartBeatResponseEntity extends ResponseEntity {


    @Override
    public String toString() {
        return "HeartBeatResponseEntity{"+"Command=" + this.getCommand() + "}";
    }
}
