package com.eebbk.bfc.im.push.debug;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/20 16:51
 * Email:  zengjingfang@foxmail.com
 */
public abstract class DebugBaseTool {

    public static final String TAG = "DebugTool >>> ";


    private static volatile Looper mLooper;
    public DebugHandler mDebugHandler;
    private String mName;

    public DebugBaseTool(String toolName) {
        mName = toolName;
        HandlerThread thread = new HandlerThread(toolName+"_handler_thread");
        thread.start();
        mLooper = thread.getLooper();
        mDebugHandler = new DebugHandler(mLooper);
    }

    public void destroy() {
        mDebugHandler.removeMessages(0);
        if (mLooper != null) {
            mLooper.quit();
        }
        mDebugHandler = null;
        mLooper = null;
    }

    public void sendMessage(Object  obj) {
        if (mDebugHandler != null) {
            mDebugHandler.sendMessage(mDebugHandler.obtainMessage(0, obj));

        }
    }

    public void sendMessageDelayed(Object obj, long delayedTime) {
        mDebugHandler.sendMessageDelayed(mDebugHandler.obtainMessage(0, obj), delayedTime);
    }

    private  final class DebugHandler extends android.os.Handler {
        public DebugHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleMessage( msg.obj);
        }

    }

    protected abstract void onHandleMessage(Object obj);

}
