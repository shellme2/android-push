package com.eebbk.bfc.demo.push.debug.sendFactory;

import com.eebbk.bfc.demo.push.PushTestApplication;
import com.eebbk.bfc.demo.push.debug.DebugPushSendTool;
import com.eebbk.bfc.http.BfcHttp;
import com.eebbk.bfc.http.config.BfcRequestConfigure;
import com.eebbk.bfc.http.error.BfcHttpError;
import com.eebbk.bfc.http.toolbox.IBfcErrorListener;
import com.eebbk.bfc.http.toolbox.StringCallBack;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/21 9:59
 * Email:  zengjingfang@foxmail.com
 */
public abstract class PushSendProduct {

    protected long sendTime;


    protected int id;

    protected String url;

    protected String content;

    private boolean isSuccess;

    public abstract Map<String, String>  convertToParams();

    public void httpRequest(String url, Map<String, String> params, final DebugPushSendTool.SendCallBack callBack) {

        sendTime = System.currentTimeMillis();

        BfcHttp.post(PushTestApplication.getAppContext(), url, params, getHttpConfig(params), new StringCallBack() {
            @Override
            public void onResponse(String response) {
                isSuccess = true;
                callBack.onSuccess();
            }
        }, new IBfcErrorListener() {
            @Override
            public void onError(BfcHttpError error) {
                isSuccess = false;
                callBack.onFailed();
            }
        });
    }

    private BfcRequestConfigure getHttpConfig(Map<String, String> params){
        // 失败不重试，测试遇到可能重复发多次，和卢振宇确认，http失败会重试多次，而且失败重试过程中的失败不会回调，出现几率小，先设置不重试排除，反正不重试也不影响测试结果。
        Map<String, String> header = new HashMap<>();
        header.put("headerId", params.get("content"));
        return new BfcRequestConfigure.Builder()
                .setRetryTimes(0)
                .setHeader(header)
                .build();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

}
