package com.eebbk.bfc.im.push.util;

import android.content.Context;
import android.widget.Toast;

public class SyncToastUtil {

    //构造函数私有，防止恶意新建
    private SyncToastUtil(){}

    public static void showToast(final Context context, final String text) {
        AsyncExecutorUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void showToast(final Context context, final String text, final int gravity) {
        AsyncExecutorUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                toast.setGravity(gravity, 0, 0);
                toast.show();
            }
        });
    }


}
