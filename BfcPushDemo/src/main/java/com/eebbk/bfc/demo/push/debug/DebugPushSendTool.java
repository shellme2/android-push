package com.eebbk.bfc.demo.push.debug;

import com.eebbk.bfc.demo.push.Constant;
import com.eebbk.bfc.demo.push.PushTestApplication;
import com.eebbk.bfc.demo.push.debug.sendFactory.IPushSendProduct;
import com.eebbk.bfc.demo.push.debug.sendFactory.PushSendFactory;
import com.eebbk.bfc.demo.push.debug.sendFactory.PushSendProduct;
import com.eebbk.bfc.http.config.BfcHttpConfigure;
import com.eebbk.bfc.im.push.debug.DebugBaseTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/20 16:42
 * Email:  zengjingfang@foxmail.com
 */
public class DebugPushSendTool extends DebugBaseTool {

    public static final int URL_MODE_TEST = 0;
    public static final int URL_MODE_RELEASE = 1;

    private String mAppName;
    private int mUrlMode=URL_MODE_RELEASE;
    /**
     * 间隔时间
     */
    private long mPeriod=10*1000;
    /**
     * 每次发送N条消息
     */
    private int mCount = 1;

    private Timer mTimer;
    private TimerTask mTimerTask;


    IPushSendProduct mDemoFactory;
    IPushSendProduct mDemoTestFactory;

    private PushSendProduct mDebugOn;
    private PushSendProduct mDebugOff;
    private PushSendProduct mDebugOnTest;
    private PushSendProduct mDebugOffTest;

    private List<PushSendProduct> mSendCacheList = new ArrayList<>();

    public DebugPushSendTool() {
        super("DebugPushSendTool");
        BfcHttpConfigure.init(PushTestApplication.getAppContext());
        initSendProduct();
        startTimer();
    }

    private ResultCallBack<PushSendProduct> mPushSendProductResultCallBack;

    private void initSendProduct() {
        mDemoFactory = new PushSendFactory.PushSendDemoFactory();

        mDemoTestFactory = new PushSendFactory.PushSendDemoTestFactory();

        mDebugOn = new PushSendFactory.PushSendDemoONFactory().createPushSendProduct();
        mDebugOff = new PushSendFactory.PushSendDemoOFFFactory().createPushSendProduct();

        mDebugOnTest = new PushSendFactory.PushSendDemoTestONFactory().createPushSendProduct();
        mDebugOffTest = new PushSendFactory.PushSendDemoTestOFFFactory().createPushSendProduct();

    }

    public void reset() {
        mSendCacheList.clear();
    }

    @Override
    public void destroy() {
        super.destroy();
        stopTimer();
        reset();
    }

    public boolean sendHttpPush(final PushSendProduct sendProduct) {

        mSendCacheList.add(sendProduct);//缓存到list
        try {
            Map<String, String> params = sendProduct.convertToParams();
            sendProduct.httpRequest(sendProduct.getUrl(), params, new SendCallBack() {
                @Override
                public void onSuccess() {
                    if(mPushSendProductResultCallBack == null){
                        return;
                    }
                    mPushSendProductResultCallBack.onSuccess(sendProduct);
                }

                @Override
                public void onFailed() {
                    if(mPushSendProductResultCallBack == null){
                        return;
                    }
                    mPushSendProductResultCallBack.onFailed(sendProduct);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onHandleMessage(Object obj) {
        PushSendProduct sendProduct = (PushSendProduct) obj;
        sendHttpPush(sendProduct);

    }


    public void setPushDebugMode(int urlMode, String appName, int count,long period) {
        mUrlMode = urlMode;
        mAppName = appName;
        mCount = count;
        mPeriod = period;
    }

    public void scheduleTask() {
        if (mTimer != null && mTimerTask != null) {
            return;
        }
        startTimer();
        mTimer.schedule(mTimerTask,  1000, mPeriod);
    }

    public void PauseTask() {
        stopTimer();
    }

    public void turnOnDebugMode() {
        switch (mUrlMode) {
            case URL_MODE_RELEASE:
                sendMessage(mDebugOn);
                break;
            case URL_MODE_TEST:
                sendMessage(mDebugOnTest);
                break;
            default:
                break;
        }
    }

    public void turnOffDebugMode() {
        switch (mUrlMode) {
            case URL_MODE_RELEASE:
                sendMessage(mDebugOff);
                break;
            case URL_MODE_TEST:
                sendMessage(mDebugOffTest);
                break;
            default:
                break;
        }
    }

    private long createRandomDelayTime() {
        long min = 0;
        long max = (long) (mPeriod*0.1);
        Random random = new Random();
        return (long) (random.nextDouble() * (max - min) + min);
    }


    private synchronized void sendPush() {
        long delayTime = createRandomDelayTime();
        switch (mUrlMode) {
            case URL_MODE_RELEASE:
                if (Constant.SendRelease.APP_NAME.equalsIgnoreCase(mAppName)) {
                    for (int i=0;i<mCount;i++) {
                        PushSendProduct sendProduct = mDemoFactory.createPushSendProduct();
                        sendMessageDelayed(sendProduct, delayTime);
                    }
                }
                break;
            case URL_MODE_TEST:
                if (Constant.SendDebug.APP_NAME.equals(mAppName)) {
                    for (int i=0;i<mCount;i++) {
                        PushSendProduct sendProduct = mDemoTestFactory.createPushSendProduct();
                        sendMessageDelayed(sendProduct, delayTime);
                    }
                }
                break;
            default:
                break;
        }

    }


    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    sendPush();
                }
            };
        }
    }

    private void stopTimer() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }


    public interface SendCallBack{
        void onSuccess();

        void onFailed();
    }

    public void unRegisterPushSendResultCallBack() {
        mPushSendProductResultCallBack = null;
    }
    public void registerPushSendResultCallBack(ResultCallBack<PushSendProduct> pushSendProductResultCallBack) {
        mPushSendProductResultCallBack = pushSendProductResultCallBack;
    }
}
