package com.eebbk.bfc.im.push;

import android.content.Context;
import android.support.annotation.NonNull;

import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.listener.OnResultListener;

import java.util.List;
/**
 * @author liuyewu
 * Push SDK对外提供的接口
 *
 * SDK集成使用：
 * 在项目工程的AndroidManifest.xml文件中配置<p>< meta-data
android:name="SYNC_APPKEY"
android:value="7be64b5953d6417490f9609154cc22fd" />
< meta-data
android:name="SYNC_RID_TAG"
android:value="1"/></p>
 * 其中SYNC_RID_TAG中的value由不同的项目工程而设置不同的值，例如工程1配置1，工程2就配置2，以此类推
 * <p>1.集成aar包，把aar包拷贝到libs目录下，在build.gradle中添加<p>repositories {
flatDir {
dirs 'libs' //this way we can find the .aar file in libs folder
}
}</p>配置，并在dependencies中添加compile(name:'sync-debug', ext:'aar')</p>
 * <p>2.或者集成lib工程依赖，点击File->Project structure->选择项目工程->Dependencies->加号->File Dependency</p>
 *
 * 2016.09.20
 */
public class EebbkPush extends PushImplements {

    /**
     * 设置URL环境  需要在初始化init之前设置
     * @param isUrlDebug 默认为false 正式开发环境
     */
    @Deprecated
    public static void setUrlDebugMode(boolean isUrlDebug) {
        setUrlDebug(isUrlDebug);
    }

    /**
     * SDK 初始化
     * @param context 上下文对象，最好传ApplicationContext
     * @param onInitSateListener 初始化完成回调监听器
     */
    @Deprecated
    public static void init(@NonNull Context context, OnInitSateListener onInitSateListener){
        initRun(context, onInitSateListener, null);
    }

    /**
     * SDK 初始化
     * @param context 上下文对象，最好传ApplicationContext
     * @param onInitSateListener 初始化完成回调监听器
     */
    @Deprecated
    public static void init(@NonNull Context context, OnInitSateListener onInitSateListener, OnPushStatusListener onPushStatusListener){
        initRun(context, onInitSateListener, onPushStatusListener);
    }

    /**
     *主动拉取
     * <p>初始化后,为了数据即时同步，进行一次主动拉取，获取服务器上的推送信息</p>
     * <p>或者是其他需要主动获取服务器推送信息的时候调用</p>
     * @param onResultListener 请求发送成功回调，判断主动拉取请求是否请求成功，及成功后的相关操作
     */
    @Deprecated
    public static void sendPushSyncTrigger(OnResultListener onResultListener){
        sendPushSyncTriggerRun(onResultListener);
    }

    /**
     * 设置标签   标签：过滤接收的内容，默认null接收所有内容
     * @param tags 标签，可以设多个,只能以数字字母下划线组成，字符长度小于40的字符串集合
     * @param onAliasAndTagsListener 设置成功或失败监听
     */
    @Deprecated
    public static void setTags(List<String> tags,OnAliasAndTagsListener onAliasAndTagsListener){
        setTagsRun(tags,onAliasAndTagsListener);
    }

    /**
     * 调试模式设置，用于控制一些log信息等
     * @param debug true打开调试模式；false关闭调试模式
     */
    @Deprecated
    public static void setDebugMode(boolean debug){
        setDebugModeRun(debug);
    }

    /**
     * 停止推送，关闭推送接收信息
     */
    @Deprecated
    public static void stopPush(OnResultListener onResultListener){
        stopPushRun(onResultListener);
    }

    /**
     * 重新开始推送，重新开始接收推送消息
     */
    @Deprecated
    public static void resumePush(OnResultListener onResultListener){
        resumePushRun(onResultListener);
    }

    /**
     * 推送是否开启
     * @return true关闭；false开启
     */
    @Deprecated
    public static boolean isStopPush(){
        return isStopPushRun();
    }


}
