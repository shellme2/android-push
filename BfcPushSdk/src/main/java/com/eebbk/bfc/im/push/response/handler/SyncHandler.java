package com.eebbk.bfc.im.push.response.handler;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.request.Request;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.NetUtil;

import java.lang.reflect.Field;

public abstract class SyncHandler {
    private static final String TAG = "SyncHandler";
    protected PushApplication app;

    protected static Looper retryLooper;

    protected static Handler retryHandler;

    protected class RetryHandler extends Handler {

        RetryHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Field[] fields = Command.class.getFields();
            if (fields != null) {
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        int cmd = field.getInt(Command.class);
                        if (what == cmd) {
                            LogUtils.i("what:" + what + ",command:" + cmd + ",cmd name:" + field.getName());
                            Request req = (Request) msg.obj;
                            Request.retryRequest(req);
                            return;
                        }
                    } catch (IllegalAccessException e) {
                        LogUtils.e(e);
                    }
                }
            }
            LogUtils.e( TAG,"retry error,msg what is wrong...");
        }
    }

    public SyncHandler(PushApplication app) {
        LogUtils.i("create sync handler:" + this.getClass().getSimpleName());
        this.app = app;
        if (retryHandler == null) {
            HandlerThread handlerThread = new HandlerThread("retry_handler_thread");
            handlerThread.start();
            retryLooper = handlerThread.getLooper();
            retryHandler = new RetryHandler(retryLooper);
        }
    }

    public static void cancelAllRetry() {
        if (retryHandler != null) {
            retryHandler.removeCallbacksAndMessages(null);
            LogUtils.w("cancel all retry...");
        }
    }

    protected void recycle() {

    }

    protected void cancelRetry(int what) {
        if (retryHandler != null) {
            retryHandler.removeMessages(what);
            LogUtils.i("cancel retry message,what(command):" + what);
        }
    }

    protected void startRetry(Request request) {
        if (request == null) {
            return;
        }
        request.setOnRetry(true);
        if (!NetUtil.isConnectToNet(app.getContext())) {
            // 如果没联网就不进行重试操作
            LogUtils.e( TAG," Network is unreachable,do not retry send request:" + request.getRequestEntity());
            request.setOnRetry(false);
            return;
        }
        if (request.isNeedRetry()) {
            int retryCount = request.getRetryCount();
            int maxRetryCount = request.getMaxRetryCount();
            retryCount++;
            request.setRetryCount(retryCount);
            if (retryCount < maxRetryCount) {

                LogUtils.w(request.getCommand()+"retryCount::maxRetryCount==" +retryCount + "::" + maxRetryCount + ",retry ...");

                int reqCmd = request.getCommand();
                Message msg = retryHandler.obtainMessage();
                msg.what = reqCmd;
                msg.obj = request;
                retryHandler.sendMessageDelayed(msg, request.getRetryDelayTime());
            } else {
                request.setOnRetry(false);
                LogUtils.w(request.getCommand()+"retryCount::maxRetryCount==" +retryCount + "::" + maxRetryCount + ",retry finished...");
            }
        } else {
            request.setOnRetry(false);
        }
    }

    public abstract void handle(Request request, Response response);
}
