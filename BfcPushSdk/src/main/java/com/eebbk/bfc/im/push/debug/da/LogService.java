package com.eebbk.bfc.im.push.debug.da;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.listener.OnPushStatusListener;
import com.eebbk.bfc.im.push.util.JsonUtil;

/**
 * 日志回调
 * 家长管理市场反馈较多，分析问题需要日志协助
 * 由于推送日志会在不同进程底下，且长连接进程不固定，所以所有日志都传到家长管理中，统一保存加密上报管理，并且可以统一开关
 * @author hesn
 * 2018/8/30
 */
public class LogService extends IntentService {
    private static final String TAG = "LogService";

    public LogService() {
        super(TAG);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null || !Da.isReportLog){
            return;
        }
        synchronized (TAG){
            try {
                LogInfo info = JsonUtil.fromJson(intent.getStringExtra("log"), LogInfo.class);
                if(info != null){
                    PushApplication.getInstance().callBackPushStatus(OnPushStatusListener.Status.LOG, info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
