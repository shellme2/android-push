package com.eebbk.bfc.im.push.response.handler.init;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.entity.request.EncryptSetRequestEntity;
import com.eebbk.bfc.im.push.entity.response.PublicKeyResponseEntity;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公钥请求反馈处理
 */
public class GetPublicKeyHandler extends SyncHandler {
    private static final String TAG = "GetPublicKeyHandler";

    public GetPublicKeyHandler(PushApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request == null) {
            return;
        }

        if (response.isSuccess()) {

            LogUtils.d(TAG, LogTagConfig.LOG_TAG_FLOW_PUBLIC_KEY, "PublicKeyResponseSuccess", "We get the public key from response,the we save the " +
                    "public key,and set encrypt next !!! Response:" + response.getResponseEntity());
            cancelRetry(request.getCommand());
            PublicKeyResponseEntity publicKeyResponseEntity = (PublicKeyResponseEntity) response.getResponseEntity();
            byte[] publicKey = publicKeyResponseEntity.getPublicKey();
            String publicKeyStr = parsePublicKeyString(publicKey);
            app.savePublicKey(publicKeyStr.getBytes());

            LogUtils.d("publickey success");

            setEncrypt(app);
        } else {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_PUBLIC_KEY, "PublicKeyResponseFailed", "Then start retry public key request !!! Response:"+ response.getResponseEntity());
            startRetry(request);
        }
    }

    private String parsePublicKeyString(byte[] publicKey) {
        String publicKeyStr = new String(publicKey);
        String start = "-----BEGIN PUBLIC KEY-----";
        String end = "-----END PUBLIC KEY-----";
        int startIndex = publicKeyStr.indexOf(start) + start.length();
        int endIndex = publicKeyStr.indexOf(end);
        publicKeyStr = publicKeyStr.substring(startIndex, endIndex);

        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(publicKeyStr);
        publicKeyStr = m.replaceAll("");

        return publicKeyStr;
    }

    /**
     * 获取公钥成功之后设置加密的EncryptUtil这个加密方案的的秘钥 通过拿到的公钥加密之后传到服务器
     *
     * @param app
     */
    private void setEncrypt(PushApplication app) {
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_SET_ENCRYPT, "we set encrypt when get public key !!!");
        EncryptSetRequestEntity entity = app.getRequestEntityFactory().createEncryptSetRequestEntity();
        Request request = Request.createRequest(app, entity);
        request.setOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    LogUtils.d("set encrypt success");
                } else {
                    LogUtils.e(TAG, "set encrypt error:" + response.getResponseEntity());
                }
            }
        });
        request.send();
    }

}
