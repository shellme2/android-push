package com.eebbk.bfc.im.push.bean;

import android.app.Notification;

public class NotificationInfo {

    private int notificationId;

    private long syncKey;

    private long dialogId;

    private Notification notification;

    public NotificationInfo(int notificationId, long dialogId, long syncKey, Notification notification) {
        this.notificationId = notificationId;
        this.dialogId = dialogId;
        this.syncKey = syncKey;
        this.notification = notification;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public long getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(long syncKey) {
        this.syncKey = syncKey;
    }

    public long getDialogId() {
        return dialogId;
    }

    public void setDialogId(long dialogId) {
        this.dialogId = dialogId;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        return "NotificationInfo{" +
                "notificationId=" + notificationId +
                ", syncKey=" + syncKey +
                ", dialogId=" + dialogId +
                ", notification=" + notification +
                '}';
    }
}
