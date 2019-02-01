package com.eebbk.bfc.im.push.debug.da;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.util.JsonUtil;

/**
 * 埋点工具
 * @author hesn
 * 2018/7/3
 */
public class Da {
    public static boolean isReport = false;
    public static boolean isReportLog = true;

    public static void record(Context context, DaInfo info){
        if(context == null){
            return;
        }
        if(isReport){
            // 由于目前主要是分析家长管理推送问题，统一把埋点放到家长管理中
            // 1.方便对埋点上报管理
            // 2.保证埋点数据不会散落到各个app，导致上报不及时
            // 3.方便出现推送异常后，如果埋点没有及时上报，可以触发家长管理埋点上报后门进行上报
            Intent intent = getIntent(context, info);
            intent.setComponent(new ComponentName(constant.DA_SERVICE_PACKAGE_NAME, DaService.class.getName()));
            try {
                ComponentName componentName = context.startService(intent);
                if(componentName == null){
                    // 如果启动不了家长管理的，就启动自身的
                    Intent currIntent = getIntent(context, info);
                    currIntent.setClass(context, DaService.class);
                    context.startService(currIntent);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(isReportLog){
            Intent intent = getLogIntent(context, info);
            intent.setComponent(new ComponentName(constant.DA_SERVICE_PACKAGE_NAME, LogService.class.getName()));
            try {
                context.startService(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static Intent getLogIntent(Context context, DaInfo info){
        LogInfo logInfo = new LogInfo(context, info);
        Intent intent = new Intent();
        intent.setAction(SyncAction.LOG_SERVICE_ACTION);
        intent.putExtra("log", JsonUtil.toJson(logInfo));
        return intent;
    }

    private static Intent getIntent(Context context, DaInfo info){
        Intent intent = new Intent();
        intent.setAction(SyncAction.DA_SERVICE_ACTION);
        intent.putExtra(constant.bundleKey.FUNCTION_NAME, info.getFunctionName());
        intent.putExtra(constant.bundleKey.MODULE_DETAIL, info.getModuleDetail());
        intent.putExtra(constant.bundleKey.TRIG_VALUE, info.getTrigValue());
        intent.putExtra(constant.bundleKey.EXTEND, info.getExtendJson(context));
        return intent;
    }

    public interface functionName {
        String INIT = "初始化";
        String REQUEST = "请求";
        String RESPONSE = "响应";
        String NET_STATUS_CHANGED = "网络变化";
        String BOOT = "开关机";
        String DUPLICATE_MSG = "重复推送消息";
        String TIME_OUT = "请求超时";
        String TLV_OOM = "tlv oom";
        String SOCKET_STATUS = "长连接状态改变";
        String CHECK_HOST = "检查是否需要切换宿主";
        String ELECT_HOST = "宿主改变";
    }

    public interface trigValue {
        String CONNECTED = "连接成功";
        String CONNECTING = "开始连接";
        String DISCONNECT = "断开";
        String BOOT = "开";
        String SHUTDOWN = "关";
    }

    public interface extend {
        String package_name = "pkg";
        String PID = "pid";
        String SDK_VERSION = "sdkVer";
        String IP = "ip";
        String PORT = "port";
    }

    interface constant {

        String MODULE_NAME = "bfc-push";
        String MODULE_PACKAGE_NAME = "com.eebbk.bfc.im";
        String DA_COLUMNS_PACKAGE_NAME =  "packageName";
        String DA_COLUMNS_APP_VERSION =  "appVer";

        String DA_SERVICE_PACKAGE_NAME = "com.eebbk.parentsupport";

        interface bundleKey {
            String FUNCTION_NAME = "functionName";
            String MODULE_DETAIL = "moduleDetail";
            String TRIG_VALUE = "trigValue";
            String EXTEND = "extend";
        }
    }
}
