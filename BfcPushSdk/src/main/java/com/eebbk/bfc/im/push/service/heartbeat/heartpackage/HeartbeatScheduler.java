package com.eebbk.bfc.im.push.service.heartbeat.heartpackage;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lhd on 2016/9/24.
 */
public abstract class HeartbeatScheduler {

    protected int timeout = 20000;

    public static final int DEFAULT_MAX_HEART = 480;

    /**
     * 最短心跳 1分钟
     */
    protected int minHeart = 60;

    /**
     * 最长心跳 8分钟
     */
    protected int maxHeart = DEFAULT_MAX_HEART;

    protected int step = 30;

    protected volatile boolean started = false;

    protected volatile long heartbeatSuccessTime;

    protected volatile int currentHeartType;

    protected volatile boolean forceAdjust;

    public static final String HEART_TYPE_TAG = "heart_type";

    protected boolean stabledCollected;

    protected AtomicInteger heartbeatTotalCount = new AtomicInteger(0);

    protected AtomicInteger heartbeatTotalSuccessCount = new AtomicInteger(0);

    protected AtomicInteger heartbeatTotalFailedCount = new AtomicInteger(0);

    protected AtomicInteger heartbeatTotalRedundancyCount = new AtomicInteger(0);

    protected AtomicInteger heartbeatTotalAlarmCount = new AtomicInteger(0);

    protected AtomicBoolean sendOne = new AtomicBoolean(false);

    protected long startProbeTime;

    protected long heartbeatProbeDuration;

    public static final int UNKNOWN_HEART = 0, SHORT_HEART = 1, PROBE_HEART = 2, STABLE_HEART = 3, REDUNDANCY_HEART = 4;

    protected PendingIntent createPendingIntent(Context context, int requestCode, int heartType) {
        Intent intent = new Intent();
        intent.setPackage(context.getPackageName());
        intent.setAction(SyncAction.HEARTBEAT_REQUEST);
        intent.putExtra(HEART_TYPE_TAG, heartType);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    protected void set(int minHeart, int maxHeart, int step) {
        this.minHeart = minHeart;
        this.maxHeart = maxHeart;
        this.step = step;
        LogUtils.i("set minMax:" + minHeart + ",maxHeart:" + maxHeart + ",step:" + step);
    }

    protected boolean isStarted() {
        return started;
    }

    protected abstract boolean isStabled();

    protected abstract int getHeartbeatStabledSuccessCount();

    protected void startOne(int currentHeartType) {
        this.currentHeartType = currentHeartType;
        heartbeatTotalCount.incrementAndGet();
        sendOne.set(true);
        if (currentHeartType == REDUNDANCY_HEART) {
            heartbeatTotalRedundancyCount.incrementAndGet();
        } else if (currentHeartType != UNKNOWN_HEART) {
            heartbeatTotalAlarmCount.incrementAndGet();
        }
        LogUtils.i("set current heart type:" + currentHeartType);
    }

    protected void cancelOne() {
        sendOne.set(false);
    }

    protected void setForceAdjust(boolean forceAdjust) {
        this.forceAdjust = forceAdjust;
    }

    protected int getTimeout() {
        return timeout;
    }

    protected void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    protected long getHeartbeatSuccessTime() {
        return heartbeatSuccessTime;
    }

    protected void setHeartbeatSuccessTime(long heartbeatSuccessTime) {
        this.heartbeatSuccessTime = heartbeatSuccessTime;
    }

    public boolean isStabledCollected() {
        return stabledCollected;
    }

    public void setStabledCollected(boolean stabledCollected) {
        this.stabledCollected = stabledCollected;
    }

    public int getHeartbeatTotalSuccessCount() {
        return heartbeatTotalSuccessCount.get();
    }

    public int getHeartbeatTotalCount() {
        return heartbeatTotalCount.get();
    }

    public int getHeartbeatTotalFailedCount() {
        return heartbeatTotalFailedCount.get();
    }

    public int getHeartbeatTotalRedundancyCount() {
        return heartbeatTotalRedundancyCount.get();
    }

    public int getHeartbeatTotalAlarmCount() {
        return heartbeatTotalAlarmCount.get();
    }

    public long getHeartbeatProbeDuration() {
        if (isStabled()) {
            return heartbeatProbeDuration;
        } else {
            return System.currentTimeMillis() - startProbeTime;
        }
    }

    protected abstract void start(Context context);

    protected abstract void stop(Context context);

    protected abstract void clear(Context context);

    protected abstract void handleHeartbeat(Context context, boolean success);

    protected abstract void startNextHeartbeat(Context context, int heartType);

    protected abstract void resetScheduledHeart(Context context);

    protected abstract void receiveHeartbeatFailed(Context context);

    protected abstract void receiveHeartbeatSuccess(Context context);

    protected abstract int getCurHeart();
}
