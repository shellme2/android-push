package com.eebbk.bfc.im.push.code;

import com.eebbk.bfc.im.push.util.LogUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DesEncryptUtil {

//    private static final String TAG=DesEncryptUtil.class.getName();
    private static final String DES_KEY="<Yb0AzXu";

//    static {
//        System.loadLibrary("GetDesKey");
//    }

    private DesEncryptUtil(){}

//    public static native String getDesKey();

    public static String encrypt(String content) {
        String encryptedString = null;
        try {
//            //初始化密钥
//            String encKey = getDesKey();

//            LogUtils.d(TAG,"encKey=="+encKey);

            SecretKeySpec keySpec = new SecretKeySpec(DES_KEY.getBytes("utf-8"), "DES");
            //选择使用 DES 算法，ECB 方式，填充方式为 PKCS5Padding
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            //初始化
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            //获取加密后的字符串
            encryptedString = bytesToHexString(cipher.doFinal(content.getBytes("utf-8")));
        } catch (Exception e) {
            LogUtils.e(e);
        }
        return encryptedString;
    }

    public static String decrypt(String encryptStr) {
        String decryptedString = null;
        try {
//            //初始化密钥
//            String encKey = getDesKey();
            SecretKeySpec keySpec = new SecretKeySpec(DES_KEY.getBytes("utf-8"), "DES");
            //选择使用 DES 算法，ECB 方式，填充方式为 PKCS5Padding
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            //初始化
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            //获取解密后的字符串
            decryptedString = new String(cipher.doFinal(hexStringToByte(encryptStr)));
        } catch (Exception e) {
            LogUtils.e(e);
        }
        return decryptedString;
    }

    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789abcdef".indexOf(c);
        return b;
    }

    /**
     * 把字节数组转换成16进制字符串
     */
    public static final String bytesToHexString(byte[] bArray) {
        if(bArray == null )
        {
            return "";
        }
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
}
