package com.eebbk.bfc.im.push;

import android.content.Context;
import android.support.annotation.NonNull;

import com.eebbk.bfc.im.push.debug.DebugBasicInfo;
import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.listener.OnResultListener;

import java.util.List;

/**
 * Desc:   推送对外接口类
 * Author: ZengJingFang
 * Time:   2017/5/5 11:41
 * Email:  zengjingfang@foxmail.com
 */
public interface BfcPush {
    /**
     * 初始化
     * @param context
     * @param onInitSateListener
     */
    void init(@NonNull Context context, OnInitSateListener onInitSateListener);

    /**
     * 初始化
     * @param context
     * @param onInitSateListener
     */
    void init(@NonNull Context context, OnInitSateListener onInitSateListener, OnPushStatusListener onPushStatusListener);

    /**
     * 主动拉取消息
     * @param onResultListener
     */
    void sendPushSyncTrigger(OnResultListener onResultListener);

    /**
     * 设置标签   标签：过滤接收的内容，默认null接收所有内容
     * @param tags
     * @param onAliasAndTagsListener
     */
    void setTags(List<String> tags, OnAliasAndTagsListener onAliasAndTagsListener);

    /**
     * 停止推送，关闭推送接收信息
     * @param onResultListener
     */
    void stopPush(OnResultListener onResultListener);

    /**
     * 重新开始推送，重新开始接收推送消息
     * @param onResultListener
     */
    void resumePush(OnResultListener onResultListener);

    /**
     推送是否开启
     * @return
     */
    boolean isStopPush();


    String analyzePush();

    DebugBasicInfo getDebugBasicInfo();

    /**
     * 清除所有推送相关数据
     */
    void clearData(Context context);

    /**
     * 设置推送状态监听广播
     * @param onPushStatusListener
     */
    void setOnPushStatusListener(OnPushStatusListener onPushStatusListener);

    class Builder{
        Settings mSettings = new Settings();

        /**
         * 调试模式设置，用于控制一些log信息等
         * @param isDebug
         * @return
         */
        public Builder setDebug(boolean isDebug) {
            mSettings.setDebug(isDebug);
            return this;
        }

        /**
         * 设置后台接口环境
         * @param urlMode
         * @return
         */
        public Builder setUrlMode(int urlMode){
            mSettings.setUrlMode(urlMode);
            return this;
        }

        public BfcPushImpl build() {
            return new BfcPushImpl(mSettings);
        }
    }

     class Settings {

        public static final int URL_MODE_RELEASE = 0;
        public static final int URL_MODE_TEST = 1;
        /**
         * 后台开发人员的本机环境
         */
        public static final int URL_MODE_NATIVE = 2;

        boolean debug = false;
        int urlMode= URL_MODE_RELEASE;

        public boolean isDebug() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public int getUrlMode() {
            return urlMode;
        }

        public void setUrlMode(int urlMode) {
            this.urlMode = urlMode;
        }
    }
}
