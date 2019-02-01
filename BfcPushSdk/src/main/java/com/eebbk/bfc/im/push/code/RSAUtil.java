package com.eebbk.bfc.im.push.code;

import android.util.Base64;

import com.eebbk.bfc.im.push.util.LogUtils;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAUtil {

    private static final String KEY_ALGORTHM = "RSA";
    private static final String SIGN_TYPE_RSA = "RSA/ECB/PKCS1Padding"; //和服务器上的RSA加密要一致

    private RSAUtil(){}

    /**
     * 用私钥加密
     *
     * @param data 加密数据
     * @param keyBytes 私钥数据
     * @throws GeneralSecurityException
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] keyBytes) throws GeneralSecurityException {
        // 取私钥
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

        // 对数据加密
        Cipher cipher = Cipher.getInstance(SIGN_TYPE_RSA);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return cipher.doFinal(data);
    }

    /**
     * 用私钥解密
     *
     * @param data 加密数据
     * @param keyBytes 私钥数据
     * @throws GeneralSecurityException
     */
    public static byte[] decryptByPrivateKey(byte[] data, byte[] keyBytes) throws GeneralSecurityException {

        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
                keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        // 对数据解密
        Cipher cipher = Cipher.getInstance(SIGN_TYPE_RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(data);
    }

    /**
     * 用公钥加密
     *
     * @param data  加密数据
     * @param keyBytes 密钥
     * @throws GeneralSecurityException
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] keyBytes) {
        byte[] encryptData = null;
        // 取公钥
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decode(keyBytes, Base64.DEFAULT));
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            // 对数据解密
            Cipher cipher = Cipher.getInstance(SIGN_TYPE_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptData = cipher.doFinal(data);
        } catch (GeneralSecurityException e) {
            LogUtils.e(e);
        }
        return encryptData;
    }

    /**
     * 用公钥解密
     *
     * @param data 加密数据
     * @param keyBytes 密钥
     * @throws GeneralSecurityException
     */
    public static byte[] decryptByPublicKey(byte[] data, byte[] keyBytes) throws GeneralSecurityException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORTHM);
        Key publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

        // 对数据解密
        Cipher cipher = Cipher.getInstance(SIGN_TYPE_RSA);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(data);
    }
}
