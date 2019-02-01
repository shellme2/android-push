package com.eebbk.bfc.im.push.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class SyncRegistInfo implements Parcelable {

    public static final SyncRegistInfo EMPTY_SYNC_REGISTINFO = new SyncRegistInfo();

    /**
     * 注册id
     */
    private long registId;

    /**
     * 注册token
     */
    private String registToken;

    /**
     * 设备登录状态
     */
    private boolean login;

    public static final Creator<SyncRegistInfo> CREATOR = new Creator<SyncRegistInfo>() {
        @Override
        public SyncRegistInfo createFromParcel(Parcel in) {
            return new SyncRegistInfo(in);
        }

        @Override
        public SyncRegistInfo[] newArray(int size) {
            return new SyncRegistInfo[size];
        }
    };

    public SyncRegistInfo() {

    }

    protected SyncRegistInfo(Parcel in) {
        registId = in.readLong();
        registToken = in.readString();
        login = in.readByte() != 0;
    }

    public long getRegistId() {
        return registId;
    }

    public void setRegistId(long registId) {
        this.registId = registId;
    }

    public String getRegistToken() {
        return registToken;
    }

    public void setRegistToken(String registToken) {
        this.registToken = registToken;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(registId);
        dest.writeString(registToken);
        dest.writeByte((byte) (login ? 1 : 0));
    }

    @Override
    public String toString() {
        return "SyncRegistInfo{" +
                "registId=" + registId +
                ", registToken='" + registToken + '\'' +
                ", login=" + login +
                '}';
    }
}
