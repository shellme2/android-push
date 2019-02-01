package com.eebbk.bfc.im.push.util;

import android.app.Notification;
import android.content.Context;
import android.os.Build;

import com.eebbk.bfc.im.push.service.ContextSelector;

/**
 * @author hesn
 *         2018/4/13
 */

public class NotificationUtil {

    public static final int NOTIFICATION_ID = 1314;

    public static Notification getNotification(Context context){
        Notification noti;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Notification.Builder builder = new Notification.Builder(selectContext(context))
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .setContentText("BBK Push Connect ...");//系统约定 该通知不弹出
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                builder.setGroup("no_tick");// 系统代码已经找不到此group“com.eebbk.no_notification”，改为no_tick
            }
            noti = builder.build();
        } else {
            noti = new Notification();
        }
        LogUtils.e("NotificationUtil", noti.toString());
        return noti;
    }

    /**
     * 老机器（如H9）系统各种原因无法更新，所以没有对"BBK Push Connect ..."通知同步屏蔽兼容，导致会出现此通知，
     * 但是旧系统是有屏蔽第三方通知，所以创建Notification.Builder时传一个第三方的context。
     * 但是根据用户反馈还是会弹通知，获取不到用户的日志，我们烧录同样版本又没有出现此问题，所以还不确定是否可行
     * @param srcContext
     * @return
     */
    private static Context selectContext(Context srcContext){
        Context targetContext = ContextSelector.selectContextByPkgName(srcContext, "com.sohu.inputmethod.sogou");
        return targetContext != null ? targetContext : srcContext;
    }
}
