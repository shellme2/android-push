package com.eebbk.bfc.im.push.service.tcp;

public class ConnectionServiceInfo {

    private long createTime;

    private long destroyTime;

    private long recordTime;

    private long duration;

    private String durationFormat;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getDestroyTime() {
        return destroyTime;
    }

    public void setDestroyTime(long destroyTime) {
        this.destroyTime = destroyTime;
    }

    public long getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDurationFormat() {
        return durationFormat;
    }

    public void setDurationFormat(String durationFormat) {
        this.durationFormat = durationFormat;
    }

    @Override
    public String toString() {
        return "ConnectionServiceInfo{" +
                "createTime=" + createTime +
                ", destroyTime=" + destroyTime +
                ", recordTime=" + recordTime +
                ", duration=" + duration +
                ", durationFormat='" + durationFormat + '\'' +
                '}';
    }
}
