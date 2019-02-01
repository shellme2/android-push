package com.eebbk.bfc.im.push.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/8/22 11:30
 * Email:  zengjingfang@foxmail.com
 */
public class AppBindInfo implements Parcelable {
    private Map<String, AppPushInfo> bindAppMap;

    public AppBindInfo(Map<String, AppPushInfo> bindAppMap) {
        this.bindAppMap = bindAppMap;
    }

    protected AppBindInfo(Parcel in) {
        super();
        this.setBindAppMap(in.readHashMap(AppPushInfo.class.getClassLoader()));
    }

    public static final Creator<AppBindInfo> CREATOR = new Creator<AppBindInfo>() {
        @Override
        public AppBindInfo createFromParcel(Parcel in) {
            return new AppBindInfo(in);
        }

        @Override
        public AppBindInfo[] newArray(int size) {
            return new AppBindInfo[size];
        }
    };

    public Map<String, AppPushInfo> getBindAppMap() {
        return bindAppMap;
    }

    public void setBindAppMap(Map<String, AppPushInfo> bindAppMap) {
        this.bindAppMap = bindAppMap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(bindAppMap);
    }
}
