package com.eebbk.bfc.im.push.service.tcp;

/**
 * 连接进程的环境属性，单例实现
 */
public class ConnectionProcessContext {

    private volatile static ConnectionProcessContext connectionProcessContext;

    /**
     * app密钥，每次个新的连接都会生成一个新的密钥（UUID），连接断开后再次重连上也会生成新的密钥
     */
    private byte[] secretKey;

    /**
     * 公钥
     */
    private byte[] publicKey;

    /**
     * 是否进行加密
     */
    private boolean encrypt;

    public static ConnectionProcessContext getInstance() {
        if (connectionProcessContext == null) {
            synchronized (ConnectionProcessContext.class) {
                if (connectionProcessContext == null) {
                    connectionProcessContext = new ConnectionProcessContext();
                }
            }
        }
        return connectionProcessContext;
    }

    private ConnectionProcessContext() {
        secretKey = new byte[0];
        publicKey = new byte[0];
        encrypt = false;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    @Override
    public String toString() {
        return "ConnectionProcessContext{" +
                "secretKey=" + new String(secretKey) +
                ", publicKey=" + new String(publicKey) +
                ", encrypt=" + encrypt +
                '}';
    }
}
