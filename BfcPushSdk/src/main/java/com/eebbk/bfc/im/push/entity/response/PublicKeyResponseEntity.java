package com.eebbk.bfc.im.push.entity.response;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;

@CommandValue(Command.PUBLICKEY_RESPONSE)
public class PublicKeyResponseEntity extends ResponseEntity {

    @TagValue(1)
    private int RID;

    @TagValue(2)
    private int code;

    @TagValue(3)
    private String desc;

    @TagValue(10)
    private byte[] publicKey;

    @TagValue(11)
    private byte[] modulus;

    @TagValue(12)
    private byte[] exponent;

    @Override
    public int getRID() {
        return RID;
    }

    @Override
    public void setRID(int RID) {
        this.RID = RID;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getModulus() {
        return modulus;
    }

    public void setModulus(byte[] modulus) {
        this.modulus = modulus;
    }

    public byte[] getExponent() {
        return exponent;
    }

    public void setExponent(byte[] exponent) {
        this.exponent = exponent;
    }
}
