package com.eebbk.bfc.im.push.service.heartbeat.heartpackage;

import android.content.Context;

import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.util.LogUtils;


/**
 * Created by lhd on 2016/9/14.
 */
public class HeartbeatManager {
    private static final String TAG = "HeartbeatManager";
    private static volatile HeartbeatManager heartbeatManager;

    public static final int PHONE = 0, WATCH = 1;

    private static int policy = WATCH;

    private long lastConnectedDuration;

    private int regularDisconnectedCount;

    private long sendHeartbeatTime;

    private HeartbeatScheduler heartbeatScheduler;

    public static HeartbeatManager getInstance() {
        if (heartbeatManager == null) {
            synchronized (HeartbeatManager.class) {
                if (heartbeatManager == null) {
                    heartbeatManager = new HeartbeatManager();
                }
            }
        }
        return heartbeatManager;
    }

    public static void setPolicy(int policy) {
        HeartbeatManager.policy = policy;
    }

    private HeartbeatManager() {
        if (policy == PHONE) {
            heartbeatScheduler = new DicHearbeatScheduler();
            LogUtils.d("create phone heartbeat scheduler...");
        } else {
            heartbeatScheduler = new DicHearbeatScheduler();
            LogUtils.d("create watch heartbeat scheduler...");
        }
    }

    public void start(Context context) {
        heartbeatScheduler.start(context);
    }

    public boolean isStarted() {
        return heartbeatScheduler.isStarted();
    }

    public void stop(Context context) {
        heartbeatScheduler.stop(context);
    }

    public void set(int minHeart, int maxHeart, int step) {
        heartbeatScheduler.set(minHeart, maxHeart, step);
    }

    public void startNextHeartbeat(Context context, int heartType) {
        heartbeatScheduler.startNextHeartbeat(context, heartType);
    }

    public void resetScheduledHeart(Context context) {
        heartbeatScheduler.resetScheduledHeart(context);
    }

    public void receiveHeartbeatFailed(ConnectionService connectionService) {
        heartbeatScheduler.setForceAdjust(false);
        long connectedDuration = connectionService.getConnectedDuration();
        if (lastConnectedDuration != 0) {
            if (heartbeatScheduler.isStabled()) {
                if (Math.abs(lastConnectedDuration - connectedDuration) <= 2) {
                    regularDisconnectedCount++;
                    LogUtils.e( TAG, "regular disconnected count:" + regularDisconnectedCount);
                    if (regularDisconnectedCount >= 5) {
                        heartbeatScheduler.setForceAdjust(true);
                        LogUtils.e( TAG, "regular disconnected,current heart:" + heartbeatManager.getCurHeart());
                    }
                } else {
                    regularDisconnectedCount = 0;
                }
            }
        }
        lastConnectedDuration = connectedDuration;
        heartbeatScheduler.receiveHeartbeatFailed(connectionService);
    }

    public void receiveHeartbeatSuccess(Context context) {
        heartbeatScheduler.setHeartbeatSuccessTime(System.currentTimeMillis());
        heartbeatScheduler.receiveHeartbeatSuccess(context);
    }

    public boolean isHeartbeatFrequency() {
        long heartbeatPeriod = System.currentTimeMillis() - sendHeartbeatTime;
        if (heartbeatPeriod < heartbeatScheduler.getTimeout()) {
            LogUtils.w("heartbeat is too frequency,just ignore this heartbeat request,sorry!");
            return true;
        }
        return false;
    }

    public void startOne(int currentHeartType) {
        heartbeatScheduler.startOne(currentHeartType);
    }

    public void cancelOne() {
        heartbeatScheduler.cancelOne();
    }

    public void setTimeout(int timeout) {
        heartbeatScheduler.setTimeout(timeout);
    }

    public int getTimeout() {
        return heartbeatScheduler.getTimeout();
    }

    public void clear(Context context) {
        heartbeatScheduler.clear(context);
    }

    public long getHeartbeatSuccessTime() {
        return heartbeatScheduler.getHeartbeatSuccessTime();
    }

    public boolean isStabled() {
        return heartbeatScheduler.isStabled();
    }

    public int getCurHeart() {
        return heartbeatScheduler.getCurHeart();
    }

    public int getHeartbeatStabledSuccessCount() {
        return heartbeatScheduler.getHeartbeatStabledSuccessCount();
    }

    public void setSendHeartbeatTime(long sendHeartbeatTime) {
        this.sendHeartbeatTime = sendHeartbeatTime;
    }

    public boolean isStabledCollected() {
        return heartbeatScheduler.isStabledCollected();
    }

    public void setStabledCollected(boolean stabledCollected) {
        heartbeatScheduler.setStabledCollected(stabledCollected);
    }

    public int getHeartbeatTotalSuccessCount() {
        return heartbeatScheduler.getHeartbeatTotalSuccessCount();
    }

    public int getHeartbeatTotalCount() {
        return heartbeatScheduler.getHeartbeatTotalCount();
    }

    public int getHeartbeatTotalFailedCount() {
        return heartbeatScheduler.getHeartbeatTotalFailedCount();
    }

    public int getHeartbeatTotalRedundancyCount() {
        return heartbeatScheduler.getHeartbeatTotalRedundancyCount();
    }

    public int getHeartbeatTotalAlarmCount() {
        return heartbeatScheduler.getHeartbeatTotalAlarmCount();
    }

    public long getHeartbeatProbeDuration() {
        return heartbeatScheduler.getHeartbeatProbeDuration();
    }
}
