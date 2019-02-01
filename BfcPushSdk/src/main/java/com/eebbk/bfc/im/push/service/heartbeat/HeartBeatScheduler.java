package com.eebbk.bfc.im.push.service.heartbeat;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.util.NetUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 心跳包控制,微信智能心跳方案实现
 */
public class HeartBeatScheduler {

    private static class HeartbeatInfo {
        /**
         * 成功心跳,初始值为最小心跳值
         */
        volatile int successHeart = minHeart;

        /**
         * 当前已经发送成功的心跳周期的失败次数
         */
        AtomicInteger successHeartFailedCount = new AtomicInteger(0);

        /**
         * 当前心跳
         */
        volatile int curHeart = successHeart; // 当前的心跳

        AtomicInteger curHeartFailCount = new AtomicInteger(0); // 当前心跳的失败次数

        AtomicInteger stableHeartSuccessCount = new AtomicInteger(0);

        /**
         * 是否是探测心跳
         */
        volatile boolean isProbeHeartbeat;

        /**
         * 是否是稳定心跳
         */
        volatile boolean isStableHeartbeat;

        /**
         * 短心跳成功次数
         */
        AtomicInteger shortHeartbeatSuccessCount = new AtomicInteger(0);

        /**
         * 是否是短心跳
         */
        volatile boolean isShortHeartbeat;

        /**
         * 是否是冗余心跳
         */
        volatile boolean isRedundancyHeartbeat;

    }

    private static volatile boolean isStarted;

    /**
     * 短心跳成功次数，虽然限制两次，但是还是会发送三次短心跳
     */
    private static final int maxShortHeartSuccessCount = 2;

    /**
     * 当前心跳的最大失败次数如果超过3次即可认为已经找到最大心跳区间，最大心跳很有可能就是比当前心跳略小一点的时间周期
     */
    private static final int maxCurHeartFailCount = 3;

    /**
     * 如果稳定的成功心跳失败次数超过3次就任务这个成功心跳周期值已经失效，需要重新计算
     */
    private static final int maxSuccessHeartFailCount = 3;

    /**
     * 稳定心跳成功执行的最大次数，用来继续探测更长心跳周期
     */
    public static final int maxStableHeartSuccessCount = 30;

    /**
     * 心跳最小值，60秒
     */
    private static volatile int minHeart = 60 * 1000; // 60秒

    /**
     * 心跳最大值，4分钟
     */
    private static volatile int maxHeart = 5 * 60 * 1000; // 5分钟

    /**
     * 心跳探测步长
     */
    private static volatile int probeHeartStep = 30 * 1000; // 心跳增加步长

    private static volatile int stableHeartStep = probeHeartStep; // 稳定期后的探测步长

    private static int shortHeartbeat = 30 * 1000; // 30秒短心跳

    public static int TIMEOUT = 20000;

    private static volatile long heartbeatSuccessTime;

    private static HashMap<String, HeartbeatInfo> heartBeatInfoMap = new HashMap<>();

    private static String networkTag;

    public static int getMaxShortHeartSuccessCount() {
        return maxShortHeartSuccessCount;
    }

    public static int getShortHeartbeatSuccessCount() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return 0;
        }
        return heartbeatInfo.shortHeartbeatSuccessCount.get();
    }

    public static void setHeartbeatPeriod(int minHeart, int maxHeart, int heartStep) {
        HeartBeatScheduler.minHeart = minHeart;
        HeartBeatScheduler.maxHeart = maxHeart;
        HeartBeatScheduler.probeHeartStep = heartStep;
        HeartBeatScheduler.stableHeartStep = heartStep;
        LogUtils.i("set heartbeat period,minHeart:" + minHeart + ",maxHeart:" + maxHeart + ",probeHeartStep:" + heartStep);
    }

    public static void setHeartbeatSuccessTime(long time) {
        heartbeatSuccessTime = time;
    }

    public static long getHeartbeatSuccessTime() {
        return heartbeatSuccessTime;
    }

    /**
     * 探测心跳连续失败5次就结束
     *
     * @return
     */
    public static boolean isFinishProbeHeartbeat() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return false;
        }
        boolean isFinish = heartbeatInfo.curHeartFailCount.get() >= maxCurHeartFailCount;
        if (isFinish) {
            LogUtils.i("finish the probe heartbeat,successHeart [" + heartbeatInfo.successHeart +"],curHeart [" + heartbeatInfo.curHeart + "]");
        }
        return isFinish;
    }

    /**
     * 短心跳连续成功3次就结束
     *
     * @return
     */
    public static boolean isFinishShortHeartbeat() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return false;
        }
        return heartbeatInfo.shortHeartbeatSuccessCount.get() >= maxShortHeartSuccessCount;
    }

    public static int increaseShortHeartbeatSuccessCount() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return 0;
        }
        int count = heartbeatInfo.shortHeartbeatSuccessCount.incrementAndGet();
        LogUtils.i("increase shortHeartbeatSuccessCount:" + count);
        return count;
    }

    public static void resetHeartbeatInfo() {
        heartbeatSuccessTime = 0;
        LogUtils.i("reset heartbeatSuccessTime:" + heartbeatSuccessTime);
        for (Map.Entry<String, HeartbeatInfo> entry : heartBeatInfoMap.entrySet()) {
            HeartbeatInfo heartbeatInfo = entry.getValue();
            if (heartbeatInfo != null) {
                heartbeatInfo.shortHeartbeatSuccessCount.set(0);
                LogUtils.i(entry.getKey() + " reset shortHeartbeatSuccessCount:" + heartbeatInfo.shortHeartbeatSuccessCount.get());
                heartbeatInfo.stableHeartSuccessCount.set(0);
                LogUtils.i(entry.getKey() + " reset stableHeartSuccessCount:" + heartbeatInfo.stableHeartSuccessCount.get());
            }
        }
    }

    public static boolean isShortHeartbeat() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return false;
        }
        return heartbeatInfo.isShortHeartbeat;
    }

    public static boolean isProbeHeartbeat() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return false;
        }
        return heartbeatInfo.isProbeHeartbeat;
    }

    public static boolean isStableHeartbeat() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return false;
        }
        return heartbeatInfo.isStableHeartbeat;
    }

    public static boolean isRedundancyHeartbeat() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return false;
        }
        return heartbeatInfo.isRedundancyHeartbeat;
    }

    public static void setIsRedundancyHeartbeat(boolean isRedundancyHeartbeat) {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            heartbeatInfo = new HeartbeatInfo();
            heartBeatInfoMap.put(networkTag, heartbeatInfo);
        }
        heartbeatInfo.isRedundancyHeartbeat = isRedundancyHeartbeat;
    }

    /**
     * 开启短心跳
     *
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void startShortHeartbeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createPendingIntent(context, 666);
        alarmManager.cancel(pendingIntent);

        int sdk = Build.VERSION.SDK_INT;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager.isScreenOn()) {
            if (sdk >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + shortHeartbeat, pendingIntent);
                LogUtils.d("use setExact method...");
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + shortHeartbeat, pendingIntent);
                LogUtils.d("use set method...");
            }
        } else {
            /*if (sdk >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + shortHeartbeat, pendingIntent);
                LogUtils.d("use setExactAndAllowWhileIdle alarm method...");
            } else */if (sdk >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + shortHeartbeat, pendingIntent);
                LogUtils.d("use setExact alarm method...");
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + shortHeartbeat, pendingIntent);
                LogUtils.d("use set alarm method...");
            }
        }
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            heartbeatInfo = new HeartbeatInfo();
            heartBeatInfoMap.put(networkTag, heartbeatInfo);
        }
        heartbeatInfo.isShortHeartbeat = true;
        LogUtils.i("start short heartbeat,shortHeartbeat [" + shortHeartbeat + "]");
    }

    public static void cancelShortHeartbeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createPendingIntent(context, 666);
        alarmManager.cancel(pendingIntent);
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            heartbeatInfo = new HeartbeatInfo();
            heartBeatInfoMap.put(networkTag, heartbeatInfo);
        }
        heartbeatInfo.isShortHeartbeat = false;
        LogUtils.d("cancel short heartbeat...");
    }

    /**
     * 开启探测心跳
     *
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void startProbeHeartbeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createPendingIntent(context, 667);
        alarmManager.cancel(pendingIntent);

        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            heartbeatInfo = new HeartbeatInfo();
            heartBeatInfoMap.put(networkTag, heartbeatInfo);
        }
        int sdk = Build.VERSION.SDK_INT;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager.isScreenOn()) {
            if (sdk >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + heartbeatInfo.curHeart, pendingIntent);
                LogUtils.d("use setExact method...");
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + heartbeatInfo.curHeart, pendingIntent);
                LogUtils.d("use set method...");
            }
        } else {
            /*if (sdk >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + curHeart, pendingIntent);
                LogUtils.d("use setExactAndAllowWhileIdle alarm method...");
            } else */if (sdk >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + heartbeatInfo.curHeart, pendingIntent);
                LogUtils.d("use setExact alarm method...");
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + heartbeatInfo.curHeart, pendingIntent);
                LogUtils.d("use set alarm method...");
            }
        }
        heartbeatInfo.isProbeHeartbeat = true;
        LogUtils.i("start probe heartbeat,curHeart [" + heartbeatInfo.curHeart + "]");
    }

    public static void cancelProbeHeartbeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createPendingIntent(context, 667);
        alarmManager.cancel(pendingIntent);
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            heartbeatInfo = new HeartbeatInfo();
            heartBeatInfoMap.put(networkTag, heartbeatInfo);
        } else {
            heartbeatInfo.isProbeHeartbeat = false;
        }
        LogUtils.d("cancel probe heartbeat...");
    }

    /**
     * 开启稳定心跳
     *
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void startStableHeartbeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createPendingIntent(context, 668);

        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            heartbeatInfo = new HeartbeatInfo();
            heartBeatInfoMap.put(networkTag, heartbeatInfo);
        }
        int period = heartbeatInfo.successHeart;
        if (period < shortHeartbeat) {
            period = shortHeartbeat;
        }
        int sdk = Build.VERSION.SDK_INT;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager.isScreenOn()) {
            if (sdk >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + period, pendingIntent);
                LogUtils.d("use setExact method...");
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + period, pendingIntent);
                LogUtils.d("use set method...");
            }
        } else {
            /*if (sdk >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + period, pendingIntent);
                LogUtils.d("use setExactAndAllowWhileIdle alarm method...");
            } else */if (sdk >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + period, pendingIntent);
                LogUtils.d("use setExact alarm method...");
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + period, pendingIntent);
                LogUtils.d("use set alarm method...");
            }
        }
        heartbeatInfo.isStableHeartbeat = true;
        LogUtils.i("start stable heartbeat,successHeart [" + heartbeatInfo.successHeart + "],period [" + period + "]");
    }

    public static void cancelStableHeartbeat(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = createPendingIntent(context, 668);
        alarmManager.cancel(pendingIntent);
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            heartbeatInfo = new HeartbeatInfo();
            heartBeatInfoMap.put(networkTag, heartbeatInfo);
        } else {
            heartbeatInfo.isStableHeartbeat = false;
            heartbeatInfo.stableHeartSuccessCount.set(0);
        }
        LogUtils.d("cancel stable heartbeat...");
    }

    private static PendingIntent createPendingIntent(Context context, int requestCode) {
        Intent intent = new Intent();
        intent.setPackage(context.getPackageName());
        intent.setAction(SyncAction.HEARTBEAT_REQUEST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public static void probeHeartbeat(boolean isSuccess) {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            heartbeatInfo = new HeartbeatInfo();
            heartBeatInfoMap.put(networkTag, heartbeatInfo);
        }
        if (isSuccess) {
            LogUtils.d("the curHeart [" + heartbeatInfo.curHeart + "] is successful...");
            heartbeatInfo.successHeart = heartbeatInfo.curHeart;
            heartbeatInfo.curHeart += probeHeartStep;
            heartbeatInfo.curHeartFailCount.set(0);
            LogUtils.i("adjust curHeart [" + heartbeatInfo.curHeart + "],successHeart [" + heartbeatInfo.successHeart
                    + "],probeHeartStep [" + probeHeartStep + "] and reset curHeartFailCount:" + heartbeatInfo.curHeartFailCount.get());
            if (heartbeatInfo.curHeart >= maxHeart) {
                heartbeatInfo.curHeartFailCount.set(maxCurHeartFailCount); // 如果当前心跳已经超出最大心跳区间就把探测心跳的结束条件设置为满足，即当前心跳失败次数大于等于5
                LogUtils.i("the curHeart [" + heartbeatInfo.curHeart + "] is larger than maxHeart [" + maxHeart + "],finish probe heartbeat.");
            }
        } else {
            int count = heartbeatInfo.curHeartFailCount.incrementAndGet();
            LogUtils.w("the curHeart is failed,increase curHeartFailCount:" + count);
        }
    }

    public static void stableHeartbeat(Context context, boolean isSuccess) {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            heartbeatInfo = new HeartbeatInfo();
            heartBeatInfoMap.put(networkTag, heartbeatInfo);
        }
        if (isSuccess) {
            heartbeatInfo.successHeartFailedCount.set(0);
            LogUtils.i("reset successHeartFailedCount as 0");
            int count = heartbeatInfo.stableHeartSuccessCount.incrementAndGet();
            if (count >= maxStableHeartSuccessCount && heartbeatInfo.successHeart < maxHeart - probeHeartStep) {
                heartbeatInfo.curHeartFailCount.set(0);
                LogUtils.i("reset curHeartFailCount as 0");
                cancelStableHeartbeat(context);
                startProbeHeartbeat(context);
            }
        } else {
            heartbeatInfo.stableHeartSuccessCount.set(0);
            int count = heartbeatInfo.successHeartFailedCount.incrementAndGet();
            LogUtils.w("increase successHeartFailedCount:" + count);
            if (count >= maxSuccessHeartFailCount) {
                heartbeatInfo.successHeart = minHeart;
                heartbeatInfo.curHeart = heartbeatInfo.successHeart;
                heartbeatInfo.curHeartFailCount.set(0);
                LogUtils.i("adjust successHeart [" + heartbeatInfo.successHeart + "],curHeart [" + heartbeatInfo.curHeart + "" +
                        "] and reset curHeartFailCount:" + heartbeatInfo.curHeartFailCount.get());
            }
        }
    }

    public static void start(Context context) {
        isStarted = true;
        networkTag = NetUtil.getNetworkTag(context);
    }

    public static void cancel(Context context) {
        isStarted = false;
        cancelShortHeartbeat(context);
        cancelProbeHeartbeat(context);
        cancelStableHeartbeat(context);
    }

    public static void clear(Context context) {
        resetHeartbeatInfo();
        cancel(context);
        heartBeatInfoMap.clear();
        networkTag = null;
    }

    public static void clearHeartbeatInfo() {
        heartBeatInfoMap.clear();
    }

    public static boolean isCanceled() {
        return !isStarted;
    }

    public static int getShortHeartbeat() {
        return shortHeartbeat;
    }

    public static int getCurrentHeartbeat() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return 0;
        }
        return heartbeatInfo.curHeart;
    }

    public static int getStableHeartbeat() {
        HeartbeatInfo heartbeatInfo = heartBeatInfoMap.get(networkTag);
        if (heartbeatInfo == null) {
            return 0;
        }
        return heartbeatInfo.successHeart;
    }

    public static void startNextHeartbeat(Context context) {
        if (HeartBeatScheduler.isShortHeartbeat()) {
            if (!isFinishShortHeartbeat()) {
                startShortHeartbeat(context);
                return;
            }
            // 短心跳结束
            cancelShortHeartbeat(context);
            if (!isFinishProbeHeartbeat()) {
                startProbeHeartbeat(context);
                return;
            }
            // 探测心跳结束
            cancelProbeHeartbeat(context);
            startStableHeartbeat(context);
        } else if (isProbeHeartbeat()) {
            if (isFinishProbeHeartbeat()) {
                cancelProbeHeartbeat(context);
                startStableHeartbeat(context);
            } else {
                startProbeHeartbeat(context);
            }
        } else if (isStableHeartbeat()) {
            startStableHeartbeat(context);
        } else {
            LogUtils.w("heartbeat type is error!");
        }
    }

    public static void resetHeartbeat(Context context) {
        startNextHeartbeat(context);
    }
}
