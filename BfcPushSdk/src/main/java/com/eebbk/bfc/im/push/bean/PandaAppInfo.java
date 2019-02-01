package com.eebbk.bfc.im.push.bean;

/**
 * Created by Administrator on 2017/10/18.
 */

public class PandaAppInfo {

    public PandaAppInfo(String packageName, int versionCode){
        this.packageName = packageName;
        this.versionCode = versionCode;
    }

    private String packageName;

    private int versionCode;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
}
