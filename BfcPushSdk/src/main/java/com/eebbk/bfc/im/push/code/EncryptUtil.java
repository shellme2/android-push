package com.eebbk.bfc.im.push.code;

import com.eebbk.bfc.im.push.util.LogUtils;

import java.io.ByteArrayOutputStream;

public class EncryptUtil {

    private EncryptUtil(){}

    public static byte[] encrypt(byte[] srcData, byte[] secretKey) {
        int srcDataLength = srcData == null ? 0 : srcData.length;
        int secretKeyLength = secretKey == null ? 0 :secretKey.length;
        if (srcDataLength == 0 || secretKeyLength == 0) {
            return srcData;
        }
        LogUtils.i("srcData.length:" + srcDataLength + ",secretKey.length:" + secretKeyLength);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int index = 0;
        for (int i = 0;i < srcDataLength;i++) {
            if (index >= secretKeyLength) {
                index = 0;
            }
            baos.write(srcData[i] ^ secretKey[index]);
            index++;
        }
        return baos.toByteArray();
    }

    public static byte[] decrypt(byte[] encryptData, byte[] secretKey) {
        return encrypt(encryptData, secretKey);
    }
}
