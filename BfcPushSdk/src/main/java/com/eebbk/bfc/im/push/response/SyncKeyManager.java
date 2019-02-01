package com.eebbk.bfc.im.push.response;

import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 同步序号管理器
 * 管理各个会话对应的同步序号值，供同步请求使用
 */
public class SyncKeyManager {
    private static final String TAG = "SyncKeyManager";

    private Map<String, Long> pushLocalSyncKeyMap = new ConcurrentHashMap<>();

    private Map<String, Long> pushServerSyncKeyMap = new ConcurrentHashMap<>();

    private PushApplication app;

    public SyncKeyManager(PushApplication app) {
        this.app = app;
    }

    public void clear() {
        pushLocalSyncKeyMap.clear();
        pushServerSyncKeyMap.clear();
    }

    public void putPushLocalSyncKey(String pgkName, String alias, long syncKey) {
        if (TextUtils.isEmpty(pgkName) || TextUtils.isEmpty(alias)) {
            LogUtils.e(TAG,  "pgkName:" + pgkName + ",alias:" + alias);
            return;
        }
        String key = String.valueOf(pgkName + "_" + alias);
        pushLocalSyncKeyMap.put(key, syncKey);
    }

    public Long getPushLocalSyncKey(String pgkName, String alias) {
        Long syncKey = null;
        String key = String.valueOf(pgkName + "_" + alias);
        syncKey = pushLocalSyncKeyMap.get(key);
        if (syncKey == null) {
            syncKey = Long.valueOf(1);
            putPushLocalSyncKey(pgkName, alias, syncKey);
        }
        return syncKey;
    }

    public void putPushServerSyncKey(String pkgName, String alias, long syncKey) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(alias)) {
            LogUtils.e(TAG,   "pkgName:" + pkgName + ",alias:" + alias);
            return;
        }
        String key = String.valueOf(pkgName + "_" + alias);
        pushServerSyncKeyMap.put(key, syncKey);
    }

    public Long getPushServerSyncKey(String pkgName, String alias) {
        Long syncKey;
        String key = String.valueOf(pkgName + "_" + alias);
        syncKey = pushServerSyncKeyMap.get(key);
        if (syncKey == null) {
            syncKey = Long.valueOf(1);
            putPushLocalSyncKey(pkgName, alias, syncKey);
        }
        return syncKey;
    }

}
