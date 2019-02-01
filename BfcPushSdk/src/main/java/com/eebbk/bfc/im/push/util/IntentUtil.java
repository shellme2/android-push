package com.eebbk.bfc.im.push.util;

import android.content.Context;
import android.content.Intent;

import com.eebbk.bfc.im.push.communication.SyncAction;

public class IntentUtil {

    //构造函数私有，防止恶意新建
    private IntentUtil(){}

    public static Intent createIntent(Context context, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.addCategory(SyncAction.TAG);
        intent.setPackage(context.getPackageName());
        intent.addFlags(Intent. FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
        return intent;
    }
}
