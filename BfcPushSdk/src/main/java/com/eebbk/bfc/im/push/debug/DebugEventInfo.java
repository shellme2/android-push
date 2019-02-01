package com.eebbk.bfc.im.push.debug;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/19 18:10
 * Email:  zengjingfang@foxmail.com
 */
public class DebugEventInfo implements Parcelable{

    private long time;

    private String process;

    private int code;

    private String content;

    private String extend;


    public DebugEventInfo(long time, String process, int code, String content, String extend) {
        this.time = time;
        this.process = process;
        this.code = code;
        this.content = content;
        this.extend = extend;
    }

    protected DebugEventInfo(Parcel in) {
        time = in.readLong();
        process = in.readString();
        code = in.readInt();
        content = in.readString();
        extend = in.readString();
    }

    public static final Creator<DebugEventInfo> CREATOR = new Creator<DebugEventInfo>() {
        @Override
        public DebugEventInfo createFromParcel(Parcel in) {
            return new DebugEventInfo(in);
        }

        @Override
        public DebugEventInfo[] newArray(int size) {
            return new DebugEventInfo[size];
        }
    };

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(time);
        dest.writeString(process);
        dest.writeInt(code);
        dest.writeString(content);
        dest.writeString(extend);
    }


}
