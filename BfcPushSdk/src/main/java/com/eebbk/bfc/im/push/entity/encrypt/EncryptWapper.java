package com.eebbk.bfc.im.push.entity.encrypt;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.Entity;
import com.eebbk.bfc.im.push.util.GsonUtil;

/**
 * 加密数据包载体
 */
@CommandValue(Command.ENCRYPT_WAPPER)
public class EncryptWapper extends Entity {

    /**
     * 加密后的数据
     */
    @TagValue(10)
    private byte[] payload;

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return GsonUtil.toJSON(this);
    }
}
