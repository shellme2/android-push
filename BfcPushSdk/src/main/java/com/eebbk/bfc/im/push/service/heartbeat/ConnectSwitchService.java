package com.eebbk.bfc.im.push.service.heartbeat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.EebbkPush;
import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.PushImplements;
import com.eebbk.bfc.im.push.communication.BaseHandleService;
import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.config.LogTagConfig;
import com.eebbk.bfc.im.push.listener.OnGetCallBack;
import com.eebbk.bfc.im.push.listener.OnResultListener;
import com.eebbk.bfc.im.push.service.ConnectionService;
import com.eebbk.bfc.im.push.service.host.HostServiceInfo;
import com.eebbk.bfc.im.push.util.DeviceUtils;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.PublicValueStoreUtil;
import com.eebbk.bfc.im.push.util.StoreUtil;

/**
 * connect服务启动和关闭控制开关的服务
 */
public class ConnectSwitchService extends BaseHandleService {

    private static final String TAG = "ConnectSwitchService";

    public static final String BUNDLE_KEY_SERVICE_SWITCH = "bundle_key_service_switch";

    public static final String BUNDLE_KEY_PID = "";

    public static final String BUNDLE_KEY_CLEAR_STORE_DATA_TYPE = "clear_store_data_type";

    public static final String BUNDLE_VALUE_SERVICE_SWITCH_ON = "bundle_value_service_switch_on";

    public static final String BUNDLE_VALUE_SERVICE_SWITCH_OFF = "bundle_value_service_switch_off";

    public static final String BUNDLE_VALUE_SERVICE_SWITCH_REINIT = "bundle_value_service_switch_reinit";

    /**
     * 执行设置别名和标签请求
     * 5.0.7-bugfix（不包括此版本）之后版本添加的开关
     */
    public static final String BUNDLE_VALUE_SERVICE_SWITCH_DO_ALIAS_AND_TAG_REQUEST = "bundle_value_service_switch_do_ailas_and_tag_request";

    /**
     * 清除shearPrf中的数据
     * 5.0.7-bugfix（不包括此版本）之后版本添加的开关
     */
    public static final String BUNDLE_VALUE_SERVICE_SWITCH_CLEAR_STORE_DATA = "bundle_value_service_switch_clear_store_data";

    /**
     * 清除所有sharePrf保存数据
     */
    public static final int CLEAR_STORE_DATA_TYPE_ALL = 0;
    /**
     * 清除sharePrf保存数据 RegisterInfo 数据
     */
    public static final int CLEAR_STORE_DATA_TYPE_REGISTER_INFO = 1;
    /**
     * 清除sharePrf保存数据 SyncSession 数据
     */
    public static final int CLEAR_STORE_DATA_TYPE_SYNC_SESSION = 2;
    /**
     * 清除sharePrf保存数据 PublicKey 数据
     */
    public static final int CLEAR_STORE_DATA_TYPE_PUBLIC_KEY = 3;
    /**
     * 清除sharePrf保存数据 AliasAndTag 数据
     */
    public static final int CLEAR_STORE_DATA_TYPE_ALIAS_AND_TAG = 4;


    public ConnectSwitchService() {
        super("ConnectSwitchService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        super.onHandleIntent(intent);

        if (intent == null) {
            LogUtils.e(TAG,"intent is null , then do nothing !!!");
            return;
        }

        String serviceSwitch = intent.getStringExtra(BUNDLE_KEY_SERVICE_SWITCH);
        int pid = intent.getIntExtra(BUNDLE_KEY_PID, 0);
        if (TextUtils.isEmpty(serviceSwitch)) {
            LogUtils.e(TAG, "service switch is empty !!! ");
        } else if (serviceSwitch.equals(BUNDLE_VALUE_SERVICE_SWITCH_ON)) {
            // 先关闭后开启,如果没有传pid，则不Kill
            killPid(pid);
            turnOnConnectService();
            LogUtils.i(TAG, "turn on connect service !!!");
        } else if (serviceSwitch.equals(BUNDLE_VALUE_SERVICE_SWITCH_OFF)) {
            turnOffConnectService();
            killPid(pid);
            LogUtils.i(TAG, "宿主切换 turn off connect service !!!");
        }  else if (serviceSwitch.equals(BUNDLE_VALUE_SERVICE_SWITCH_REINIT)) {
            killPid(pid);
            turnOnConnectService();
            LogUtils.i(TAG, "turn reInit connect service !!!");
        }   else if(serviceSwitch.equals(BUNDLE_VALUE_SERVICE_SWITCH_DO_ALIAS_AND_TAG_REQUEST)){
            LogUtils.i(TAG, "doAliasAndTagRequest !!!");
            doAliasAndTagRequest();
        }   else if(serviceSwitch.equals(BUNDLE_VALUE_SERVICE_SWITCH_CLEAR_STORE_DATA)){
            LogUtils.i(TAG, "clear store data !!!");
            cleanStoreData(intent);
        }   else {
            LogUtils.e(TAG, "service switch must be error !!! ");
        }
    }

    /**
     * 清除shearPrf中的数据
     * @param intent
     */
    private void cleanStoreData(final Intent intent){
        PushImplements.getPushApplicationSafely(getApplicationContext(), new OnGetCallBack<PushApplication>() {
            @Override
            public void onGet(PushApplication app) {
                int type = intent.getIntExtra(BUNDLE_KEY_CLEAR_STORE_DATA_TYPE, -1);
                switch (type){
                    case CLEAR_STORE_DATA_TYPE_ALL:
                        LogUtils.i(TAG, "CLEAR_STORE_DATA_TYPE_ALL");
                        StoreUtil.clearDeviceInfo(app.getPlatform().getStore());
                        DeviceUtils.clearMachineId();
                        break;
                    case CLEAR_STORE_DATA_TYPE_REGISTER_INFO:
                        LogUtils.i(TAG, "CLEAR_STORE_DATA_TYPE_REGISTER_INFO");
                        StoreUtil.clearRegisterInfo(app.getPlatform().getStore());
                        break;
                    case CLEAR_STORE_DATA_TYPE_SYNC_SESSION:
                        LogUtils.i(TAG, "CLEAR_STORE_DATA_TYPE_SYNC_SESSION");
                        StoreUtil.clearSyncSession(app.getPlatform().getStore());
                        break;
                    case CLEAR_STORE_DATA_TYPE_PUBLIC_KEY:
                        LogUtils.i(TAG, "CLEAR_STORE_DATA_TYPE_PUBLIC_KEY");
                        StoreUtil.clearPublicKey(app.getPlatform().getStore());
                        break;
                    case CLEAR_STORE_DATA_TYPE_ALIAS_AND_TAG:
                        LogUtils.i(TAG, "CLEAR_STORE_DATA_TYPE_ALIAS_AND_TAG");
                        StoreUtil.clearAliasAndTag(app.getPlatform().getStore());
                        break;
                    default:
                        LogUtils.i(TAG, "CLEAR_STORE_DATA_TYPE unknown type:" + type);
                        break;
                }
            }
        });
    }

    private void doAliasAndTagRequest(){
        EebbkPush.resumePush(new OnResultListener() {
            @Override
            public void onSuccess() {
                LogUtils.i(TAG, "doAliasAndTagRequest onSuccess");
            }

            @Override
            public void onFail(String errorMsg, String errorCode) {
                LogUtils.i(TAG, "doAliasAndTagRequest onFail errorMsg:" + errorMsg + " errorCode:" + errorCode);
            }
        });
    }

    private void turnOnConnectService() {
        //TODO 回调暂时设置为Null,还存在问题
        EebbkPush.init(getApplicationContext(), null);
    }

    private void killPid(int pid) {
        if (pid != 0) {
            try {
                LogUtils.w(TAG, LogTagConfig.LOG_TAG_ERROR_SERVICE,"宿主切换","kill pid :" + pid + " pkNane:" + getApplicationContext().getPackageName());
                Process.killProcess(pid);
            } catch (Exception e) {
                LogUtils.e(TAG, LogTagConfig.LOG_TAG_ERROR_SERVICE,"宿主切换"," The pid must be error !!! ", e);
            }
        }
    }

    private void turnOffConnectService() {
        PushImplements.getPushApplicationSafely(getApplicationContext(), new OnGetCallBack<PushApplication>() {
            @Override
            public void onGet(PushApplication app) {
                app.exit();
            }
        });
    }

    public static void turnOnConnectService(Context context, int connectServicePid) {

        LogUtils.w(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE +" turn on service !!! ");

        String hostPackageName = getHostServicePackageName(context);

        if (TextUtils.isEmpty(hostPackageName)) {
            //宿主选择这一块还存在一些问题，需要再次梳理一次
            //不存在正在运行的宿主ConnectionService，So We Just 重启推送服务
            String currentPkgName = "";
            String localPkgName = context.getPackageName();
            //本地保存的宿主
            String savedHostPkgName = PublicValueStoreUtil.getHostPackageName();
            LogUtils.d(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"HostService","get running service package is not exit !!! ==>>> localPkgName="+localPkgName+"   savedHostPkgName="+savedHostPkgName);
            // 优先将ConnectionService服务挂载到存储本地保存的宿主，如果不存在则挂载自己的的 ===>>> 直接挂载自己
            if (!TextUtils.isEmpty(savedHostPkgName)) {
                currentPkgName = savedHostPkgName;
            }else
            {
                currentPkgName = localPkgName;
            }
            Intent intent = new Intent();
            intent.setPackage(currentPkgName);
            intent.setAction(SyncAction.CONNECT_SWITCH_SERVICE_ACTION);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
            intent.putExtra(ConnectSwitchService.BUNDLE_KEY_SERVICE_SWITCH, ConnectSwitchService.BUNDLE_VALUE_SERVICE_SWITCH_REINIT);
            intent.setComponent(new ComponentName(intent.getPackage(), ConnectSwitchService.class.getName()));
            ComponentName componentName = startService(context, intent);
            if (componentName != null) {
                LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE,"HostService"," == restart service successfully,service info:" + componentName.toString()+" service name :"+ ConnectSwitchService.class.getSimpleName());
            } else {
                LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE ,"HostService","== restart service failed ,service name : " + ConnectSwitchService.class.getSimpleName());
            }
            return;
        }
        Intent intent = new Intent();
        intent.setPackage(hostPackageName);
        intent.setAction(SyncAction.CONNECT_SWITCH_SERVICE_ACTION);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
        intent.putExtra(ConnectSwitchService.BUNDLE_KEY_SERVICE_SWITCH, ConnectSwitchService.BUNDLE_VALUE_SERVICE_SWITCH_ON);
        intent.putExtra(ConnectSwitchService.BUNDLE_KEY_PID, connectServicePid);
        intent.setComponent(new ComponentName(intent.getPackage(), ConnectSwitchService.class.getName()));
        ComponentName componentName = startService(context, intent);
        if (componentName != null) {
            LogUtils.i(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE ,"Start Service Success","start service successfully,service info:" + componentName.toString());
        } else {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE ,"start service failed ","start service failed,service name : " + ConnectSwitchService.class.getSimpleName());
        }
    }

    /**
     * @param context
     * @param connectServicePid ConnectService的进程PID,也就是宿主进程的pid ,没有传0！！！！
     */
    public static void turnOffConnectService(Context context, int connectServicePid) {
        LogUtils.w(TAG,LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE +" turn off service !!!");
        String servicePkgName = getHostServicePackageName(context);
        if (servicePkgName == null) {
            LogUtils.e(TAG, " servicePkgName is null ，the host service is not running now  ,so we cancel turn off !!!");
            return;
        }
        Intent intent = new Intent();
        intent.setAction(SyncAction.CONNECT_SWITCH_SERVICE_ACTION);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
        intent.putExtra(ConnectSwitchService.BUNDLE_KEY_SERVICE_SWITCH, ConnectSwitchService.BUNDLE_VALUE_SERVICE_SWITCH_OFF);
        intent.putExtra(ConnectSwitchService.BUNDLE_KEY_PID, connectServicePid);
        intent.setComponent(new ComponentName(servicePkgName, ConnectSwitchService.class.getName()));
        ComponentName componentName = startService(context, intent);
        if (componentName != null) {
            LogUtils.i(TAG, "start service successfully,service info:" + componentName.toString());
        } else {
            LogUtils.e(TAG, "start service failed ,service name : " + ConnectSwitchService.class.getSimpleName());
        }
    }

    @Nullable
    public static String getHostServicePackageName(Context context) {
        HostServiceInfo hostServiceInfo = new HostServiceInfo();
        ConnectionService.getHostServiceInfo(context, hostServiceInfo);
        // 宿主服务（正在运行的ConnectionService服务）
        String servicePkgName = hostServiceInfo.getServicePkgName();
        if (TextUtils.isEmpty(servicePkgName)) {
            LogUtils.i(TAG, "SyncPushSystemReceiver servicePkgName is empty!");
            return null;
        }
        return servicePkgName;
    }

    private static ComponentName startService(Context context, Intent intent){
        try {
            return context.startService(intent);
        } catch (Exception e) {
            LogUtils.e(TAG, LogTagConfig.LOG_TAG_FLOW_CONNECT_SERVICE," startService error !!! ", e);
        }
        return null;
    }

}
