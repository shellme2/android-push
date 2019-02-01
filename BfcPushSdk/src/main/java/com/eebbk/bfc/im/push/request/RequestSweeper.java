package com.eebbk.bfc.im.push.request;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 请求队列扫描
 */
public class RequestSweeper {

    private static final String TAG = "RequestSweeper";

    private PushApplication app;

    private SweepHandler sweepHandler;

    /**
     * 扫描时间间隔
     */
    private static int period = 1000;

    private AtomicBoolean started = new AtomicBoolean(false);

    private static class SweepHandler extends Handler {

        WeakReference<RequestSweeper> target;

        SweepHandler(Looper looper, RequestSweeper requestSweeper) {
            super(looper);
            this.target = new WeakReference<>(requestSweeper);
        }

        @Override
        public void handleMessage(Message msg) {
            RequestSweeper requestSweeper = target.get();
            if (requestSweeper == null) {
                return;
            }
            if (requestSweeper.started.get()) {
                requestSweeper.sweep();
                if (requestSweeper.app.getRequestManager().isEmpty()) {
                    requestSweeper.cancel();
                } else {
                    sendEmptyMessageDelayed(1, 1000);
                }
            } else {
                requestSweeper.cancel();
            }

        }
    }

    public RequestSweeper(PushApplication app) {
        this.app = app;
        HandlerThread handlerThread = new HandlerThread(SweepHandler.class.getSimpleName());
        handlerThread.start();
        sweepHandler = new SweepHandler(handlerThread.getLooper(), this);
    }

    private void sweep() {
        RequestManager requestManager = app.getRequestManager();
        if (requestManager == null) {
            return;
        }
        LogUtils.v(TAG,"sweep request...");
        int clearCount = requestManager.dispatchTimeoutRequest();
        if (clearCount > 0) {
            LogUtils.d(TAG,"dispatch [" + clearCount + "] time out requests.");
        }
    }

    /**
     * 启动请求队列周期扫描
     */
    public void start() {
        if (started.get()) {
            LogUtils.w(TAG,"request sweeper is started.");
            return;
        }
//        WakeLockUtil.acquire(app.getContext(), 60000);
        started.set(true);
        LogUtils.d(TAG,"request sweeper started.");
        sweepHandler.sendEmptyMessageDelayed(1, 1000);
    }

    public static int getPeriod() {
        return period;
    }

    public static void setPeriod(int period) {
        RequestSweeper.period = period;
    }

    public void cancel() {
        LogUtils.w(TAG,"cancel request sweeper.");
        started.set(false);
//        WakeLockUtil.release();
    }
}
