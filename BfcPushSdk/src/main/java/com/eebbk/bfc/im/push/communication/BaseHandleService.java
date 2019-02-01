package com.eebbk.bfc.im.push.communication;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.SparseArray;

import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseHandleService extends IntentService {

    private String name;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public BaseHandleService(String name) {
        super(name);
        this.name = name;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.d(name + " onHandleMessage...");
    }

}
