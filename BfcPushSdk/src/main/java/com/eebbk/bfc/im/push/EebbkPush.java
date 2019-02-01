package com.eebbk.bfc.im.push;

import android.content.Context;
import android.support.annotation.NonNull;

import com.eebbk.bfc.im.push.listener.OnAliasAndTagsListener;
import com.eebbk.bfc.im.push.listener.OnInitSateListener;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.listener.OnStopResumeListener;

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
public class EebbkPush extends PushInterface{
    /**
     * SDK 初始化
     * @param context 上下文对象，最好传ApplicationContext
     * @param onInitSateListener 初始化完成回调监听器
     */
    public static void init(Context context, OnInitSateListener onInitSateListener){
        initRun(context, onInitSateListener);
    }

    /**
     *主动拉取
     * <p>初始化后,为了数据即时同步，进行一次主动拉取，获取服务器上的推送信息</p>
     * <p>或者是其他需要主动获取服务器推送信息的时候调用</p>
     * @param onReceiveListener 请求发送成功回调，判断主动拉取请求是否请求成功，及成功后的相关操作
     */
    public static void sendPushSyncTrigger(OnReceiveListener onReceiveListener){
        sendPushSyncTriggerRun(onReceiveListener);
    }

    /**
     * 设置别名标签   别名：推送依据，默认为机械序列号；标签：过滤接收的内容，默认null接收所有内容
     * @param alias 别名，只能是一个，设置会把之前的覆盖，不能为空（null），字符长度不大于45，不能为中文
     * @param tags  标签，可以设多个，字符长度不大于45，不能为中文
     * @param onAliasAndTagsListener 设置成功或失败监听
     */
    public static void setAliasAndTags(String alias,List<String> tags,OnAliasAndTagsListener onAliasAndTagsListener){
        setAliasAndTagsRun(alias,tags,onAliasAndTagsListener);
    }

    /**
     * 设置别名     别名：推送依据，默认为机械序列号
     * @param alias 别名，只能是一个，设置会把之前的覆盖不能为空（null），字符长度不大于45，不能为中文
     * @param onAliasAndTagsListener 设置成功或失败监听
     */
    public static void setAlias(String alias,OnAliasAndTagsListener onAliasAndTagsListener){
        setAliasRun(alias,onAliasAndTagsListener);
    }

    /**
     * 设置标签   标签：过滤接收的内容，默认null接收所有内容
     * @param tags 标签，可以设多个,字符长度不大于45，不能为中文
     * @param onAliasAndTagsListener 设置成功或失败监听
     */
    public static void setTags(List<String> tags,OnAliasAndTagsListener onAliasAndTagsListener){
        setTagsRun(tags,onAliasAndTagsListener);
    }

    /**
     * 调试模式设置，用于控制一些log信息等
     * @param debug true打开调试模式；false关闭调试模式
     */
    public static void setDebugMode(boolean debug){
        setDebugModeRun(debug);
    }

    /**
     * SDK 带confing参数的调试模式设置
     * @param debug true打开调试模式；false关闭调试模式
     * @param confing <b>用于设置推送的一些定制信息，具体如下：</b>
     *                <p>1，心跳包周期临界值和探测步长，单位秒</p>
     *                <p>2，预埋IP信息</p>
     */
    public static void setDebugMode(boolean debug,PushConfig confing){
        setDebugModeRun(debug,confing);
    }
    /**
     * 获取SDK版本号
     * @return 返回版本号
     */
    public static String getSDKVersion(){
        return getSDKVersionRun();
    }


    /**
     * 停止推送，关闭推送接收信息
     */
    public static void stopPush(OnStopResumeListener onStopResumeListener){
        stopPushRun(onStopResumeListener);
    }

    /**
     * 重新开始推送，重新开始接收推送消息
     */
    public static void resumePush(OnStopResumeListener onStopResumeListener){
        resumePushRun(onStopResumeListener);
    }

    /**
     * 推送是否开启
     * @return true关闭；false开启
     */
    public static boolean isStopPush(){
        return isStopPushRun();
    }
}
