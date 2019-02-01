package com.eebbk.bfc.im.push.service.heartbeat.heartpackage;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NetUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lhd on 2016/9/20.
 */
public class DicHearbeatScheduler extends HeartbeatScheduler {
    private static final String TAG = "DicHearbeatScheduler";

    private class Heartbeat {

        AtomicInteger heartbeatStabledSuccessCount = new AtomicInteger(0); // 心跳连续成功次数

        AtomicInteger heartbeatFailedCount = new AtomicInteger(0); // 心跳连续失败次数

        int successHeart;

        int failedHeart;

        /**
         * 默认初始心跳时长4:30钟
         */
        int curHeart = 270;

        AtomicBoolean stabled = new AtomicBoolean(false);

    }

    private int curMaxHeart = maxHeart;

    private int curMinHeart = minHeart;

    private int maxFailedCount = 3;

    private int maxSuccessCount = 10;

    private volatile String networkTag;

    private int requestCode = hashCode();

    private Map<String, Heartbeat> heartbeatMap = new HashMap<>();

    private List<Integer> stabledHeartList = new CopyOnWriteArrayList<>();

    private List<Integer> successHeartList = new CopyOnWriteArrayList<>();

    protected DicHearbeatScheduler() {

    }

    @Override
    protected void start(Context context) {
        started = true;
        startProbeTime = System.currentTimeMillis();
        networkTag = NetUtil.getNetworkTag(context);
        alarm(context);
        LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_HEARTBEAT,"HeartBeatStart","start heartbeat,networkTag:" + networkTag);
    }

    @Override
    protected void stop(Context context) {
        heartbeatSuccessTime = 0;
        started = false;
        currentHeartType = UNKNOWN_HEART;
        Map<String, Heartbeat> tempHeartbeatMap = new HashMap<>(heartbeatMap);
        for (Map.Entry<String, Heartbeat> entry : tempHeartbeatMap.entrySet()) {
            Heartbeat heartbeat = entry.getValue();
            heartbeat.heartbeatStabledSuccessCount.set(0);
        }
        cancel(context);
        LogUtils.d("stop heartbeat...");
    }

    @Override
    protected void set(int minHeart, int maxHeart, int step) {
        super.set(minHeart, maxHeart, step);
        curMaxHeart = maxHeart;
        curMinHeart = minHeart;
    }

    @Override
    protected boolean isStabled() {
        Heartbeat heartbeat = getHeartbeat();
        return heartbeat.stabled.get();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void alarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Heartbeat heartbeat = getHeartbeat();
        boolean stabled = heartbeat.stabled.get();
        int heart;
        if (stabled) {
            heart = heartbeat.curHeart - 10;
            if (heart < minHeart) {
                heart = minHeart;
            }
            heart = heart * 1000;
        } else {
            heart = heartbeat.curHeart * 1000;
        }
        int heartType = stabled ? STABLE_HEART : PROBE_HEART;
        PendingIntent pendingIntent = createPendingIntent(context, requestCode, heartType);
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + heart, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + heart, pendingIntent);
        }
        //TODO 每一次心跳
        LogUtils.e(TAG,LogTagConfig.LOG_TAG_FLOW_HEARTBEAT,"HeartbeatAlarm","PackageName="+context.getPackageName()+"  >>>  start heartbeat,curHeart [" + heartbeat.curHeart + "],heart [" + heart + "],requestCode:" + requestCode + ",stabled:" + stabled);
    }

    private void cancel(Context context) {
        Heartbeat heartbeat = getHeartbeat();
        int heartType = heartbeat.stabled.get() ? STABLE_HEART : PROBE_HEART;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createPendingIntent(context, requestCode, heartType);
        alarmManager.cancel(pendingIntent);
        LogUtils.d(TAG,LogTagConfig.LOG_TAG_FLOW_HEARTBEAT,"HeartbeatCancel","cancel heartbeat,requestCode:" + requestCode);
    }

    @Override
    protected void startNextHeartbeat(Context context, int heartType) {
        LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_HEARTBEAT,"startNextHeartbeat");
        alarm(context);
    }

    @Override
    protected void resetScheduledHeart(Context context) {
        LogUtils.i(TAG,LogTagConfig.LOG_TAG_FLOW_HEARTBEAT,"resetScheduledHeart");
        alarm(context);
    }

    private void addStabledHeart(Integer stabledHeart) {
        if (!stabledHeartList.contains(stabledHeart)) {
            for (Integer period : stabledHeartList) {
                if (period != null && Math.abs(period - stabledHeart) <= 5) {
                    return;
                }
            }
            if (stabledHeartList.size() > 10) {
                stabledHeartList.remove(0);
            }
            stabledHeartList.add(stabledHeart);
            LogUtils.i("add stabledHeart:" + stabledHeart);
        }
        LogUtils.i("stabledHeartList:" + stabledHeartList);
    }

    private void removeStabledHeart(Integer stabledHeart) {
        stabledHeartList.remove(stabledHeart);
        LogUtils.i("stabledHeartList:" + stabledHeartList);
    }

    private void addSuccessHeart(Integer successHeart) {
        if (successHeartList.contains(successHeart)) {
            return;
        }
        final List<Integer> heartList = successHeartList;
        for (Integer period : heartList) {
            if (period != null && Math.abs(period - successHeart) <= 5) {
                return;
            }
        }
        if (successHeartList.size() > 10) {
            successHeartList.remove(0);
        }
        successHeartList.add(successHeart);
        LogUtils.i("add successHeart:" + successHeart);
        LogUtils.i("successHeartList:" + successHeartList);
    }

    private void removeSuccessHeart(Integer successHeart) {
        successHeartList.remove(successHeart);
        LogUtils.i("successHeartList:" + successHeartList);
    }

    @Override
    protected void handleHeartbeat(Context context, boolean success) {
        countHeartbeat(success);

        adjustHeartbeat(success);
    }

    private void countHeartbeat(boolean success) {
        if (success) {
            sendOne.set(false);
            heartbeatTotalSuccessCount.incrementAndGet();
        } else {
            if (sendOne.get()) {
                sendOne.set(false);
                heartbeatTotalFailedCount.incrementAndGet();
            }
        }
    }

    private void adjustHeartbeat(boolean success) {
        if (currentHeartType == REDUNDANCY_HEART) {
            LogUtils.d("redundancy heart,do not adjustHeartbeat...");
            return;
        }
        Heartbeat heartbeat = getHeartbeat();
        if (success) {
            onSuccessHeartbeat(heartbeat);
        } else {
            onFailedHeartbeat(heartbeat);
        }
        LogUtils.i("after success is [" + success +  "] adjusted,heartbeat.curHeart:" + heartbeat.curHeart + ",networkTag:" + networkTag);
    }

    private void setStabled(Heartbeat heartbeat, boolean stabled) {
        boolean same = heartbeat.stabled.get() == stabled;
        if (!same) {
            heartbeat.stabled.set(stabled);
            if (stabled) { // 进入稳定心跳
                heartbeatProbeDuration = System.currentTimeMillis() - startProbeTime;
            } else { // 进入探测心跳(不稳定)
                stabledCollected = false;
                startProbeTime = System.currentTimeMillis();
            }
        }
    }

    private void onSuccessHeartbeat(Heartbeat heartbeat) {
        heartbeat.successHeart = heartbeat.curHeart;
        curMinHeart = heartbeat.curHeart;
        heartbeat.heartbeatFailedCount.set(0);
        addSuccessHeart(heartbeat.successHeart);
        if (heartbeat.stabled.get()) {
            addStabledHeart(heartbeat.successHeart);
            int count = heartbeat.heartbeatStabledSuccessCount.incrementAndGet();
            LogUtils.i("heartbeatStabledSuccessCount:" + heartbeat.heartbeatStabledSuccessCount.get());
            if (count >= maxSuccessCount) {
                maxSuccessCount += 10;
                LogUtils.i("maxSuccessCount:" + maxSuccessCount);
                Integer stabledHeart = selectMinSuccessHeart(stabledHeartList, heartbeat.curHeart);
                if (stabledHeart != null) {
                    heartbeat.curHeart = stabledHeart;
                } else {
                    setStabled(heartbeat, false);
                    curMaxHeart = maxHeart;
                    heartbeat.curHeart = (curMinHeart + curMaxHeart) / 2;
                    LogUtils.i("curHeart = (" + curMinHeart + " + " + curMaxHeart + ") / 2 = " + heartbeat.curHeart);
                }
            }
        } else {
            if (curMinHeart > curMaxHeart) { // 针对切网情况有可能出现curMinHeart > curMaxHeart
                curMaxHeart = maxHeart;
            }
            heartbeat.curHeart = (curMinHeart + curMaxHeart) / 2;
            LogUtils.i("curHeart = (" + curMinHeart + " + " + curMaxHeart + ") / 2 = " + heartbeat.curHeart);
        }

        if (heartbeat.curHeart >= maxHeart) {
            heartbeat.curHeart = maxHeart;
            setStabled(heartbeat, true);
            LogUtils.i("探测达到最大心跳adjust stabled:" + heartbeat.stabled.get());
        } else if (curMaxHeart - curMinHeart < 10) {
            if (!heartbeat.stabled.get()) {
                heartbeat.curHeart = curMinHeart;
            }
            setStabled(heartbeat, true);
            LogUtils.i("二分法探测尽头adjust stabled:" + heartbeat.stabled.get());
        }
        LogUtils.i("curHeart:" + heartbeat.curHeart + ",curMinHeart:" + curMinHeart + ",curMaxHeart:" + curMaxHeart);
    }

    private void onFailedHeartbeat(Heartbeat heartbeat) {
        removeSuccessHeart(heartbeat.curHeart);
        heartbeat.failedHeart = heartbeat.curHeart;
        heartbeat.heartbeatStabledSuccessCount.set(0);
        int count = heartbeat.heartbeatFailedCount.incrementAndGet();
        LogUtils.i("heartbeatFailedCount:" + count);
        if (maxSuccessCount > 10) {
            maxSuccessCount -= 10;
        }
        if (count >= maxFailedCount) {
            curMaxHeart = heartbeat.curHeart;
            if (curMinHeart > curMaxHeart) {
                curMinHeart = minHeart;
            }
            removeStabledHeart(heartbeat.curHeart);
        }
        if (heartbeat.stabled.get()) {
            if (count >= maxFailedCount || forceAdjust) {
                Integer stabledHeart = selectMaxSuccessHeart(stabledHeartList, heartbeat.curHeart);
                if (stabledHeart != null) {
                    heartbeat.curHeart = stabledHeart;
                } else {
                    setStabled(heartbeat, false);
                    curMinHeart = minHeart;
                    heartbeat.curHeart = (curMinHeart + curMaxHeart) / 2;
                    LogUtils.i("curHeart = (" + curMaxHeart + " + " + curMinHeart + ") / 2 = " + heartbeat.curHeart);
                }
            } else {
                LogUtils.i("continue retry heartbeat.curHeart:" + heartbeat.curHeart + ",stabled:" + heartbeat.stabled.get());
            }
        } else {
            if (count > maxFailedCount || forceAdjust) {
                heartbeat.curHeart = (curMinHeart + curMaxHeart) / 2;
                LogUtils.i("curHeart = (" + curMaxHeart + " + " + curMinHeart + ") / 2 = " + heartbeat.curHeart);
            }
        }
        if (curMaxHeart - curMinHeart < 10) {
            if (!heartbeat.stabled.get()) {
                curMinHeart = minHeart;
            }
            LogUtils.i("二分法探测达到瓶颈" + ",curHeart:" + heartbeat.curHeart);
            LogUtils.i("curMinHeart:" + curMinHeart + ",curMaxHeart:" + curMaxHeart);
        }
        LogUtils.i("curHeart:" + heartbeat.curHeart + ",curMinHeart:" + curMinHeart + ",curMaxHeart:" + curMaxHeart);
    }

    private Integer selectMaxSuccessHeart(List<Integer> list, int curHeart) {
        // CopyOnWriteArrayList 好像不支持Collections.sort
        List<Integer> temp = new ArrayList<>(list);
        Collections.sort(temp, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs.compareTo(lhs);
            }
        });
        LogUtils.i("list:" + temp);
        for (Integer heart : temp) {
            if (curHeart >= heart) {
                continue;
            } else {
                return heart;
            }
        }
        return null;
    }

    private Integer selectMinSuccessHeart(List<Integer> list, int curHeart) {
        // CopyOnWriteArrayList 好像不支持Collections.sort
        List<Integer> temp = new ArrayList<>(list);
        Collections.sort(temp, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs.compareTo(rhs);
            }
        });
        LogUtils.i("list:" + temp);
        for (Integer heart : temp) {
            if (curHeart >= heart) {
                continue;
            } else {
                return heart;
            }
        }
        return null;
    }

    private Heartbeat getHeartbeat() {
        Heartbeat heartbeat = heartbeatMap.get(networkTag);
        if (heartbeat == null) {
            heartbeat = new Heartbeat();
            heartbeatMap.put(networkTag, heartbeat);
        }
        return heartbeat;
    }

    @Override
    protected void receiveHeartbeatFailed(Context context) {
        handleHeartbeat(context, false);
    }

    @Override
    protected void receiveHeartbeatSuccess(Context context) {
        handleHeartbeat(context, true);
        alarm(context);
    }

    @Override
    protected int getHeartbeatStabledSuccessCount() {
        Heartbeat heartbeat = getHeartbeat();
        int count;
        if (heartbeat.stabled.get()) {
            count = heartbeat.heartbeatStabledSuccessCount.get();
        } else {
            count = 0;
        }
        return count;
    }

    @Override
    protected void clear(Context context) {
        stop(context);
        heartbeatMap.clear();
        stabledHeartList.clear();
        curMinHeart = minHeart;
        curMaxHeart = maxHeart;
        networkTag = null;
        LogUtils.d("clear heartbeat...");
    }

    @Override
    protected int getCurHeart() {
        Heartbeat heartbeat = getHeartbeat();
        return heartbeat.curHeart;
    }
}
