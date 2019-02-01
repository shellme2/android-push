package com.eebbk.bfc.im.push.util;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockUtil {
    //构造函数私有，防止恶意新建
    private WakeLockUtil(){}

    private volatile static PowerManager.WakeLock wakeLock;

    private static PowerManager.WakeLock createWakeLock(Context context, String tag) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
        wakeLock.setReferenceCounted(false);
        return wakeLock;
    }

    public synchronized static void acquire(Context context) {
        if (wakeLock == null) {
            wakeLock = createWakeLock(context, "WakeLock");
            wakeLock.acquire();
        } else {
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
                LogUtils.d("acquire the wake lock...");
            }
        }
    }

    public synchronized static void acquire(Context context, int timeout) {
        if (wakeLock == null) {
            wakeLock = createWakeLock(context, "bfc-push-connect-lock");
            wakeLock.acquire(timeout);
        } else {
            if (!wakeLock.isHeld()) {
                wakeLock.acquire(timeout);
                LogUtils.d("acquire the wake lock...");
            }
        }
    }

    public synchronized static void release() {
        if (wakeLock == null) {
            return;
        }
        if (wakeLock.isHeld()) {
            wakeLock.release();
            LogUtils.d("release the wake lock...");
        }
        wakeLock = null;
    }
}
