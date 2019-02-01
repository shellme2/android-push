package com.eebbk.bfc.im.push.bean;


import com.eebbk.bfc.im.push.util.JsonUtil;
import com.eebbk.bfc.im.push.util.TimeFormatUtil;

/**
 * Created by lhd on 2017/1/11.
 */

public class HeartbeatStatus {

    private boolean started;

    private boolean stabled;

    private int curHeart;

    private int heartbeatStabledSuccessCount;

    private long heartbeatProbeDuration;

    private String heartbeatProbeDurationFormat;

    private int heartbeatTotalCount;

    private int heartbeatTotalRedundancyCount;

    private int heartbeatTotalAlarmCount;

    private int heartbeatTotalSuccessCount;

    private int heartbeatTotalFailedCount;

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isStabled() {
        return stabled;
    }

    public void setStabled(boolean stabled) {
        this.stabled = stabled;
    }

    public int getCurHeart() {
        return curHeart;
    }

    public void setCurHeart(int curHeart) {
        this.curHeart = curHeart;
    }

    public int getHeartbeatStabledSuccessCount() {
        return heartbeatStabledSuccessCount;
    }

    public void setHeartbeatStabledSuccessCount(int heartbeatStabledSuccessCount) {
        this.heartbeatStabledSuccessCount = heartbeatStabledSuccessCount;
    }

    public long getHeartbeatProbeDuration() {
        return heartbeatProbeDuration;
    }

    public void setHeartbeatProbeDuration(long heartbeatProbeDuration) {
        this.heartbeatProbeDuration = heartbeatProbeDuration;
        this.heartbeatProbeDurationFormat = TimeFormatUtil.format(heartbeatProbeDuration);
    }

    public String getHeartbeatProbeDurationFormat() {
        return heartbeatProbeDurationFormat;
    }

    public void setHeartbeatProbeDurationFormat(String heartbeatProbeDurationFormat) {
        this.heartbeatProbeDurationFormat = heartbeatProbeDurationFormat;
    }

    public int getHeartbeatTotalCount() {
        return heartbeatTotalCount;
    }

    public void setHeartbeatTotalCount(int heartbeatTotalCount) {
        this.heartbeatTotalCount = heartbeatTotalCount;
    }

    public int getHeartbeatTotalRedundancyCount() {
        return heartbeatTotalRedundancyCount;
    }

    public void setHeartbeatTotalRedundancyCount(int heartbeatTotalRedundancyCount) {
        this.heartbeatTotalRedundancyCount = heartbeatTotalRedundancyCount;
    }

    public int getHeartbeatTotalAlarmCount() {
        return heartbeatTotalAlarmCount;
    }

    public void setHeartbeatTotalAlarmCount(int heartbeatTotalAlarmCount) {
        this.heartbeatTotalAlarmCount = heartbeatTotalAlarmCount;
    }

    public int getHeartbeatTotalSuccessCount() {
        return heartbeatTotalSuccessCount;
    }

    public void setHeartbeatTotalSuccessCount(int heartbeatTotalSuccessCount) {
        this.heartbeatTotalSuccessCount = heartbeatTotalSuccessCount;
    }

    public int getHeartbeatTotalFailedCount() {
        return heartbeatTotalFailedCount;
    }

    public void setHeartbeatTotalFailedCount(int heartbeatTotalFailedCount) {
        this.heartbeatTotalFailedCount = heartbeatTotalFailedCount;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
