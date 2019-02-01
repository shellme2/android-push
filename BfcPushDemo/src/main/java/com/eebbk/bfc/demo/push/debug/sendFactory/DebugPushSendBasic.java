package com.eebbk.bfc.demo.push.debug.sendFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/20 17:34
 * Email:  zengjingfang@foxmail.com
 */
public class DebugPushSendBasic extends PushSendProduct {


    private String aliases;

    private String appName;


    private String offline;

    public DebugPushSendBasic(int id,String url, String aliases, String appName, String content, String offline) {
        this.id = id;
        this.url = url;
        this.aliases = aliases;
        this.appName = appName;
        this.content = content;
        this.offline = offline;
    }


    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }


    public String getOffline() {
        return offline;
    }

    public void setOffline(String offline) {
        this.offline = offline;
    }

    @Override
    public  Map<String, String> convertToParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("aliases", this.getAliases());
        params.put("content", this.getContent());
        params.put("appName", this.getAppName());
        params.put("offlineUnpush", this.getOffline());
        return params;
    }

}
