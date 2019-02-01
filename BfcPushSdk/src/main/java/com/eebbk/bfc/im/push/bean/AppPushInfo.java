package com.eebbk.bfc.im.push.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class AppPushInfo implements Parcelable{

    private String pkgName;

    private Integer ridTag;

    public AppPushInfo(String pkgName) {
        this.pkgName = pkgName;
    }

    protected AppPushInfo(Parcel in) {
        pkgName = in.readString();
        if (in.readByte() == 0) {
            ridTag = null;
        } else {
            ridTag = in.readInt();
        }
    }

    public static final Creator<AppPushInfo> CREATOR = new Creator<AppPushInfo>() {
        @Override
        public AppPushInfo createFromParcel(Parcel in) {
            return new AppPushInfo(in);
        }

        @Override
        public AppPushInfo[] newArray(int size) {
            return new AppPushInfo[size];
        }
    };

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

 /*   @Override
    public String toString() {
        return "AppPushInfo{" +
                "pkgName='" + pkgName + '\'' +
                ", ridTag=" + ridTag +
                '}';
    }*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pkgName);
        if (ridTag == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(ridTag);
        }
    }
}
