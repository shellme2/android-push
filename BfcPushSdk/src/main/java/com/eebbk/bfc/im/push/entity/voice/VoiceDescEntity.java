package com.eebbk.bfc.im.push.entity.voice;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Entity;

/**
 * 语音描述实体类
 */
@CommandValue(0)
public class VoiceDescEntity extends Entity {

    /**
     * 语音时长
     */
    @TagValue(10)
    private int vocTime;

    /**
     * 文件存储地址
     */
    @TagValue(11)
    private String storeAddr;

    /**
     * 语音分片组id
     */
    @TagValue(12)
    private String groupId;

    /**
     * 最后一个语音分片切片序号
     */
    @TagValue(13)
    private int lastIndex;

    @TagValue(14)
    private String gdLatitude;

    @TagValue(15)
    private String gdLongitude;

    @TagValue(16)
    private int gdRadius;

    @TagValue(17)
    private String bdLatitude;

    @TagValue(18)
    private String bdLongitude;

    @TagValue(19)
    private int bdRadius;

    @TagValue(20)
    private String ggLatitude;

    @TagValue(21)
    private String ggLongitude;

    @TagValue(22)
    private int ggRadius;

    @TagValue(23)
    private String resoureKey;

    @TagValue(24)
    private long deadline;

    public int getVocTime() {
        return vocTime;
    }

    public void setVocTime(int vocTime) {
        this.vocTime = vocTime;
    }

    public String getStoreAddr() {
        return storeAddr;
    }

    public void setStoreAddr(String storeAddr) {
        this.storeAddr = storeAddr;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    public String getGdLatitude() {
        return gdLatitude;
    }

    public void setGdLatitude(String gdLatitude) {
        this.gdLatitude = gdLatitude;
    }

    public String getGdLongitude() {
        return gdLongitude;
    }

    public void setGdLongitude(String gdLongitude) {
        this.gdLongitude = gdLongitude;
    }

    public int getGdRadius() {
        return gdRadius;
    }

    public void setGdRadius(int gdRadius) {
        this.gdRadius = gdRadius;
    }

    public String getBdLatitude() {
        return bdLatitude;
    }

    public void setBdLatitude(String bdLatitude) {
        this.bdLatitude = bdLatitude;
    }

    public String getBdLongitude() {
        return bdLongitude;
    }

    public void setBdLongitude(String bdLongitude) {
        this.bdLongitude = bdLongitude;
    }

    public int getBdRadius() {
        return bdRadius;
    }

    public void setBdRadius(int bdRadius) {
        this.bdRadius = bdRadius;
    }

    public String getGgLatitude() {
        return ggLatitude;
    }

    public void setGgLatitude(String ggLatitude) {
        this.ggLatitude = ggLatitude;
    }

    public String getGgLongitude() {
        return ggLongitude;
    }

    public void setGgLongitude(String ggLongitude) {
        this.ggLongitude = ggLongitude;
    }

    public int getGgRadius() {
        return ggRadius;
    }

    public void setGgRadius(int ggRadius) {
        this.ggRadius = ggRadius;
    }

    public String getResoureKey() {
        return resoureKey;
    }

    public void setResoureKey(String resoureKey) {
        this.resoureKey = resoureKey;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }
}
