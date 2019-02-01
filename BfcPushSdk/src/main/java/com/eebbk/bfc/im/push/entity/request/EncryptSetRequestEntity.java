package com.eebbk.bfc.im.push.entity.request;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;

@CommandValue(Command.ENCRYPT_SET_REQUEST)
public class EncryptSetRequestEntity extends RequestEntity {

    @TagValue(1)
    private int RID;

    @TagValue(10)
    private int encryptType;

    @TagValue(11)
    private byte[] encryptKey;

    @Override
    public int getRID() {
        return RID;
    }

    @Override
    public void setRID(int RID) {
        this.RID = RID;
    }

    public int getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(int encryptType) {
        this.encryptType = encryptType;
    }

    public byte[] getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(byte[] encryptKey) {
        this.encryptKey = encryptKey;
    }
}
