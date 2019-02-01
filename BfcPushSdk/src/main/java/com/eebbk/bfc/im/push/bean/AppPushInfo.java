package com.eebbk.bfc.im.push.bean;

import android.text.TextUtils;

public class AppPushInfo {

    private String pkgName;

    private Integer ridTag;

    public AppPushInfo(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public Integer getRidTag() {
        return ridTag;
    }

    public void setRidTag(Integer ridTag) {
        this.ridTag = ridTag;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AppPushInfo) {
            AppPushInfo appPushInfo = (AppPushInfo) o;
            String pkgName = appPushInfo.getPkgName();
            if (TextUtils.isEmpty(pkgName)) {
                return false;
            }
            if (pkgName.equals(this.pkgName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "AppPushInfo{" +
                "pkgName='" + pkgName + '\'' +
                ", ridTag=" + ridTag +
                '}';
    }
}
