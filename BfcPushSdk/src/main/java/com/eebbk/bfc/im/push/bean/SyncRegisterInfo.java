package com.eebbk.bfc.im.push.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class SyncRegisterInfo implements Parcelable {

    public static final SyncRegisterInfo EMPTY_SYNC_REGISTER_INFO = new SyncRegisterInfo();

    /**
     * 注册id
     */
    private long registerId;

    /**
     * 注册token
     */
    private String registerToken;

    /**
     * 设备登录状态
     */
    private boolean login;

    public static final Creator<SyncRegisterInfo> CREATOR = new Creator<SyncRegisterInfo>() {
        @Override
        public SyncRegisterInfo createFromParcel(Parcel in) {
            return new SyncRegisterInfo(in);
        }

        @Override
        public SyncRegisterInfo[] newArray(int size) {
            return new SyncRegisterInfo[size];
        }
    };

    public SyncRegisterInfo() {

    }

    protected SyncRegisterInfo(Parcel in) {
        registerId = in.readLong();
        registerToken = in.readString();
        login = in.readByte() != 0;
    }

    public long getRegisterId() {
        return registerId;
    }

    public void setRegisterId(long registerId) {
        this.registerId = registerId;
    }

    public String getRegisterToken() {
        return registerToken;
    }

    public void setRegisterToken(String registerToken) {
        this.registerToken = registerToken;
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
        dest.writeLong(registerId);
        dest.writeString(registerToken);
        dest.writeByte((byte) (login ? 1 : 0));
    }

    @Override
    public String toString() {
        return "SyncRegisterInfo{" +
                "registerId=" + registerId +
                ", registerToken='" + registerToken + '\'' +
                ", login=" + login +
                '}';
    }
}
