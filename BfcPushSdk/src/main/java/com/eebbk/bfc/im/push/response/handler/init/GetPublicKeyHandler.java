package com.eebbk.bfc.im.push.response.handler.init;

import com.eebbk.bfc.im.push.entity.request.EncryptSetRequestEntity;
import com.eebbk.bfc.im.push.entity.response.PublicKeyResponseEntity;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.handler.SyncHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公钥请求反馈处理
 */
public class GetPublicKeyHandler extends SyncHandler {

    public GetPublicKeyHandler(SyncApplication app) {
        super(app);
    }

    @Override
    public void handle(Request request, Response response) {
        if (request == null) {
            return;
        }

        if (response.isSuccess()) {
            cancelRetry(request.getCommand());
            PublicKeyResponseEntity publicKeyResponseEntity = (PublicKeyResponseEntity) response.getResponseEntity();
            byte[] publicKey = publicKeyResponseEntity.getPublicKey();
            String publicKeyStr = parsePublicKeyString(publicKey);
            app.savePublicKey(publicKeyStr.getBytes());

            LogUtils.d("publickey success");

            setEncrypt(app);
        } else {
            LogUtils.e("publickey error:" + response.getResponseEntity());
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

    private void setEncrypt(SyncApplication app) {
        EncryptSetRequestEntity entity = app.getRequestEntityFactory().createEncryptSetRequestEntity();
        Request request = Request.createRequest(app, entity);
        request.setOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(Request request, Response response) {
                if (response.isSuccess()) {
                    LogUtils.d("set encrypt success");
                } else {
                    LogUtils.e("set encrypt error:" + response.getResponseEntity());
                }
            }
        });
        request.send();
    }

}
