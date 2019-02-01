package com.eebbk.bfc.im.push.debug.da;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.sdk.behavior.aidl.BfcBehaviorAidl;
import com.eebbk.bfc.sdk.behavior.aidl.listener.OnServiceConnectionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author hesn
 * 2018/7/3
 */
public class DaService extends Service {

    public static final String TAG = "DaService";
    private BfcBehaviorAidl mBfcBehaviorAidl;
    private List<DaInfo> mCache = new ArrayList<>();
    private static final int BIND_STATE_IDLE = 0;
    private static final int BIND_STATE_CONNECTING = 1;
    private static final int BIND_STATE_CONNECTED = 2;
    private final AtomicInteger mBindState = new AtomicInteger(BIND_STATE_IDLE);
    private final AtomicLong mDaId = new AtomicLong();

    @Override
    public void onCreate() {
        super.onCreate();
        mBfcBehaviorAidl = new BfcBehaviorAidl.Builder()
                .setOnServiceConnectionListener(mListener)
                .setModuleName(Da.constant.MODULE_NAME)
                .build(null);
        mBfcBehaviorAidl.putAttr(Da.constant.DA_COLUMNS_PACKAGE_NAME, Da.constant.MODULE_PACKAGE_NAME);
        mBfcBehaviorAidl.putAttr(Da.constant.DA_COLUMNS_APP_VERSION, com.eebbk.bfc.im.push.version.Build.VERSION.VERSION_NAME);
        bindDaAidl();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBfcBehaviorAidl.unbindService(getApplicationContext());
        mBfcBehaviorAidl.destroy();
        mBfcBehaviorAidl = null;
        mBindState.set(BIND_STATE_IDLE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null || !Da.isReport){
            return super.onStartCommand(intent, flags, startId);
        }
        synchronized (TAG){
            String functionName = intent.getStringExtra(Da.constant.bundleKey.FUNCTION_NAME);
            String moduleDetail = intent.getStringExtra(Da.constant.bundleKey.MODULE_DETAIL);
            String trigValue = intent.getStringExtra(Da.constant.bundleKey.TRIG_VALUE);
            String extend = intent.getStringExtra(Da.constant.bundleKey.EXTEND);
            LogUtils.e(TAG, "functionName:" + functionName
//                    + "\nmoduleDetail:" + moduleDetail
                    + "\ntrigValue:" + trigValue
                    + "\nextend:" + extend);
            if(mBfcBehaviorAidl.isConnectionService()){
                mBfcBehaviorAidl.customEvent(null, functionName, moduleDetail, trigValue, extend,
                        String.valueOf(mDaId.getAndIncrement()), null, null, null, null, null, null, null);
            }else {
                mCache.add(new DaInfo()
                        .setFunctionName(functionName)
                        .setModuleDetail(moduleDetail)
                        .setTrigValue(trigValue)
                        .setExtend(extend));
                bindDaAidl();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void bindDaAidl(){
        if(mBindState.get() == BIND_STATE_IDLE){
            mBindState.set(BIND_STATE_CONNECTING);
            mBfcBehaviorAidl.bindService(getApplicationContext());
        }
    }

    private OnServiceConnectionListener mListener = new OnServiceConnectionListener() {
        @Override
        public void onConnected() {
            mBindState.set(BIND_STATE_CONNECTED);
            if(mCache.size() == 0){
                return;
            }
            for (DaInfo info : mCache) {
                mBfcBehaviorAidl.clickEvent(null, info.getFunctionName(),
                        info.getModuleDetail(), info.getExtend());
            }
            mCache.clear();
        }

        @Override
        public void onDisconnected() {
            mBindState.set(BIND_STATE_IDLE);
        }
    };
}
