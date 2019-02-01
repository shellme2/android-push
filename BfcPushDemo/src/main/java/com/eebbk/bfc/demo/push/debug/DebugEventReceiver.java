package com.eebbk.bfc.demo.push.debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.eebbk.bfc.im.push.debug.DebugEventInfo;
import com.eebbk.bfc.im.push.debug.DebugEventTool;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/20 11:41
 * Email:  zengjingfang@foxmail.com
 */
public class DebugEventReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        DebugEventInfo debugEventInfo = intent.getParcelableExtra(DebugEventTool.DEBUG_PUSH_EVENT_PUBLISH_KEY);

        Log.d(DebugEventTool.TAG, "DebugEventReceiver == "+debugEventInfo.getContent());
    }
}
