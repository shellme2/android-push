package com.eebbk.bfc.im.push;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.debug.DebugBasicInfo;
import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.listener.OnResultListener;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.StoreUtil;

import java.util.List;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/5/5 11:41
 * Email:  zengjingfang@foxmail.com
 */
public class BfcPushImpl implements BfcPush {
    private BfcPush.Settings mSettings;

    public BfcPushImpl(BfcPush.Settings settings) {
        mSettings = settings;
    }

    @Override
    public void init(@NonNull Context context, OnInitSateListener onInitSateListener) {
        init(context, onInitSateListener, null);
    }

    @Override
    public void init(@NonNull Context context, OnInitSateListener onInitSateListener, OnPushStatusListener onPushStatusListener) {
        if(context == null){
            throw new NullPointerException("bfcPush initialization failed, context == null");
        }
        Log.w(LogTagConfig.LOG_TAG_FLOW_PUSH_INIT, "初始化参数： urlMode=" + mSettings.getUrlMode() + " debug: " + mSettings.isDebug());
        EebbkPush.setDebugMode(mSettings.isDebug());
        EebbkPush.setUrlDebugMode(mSettings.getUrlMode() != Settings.URL_MODE_RELEASE);
        EebbkPush.init(context.getApplicationContext(), onInitSateListener, onPushStatusListener);
    }

    @Override
    public void sendPushSyncTrigger(OnResultListener onResultListener) {
        EebbkPush.sendPushSyncTrigger(onResultListener);
    }

    @Override
    public void setTags(List<String> tags, OnAliasAndTagsListener onAliasAndTagsListener) {
        EebbkPush.setTags(tags, onAliasAndTagsListener);
    }


    @Override
    public void stopPush(OnResultListener onResultListener) {
        EebbkPush.stopPush(onResultListener);
    }

    @Override
    public void resumePush(OnResultListener onResultListener) {
        EebbkPush.resumePush(onResultListener);
    }

    @Override
    public boolean isStopPush() {
        return EebbkPush.isStopPush();
    }

    @Override
    public String analyzePush() {
        return PushImplements.analyzePush();
    }

    @Override
    public DebugBasicInfo getDebugBasicInfo() {
        return PushImplements.getDebugBasicInfo();
    }

    @Override
    public void clearData(Context context) {
        if(context == null){
            throw new NullPointerException("context cannot be null!!!");
        }
        LogUtils.i("BfcPushImpl", "CLEAR_STORE_DATA_TYPE_ALL");
        PushImplements.getPushApplicationSafely(context.getApplicationContext(), new OnGetCallBack<PushApplication>() {
            @Override
            public void onGet(PushApplication app) {
                app.exit();
                StoreUtil.clearDeviceInfo(app.getPlatform().getStore());
            }
        });
    }

    @Override
    public void setOnPushStatusListener(OnPushStatusListener onPushStatusListener) {
        PushImplements.setOnPushStatusListener(onPushStatusListener);
    }
}
