package com.eebbk.bfc.im.push.service.dispatcher;

import android.app.NotificationManager;
import android.content.Context;

import com.eebbk.bfc.im.push.bean.NotificationInfo;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.HashMap;
import java.util.Map;

public class SyncNotificationManager {

    private volatile static SyncNotificationManager syncNotificationManager;

    private Map<Long, Long> onHandleNotificationMap;

    public static SyncNotificationManager getSyncNotificationManager() {
        if (syncNotificationManager == null) {
            synchronized (SyncNotificationManager.class) {
                if (syncNotificationManager == null) {
                    syncNotificationManager = new SyncNotificationManager();
                }
            }
        }
        return syncNotificationManager;
    }

    private SyncNotificationManager() {
        onHandleNotificationMap = new HashMap<>();
    }

    private synchronized static void release() {
        if (syncNotificationManager != null) {
            syncNotificationManager = null;
        }
    }

    public synchronized void clear() {
        onHandleNotificationMap.clear();
        release();
    }

    public void onShowNotification(long dialogId, long syncKey) {
        onClickNotification(dialogId, syncKey);
    }

    public void onClickNotification(long dialogId, long syncKey) {
        if (dialogId <= 0) {
            return;
        }
        if (syncKey <= 0) {
            return;
        }
        synchronized (this) {
            if (onHandleNotificationMap.size() > 1000) {
                onHandleNotificationMap.clear();
            }
            onHandleNotificationMap.put(dialogId, syncKey);
        }
        LogUtils.i("onClickNotification dialogId:" + dialogId + ",syncKey:" + syncKey);
    }

    public boolean isNotified(long dialogId, long syncKey) {
        synchronized(this) {
            Long localSyncKey = onHandleNotificationMap.get(dialogId);
            if (localSyncKey != null && localSyncKey.longValue() >= syncKey) {
                return true;
            }
        }
        return false;
    }

    public boolean showNotification(Context context, NotificationInfo notificationInfo) {
        long dialogId = notificationInfo.getDialogId();
        long syncKey = notificationInfo.getSyncKey();
        synchronized (this) {
            Long localSyncKey = onHandleNotificationMap.get(dialogId);
            if (localSyncKey != null && localSyncKey.longValue() >= syncKey) {
                LogUtils.d("notification dialogId:" + dialogId + ",syncKey:" + syncKey);
                return false;
            }
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationInfo.getNotificationId(), notificationInfo.getNotification());
        onShowNotification(dialogId, syncKey);
        LogUtils.i("showNotification dialogId:" + dialogId + ",syncKey:" + syncKey);
        return true;
    }
}
