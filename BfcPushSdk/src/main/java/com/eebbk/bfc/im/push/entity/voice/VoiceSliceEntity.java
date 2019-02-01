package com.eebbk.bfc.im.push.entity.voice;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Entity;

/**
 * 语音切片实体，用于描述一个语音切片数据
 */
@CommandValue(0)
public class VoiceSliceEntity extends Entity {

    public interface IsFin {
        /**
         * 不是最后一个语音切片
         */
        int NO = 1;

        /**
         * 最后一个语音切片
         */
        int YES = 2;
    }

    /**
     * 组id，客户端生成uuid，去掉中间的横杠。
     */
    @TagValue(10)
    private String groupId;

    /**
     * 语音序号，每个语音从1开始自增计数。
     */
    @TagValue(11)
    private int index;

    /**
     * 语音切片数据。
     */
    @TagValue(12)
    private byte[] voc;

    /**
     * 语音时间长度，单位为ms，只有语音结束切片才包含该字段。
     */
    @TagValue(13)
    private int vocTime;

    /**
     * 是否为改组的最后一个语音切片，1：不是，2：是。
     */
    @TagValue(14)
    private int isFin;

    @TagValue(15)
    private String gdLatitude;

    @TagValue(16)
    private String gdLongitude;

    @TagValue(17)
    private int gdRadius;

    @TagValue(18)
    private String bdLatitude;

    @TagValue(19)
    private String bdLongitude;

    @TagValue(20)
    private int bdRadius;

    @TagValue(21)
    private String ggLatitude;

    @TagValue(22)
    private String ggLongitude;

    @TagValue(23)
    private int ggRadius;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getVoc() {
        return voc;
    }

    public void setVoc(byte[] voc) {
        this.voc = voc;
    }

    public int getVocTime() {
        return vocTime;
    }

    public void setVocTime(int vocTime) {
        this.vocTime = vocTime;
    }

    public int getIsFin() {
        return isFin;
    }

    public void setIsFin(int isFin) {
        this.isFin = isFin;
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

    public void setBdLatiude(String bdLatitude) {
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
}
