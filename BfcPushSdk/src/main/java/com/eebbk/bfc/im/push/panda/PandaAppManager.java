package com.eebbk.bfc.im.push.panda;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eebbk.bfc.common.app.AppUtils;
import com.eebbk.bfc.im.push.bean.AppBindInfo;
import com.eebbk.bfc.im.push.bean.PandaAppInfo;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.service.heartbeat.ConnectSwitchService;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 需要特殊照顾的app
 * 如果动态添加删除app信息需要注意，目前不夸宿主和app层，会数据不同步
 * Created by Administrator on 2017/10/19.
 */

public class PandaAppManager {
    private static final String TAG = "PandaAppManager";

    private static class InstanceHolder {
        private static final PandaAppManager mInstance = new PandaAppManager();
    }

    public static PandaAppManager getInstance() {
        return InstanceHolder.mInstance;
    }

    // 特别照顾的app
    private static final List<PandaAppInfo> PANDA_LIST = Arrays.asList(new PandaAppInfo[]{
            new PandaAppInfo("com.eebbk.parentsupport", 123)
    });

    /**
     * 通知所有需要重点照顾的app重新进行初始化操作
     */
    public void notifyAllPandaApp(Context context, boolean isTurnOn) {
        LogUtils.w(TAG,"宿主切换","notifyAllPandaApp() start");
        List<PandaAppInfo> pandaList = getPandaApps();
        for (PandaAppInfo pandaApp : pandaList) {
            if(pandaApp == null){
                LogUtils.w(TAG,"宿主切换","pandaApp == null");
                continue;
            }
            if(isTurnOn){
                notifyPandaAppTurnOn(context, pandaApp);
            }else {
                notifyPandaAppTurnOff(context, pandaApp);
            }

        }
    }

    /**
     * 通知家长管理重新进行初始化操作
     */
    public void notifyPandaAppTurnOn(Context context, PandaAppInfo pandaApp) {
        LogUtils.i(TAG,"宿主切换","单独兼容家长管控");
        if(pandaApp == null){
            LogUtils.w(TAG,"宿主切换","pandaApp == null");
            return;
        }
        if(TextUtils.isEmpty(pandaApp.getPackageName())){
            LogUtils.w(TAG,"宿主切换","packageName == null");
            return;
        }
        LogUtils.w(TAG,"宿主切换","notifyPandaApp packageName:" + pandaApp.getPackageName());
        Intent intent = new Intent();
        intent.setAction(SyncAction.CONNECT_SWITCH_SERVICE_ACTION);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
        intent.putExtra(ConnectSwitchService.BUNDLE_KEY_SERVICE_SWITCH, ConnectSwitchService.BUNDLE_VALUE_SERVICE_SWITCH_ON);
        intent.setComponent(new ComponentName(pandaApp.getPackageName(), ConnectSwitchService.class.getName()));
        startService(context, intent);
    }

    private void notifyPandaAppTurnOff(Context context, PandaAppInfo pandaApp) {
        LogUtils.i(TAG,"宿主切换","单独兼容 turnoff");
        if(pandaApp == null){
            LogUtils.w(TAG,"宿主切换","pandaApp == null");
            return;
        }
        if(TextUtils.isEmpty(pandaApp.getPackageName())){
            LogUtils.w(TAG,"宿主切换","packageName == null");
            return;
        }
        Intent intent = new Intent();
        intent.setAction(SyncAction.CONNECT_SWITCH_SERVICE_ACTION);
        intent.putExtra(ConnectSwitchService.BUNDLE_KEY_SERVICE_SWITCH, ConnectSwitchService.BUNDLE_VALUE_SERVICE_SWITCH_OFF);
        intent.setComponent(new ComponentName(pandaApp.getPackageName(), ConnectSwitchService.class.getName()));
        startService(context, intent);
    }

    /**
     * 通知所有特殊照顾的app重新进行初始化操作
     * @param context
     * @param appBindInfo 当前宿主绑定的app信息
     */
    public void notifyPandaAppReboot(Context context, AppBindInfo appBindInfo) {
        LogUtils.i(TAG,"宿主切换","兼容特殊照顾的app重启");
        List<PandaAppInfo> pandaList = getPandaApps();
        // 断网重连后由于4.0.5-bugfix有个 PushApplication.isIniting 没有重置，导致再次联网后跑到login后发送不了
        // PushSyncTriggerRequest 的请求，尝试过调用turnoff和trunon，但是没有效果。
        // 所以屏蔽下面检查绑定后再杀的代码，每次连接后都杀。

        //获取当前绑定的app信息
//        if (appBindInfo != null && appBindInfo.getBindAppMap() != null) {
//            Map<String, AppPushInfo> appPushInfoMap = appBindInfo.getBindAppMap();
//            for (PandaAppInfo pandaApp : pandaList) {
//                LogUtils.i(TAG,"宿主切换","check bind pandaApp.getPackageName():" + pandaApp.getPackageName());
//                if (!appPushInfoMap.containsKey(pandaApp.getPackageName())) {
//                    // 如果特殊照顾的app没有绑定到当前的宿主，就重启它
//                    notifyPandaAppReboot(context, pandaApp);
//                } else{
//                    LogUtils.i(TAG,"宿主切换","has binded pandaApp.getPackageName():" + pandaApp.getPackageName());
//                }
//            }
//        }else {
        LogUtils.i(TAG,"宿主切换","重启所有特殊照顾的app重启");
        for (PandaAppInfo pandaApp : pandaList) {
            notifyPandaAppReboot(context, pandaApp);
        }
//        }
    }

    /**
     * 通知所有特殊照顾的app重新进行初始化操作
     */
    private void notifyPandaAppReboot(Context context, PandaAppInfo pandaApp) {
        LogUtils.i(TAG,"宿主切换","兼容特殊照顾的app重启");
        if(pandaApp == null){
            LogUtils.i(TAG,"宿主切换","pandaApp == null");
            return;
        }
        List<Integer> pids = AppUtil.getPidByName(context, pandaApp.getPackageName());
        if(pids.size() > 0){
            int versionCode = AppUtils.getVersionCode(context, pandaApp.getPackageName());
            if(versionCode >= pandaApp.getVersionCode()){
                LogUtils.i(TAG,"宿主切换","无需重启特殊照顾的app:" + pandaApp.getPackageName()
                        + "\npandaApp.getVersionCode():" + pandaApp.getVersionCode()
                        + "\nversionCode:" + versionCode);
                return;
            }
            for (int pid : pids) {
                LogUtils.i(TAG,"宿主切换",context.getPackageName() + " 尝试重启 pid:" + pid);
                Intent intent = new Intent();
                intent.setAction(SyncAction.CONNECT_SWITCH_SERVICE_ACTION);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
                intent.putExtra(ConnectSwitchService.BUNDLE_KEY_SERVICE_SWITCH, ConnectSwitchService.BUNDLE_VALUE_SERVICE_SWITCH_ON);
                intent.putExtra(ConnectSwitchService.BUNDLE_KEY_PID, pid);
                intent.setComponent(new ComponentName(pandaApp.getPackageName(), ConnectSwitchService.class.getName()));
                startService(context, intent);
            }
        }else{
            //正在运行的列表中找不到，就拉起
            notifyPandaAppTurnOn(context, pandaApp);
        }
    }

    /**
     * 获取需要特殊照顾app
     * @return
     */
    public List<PandaAppInfo> getPandaApps(){
        return new ArrayList<>(PANDA_LIST);
    }

    /**
     * 添加特殊照顾app
     * @param pandaAppInfo
     * @return
     */
    public boolean addPandaApp(PandaAppInfo pandaAppInfo){
        if(pandaAppInfo == null || TextUtils.isEmpty(pandaAppInfo.getPackageName())){
            LogUtils.w(TAG,"宿主切换","addPandaApp packageName == null");
            return false;
        }
        if(PANDA_LIST.contains(pandaAppInfo)){
            LogUtils.w(TAG,"宿主切换","contains packageName:" + pandaAppInfo.getPackageName());
            return false;
        }
        return PANDA_LIST.add(pandaAppInfo);
    }

    /**
     * 移除特殊照顾app
     * @return
     */
    public boolean removeAllPandaApp(){
        if(PANDA_LIST.size() <= 0){
            LogUtils.w(TAG,"宿主切换","PANDA_LIST.size() <= 0");
            return false;
        }
        PANDA_LIST.clear();
        return true;
    }

    /**
     * 移除特殊照顾app
     * @param pandaAppInfo
     * @return
     */
    public boolean removePandaApp(PandaAppInfo pandaAppInfo){
        if(pandaAppInfo == null || TextUtils.isEmpty(pandaAppInfo.getPackageName())){
            LogUtils.w(TAG,"宿主切换","addPandaApp packageName == null");
            return false;
        }
        if(!PANDA_LIST.contains(pandaAppInfo)){
            LogUtils.w(TAG,"宿主切换","do not contains packageName:" + pandaAppInfo.getPackageName());
            return false;
        }
        return PANDA_LIST.remove(pandaAppInfo);
    }

    private ComponentName startService(Context context, Intent intent){
        try {
            return context.startService(intent);
        } catch (Exception e) {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE," startService error !!! ", e);
        }
        return null;
    }

    private PandaAppManager(){

    }
}
