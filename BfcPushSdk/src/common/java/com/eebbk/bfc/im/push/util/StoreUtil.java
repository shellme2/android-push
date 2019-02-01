package com.eebbk.bfc.im.push.util;

import android.content.Context;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.bean.AliasAndTagsInfo;
import com.eebbk.bfc.im.push.bean.SyncRegisterInfo;
import com.eebbk.bfc.im.push.bean.AppPushInfo;
import com.eebbk.bfc.im.push.util.platform.Store;
import com.eebbk.bfc.json.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * 指定数据保存
 */
public class StoreUtil {
    private static final String TAG = "StoreUtil";

    private static final String REGISTER_INFO_KEY = "register_info";

    private static final String PUBLIC_KEY_KEY = "public_key";

    private static final String SYNC_SESSION_KEY = "sync_session";

    private static final String APP_PUSH_INFO_KEY = "app_push_info_key";

    private static final String ALIAS_TAGS_INFO_KEY="alias_tags_info_key";

    private static final String IS_STOP_PUSH_KEY="is_stop_push_key";

    private static final String HUAWEI_PUSH_TOKEN_KEY = "huawei_push_token_key";

    //构造函数私有，防止恶意新建
    private StoreUtil(){}

    public static void saveRegisterInfo(Store store, SyncRegisterInfo syncRegisterInfo) {
        long registerId = syncRegisterInfo.getRegisterId();
        String registerToken = syncRegisterInfo.getRegisterToken();
        if (registerId == 0 || TextUtils.isEmpty(registerToken)) {
            LogUtils.e(TAG,"registerId == 0 or registerToken is empty.");
            return;
        }
        checkRegisterInfo(store, syncRegisterInfo);
        String saveJson = JsonUtil.toJson(syncRegisterInfo);
        store.put(REGISTER_INFO_KEY, saveJson);
        LogUtils.i("save register Info finished,register_info:" + saveJson);
    }

    public static void saveRegisterInfo(Store store, boolean login) {
        String json = store.getString(REGISTER_INFO_KEY);
        SyncRegisterInfo syncRegisterInfo = JsonUtil.fromJson(json, SyncRegisterInfo.class);
        if (syncRegisterInfo == null || syncRegisterInfo.getRegisterId() == 0 || TextUtils.isEmpty(syncRegisterInfo.getRegisterToken())) {
            LogUtils.e(TAG,"save login [" + login + "] to register_info fail:" + syncRegisterInfo);
            return;
        }
        syncRegisterInfo.setLogin(login);
        store.put(REGISTER_INFO_KEY, JsonUtil.toJson(syncRegisterInfo));
        LogUtils.i("save login [" + login + "] to register_info success:" + JsonUtil.toJson(syncRegisterInfo));
    }

    public static SyncRegisterInfo readRegisterInfo(Store store) {
        String json = store.getString(REGISTER_INFO_KEY);
        SyncRegisterInfo syncRegisterInfo = JsonUtil.fromJson(json, SyncRegisterInfo.class);
        if (syncRegisterInfo == null) {
            syncRegisterInfo = SyncRegisterInfo.EMPTY_SYNC_REGISTER_INFO;
        }
        LogUtils.i("read syncRegisterInfo:" + JsonUtil.toJson(syncRegisterInfo));
        return syncRegisterInfo;
    }

    /**
     * 检查注册信息是否改变，如果registerId改变了，需要重新触发 SetAliasAndTagRequest 才能上线成功
     */
    public static void checkRegisterInfo(Store store, SyncRegisterInfo syncRegisterInfo){
        long registerId = syncRegisterInfo.getRegisterId();
        if (registerId == 0) {
            LogUtils.e( TAG, "checkRegisterInfo() registerId == 0 or registerToken is empty.");
            return;
        }
        SyncRegisterInfo currSyncRegisterInfo = readRegisterInfo(store);
        if (registerId == currSyncRegisterInfo.getRegisterId()){
            LogUtils.e( TAG, "registerId unchange.");
            return;
        }
        LogUtils.e( TAG, "registerId change");
        clearAliasAndTag(store);
    }

    public static void savePublicKey(Store store, byte[] publicKey) {
        if (publicKey == null || publicKey.length == 0) {
            LogUtils.e("","public key is empty.");
            return;
        }
        store.put(PUBLIC_KEY_KEY, new String(publicKey));
        LogUtils.i("save public key success:" + new String(publicKey));
    }

    public static byte[] readPublicKey(Store store) {
        String publicKey = store.getString(PUBLIC_KEY_KEY);
        if (TextUtils.isEmpty(publicKey)) {
            return null;
        }
        LogUtils.i("read publicKey:" + publicKey);
        return publicKey.getBytes();
    }

    public static void saveAppPushInfo(Store store, AppPushInfo appPushInfo) {
        if (TextUtils.isEmpty(appPushInfo.getPkgName())) {
            LogUtils.e(TAG,"appPushInfo pkgName is empty.");
            return;
        }
        String json = store.getString(APP_PUSH_INFO_KEY);
        List<AppPushInfo> list = JsonUtil.fromJson(json, new TypeToken<List<AppPushInfo>>(){}.getType());
        if (list == null) {
            list = new ArrayList<>();
        }
        int index = list.indexOf(appPushInfo);
        if (index != -1) {
            list.set(index, appPushInfo);
        } else {
            list.add(appPushInfo);
        }
        store.put(APP_PUSH_INFO_KEY, JsonUtil.toJson(list));
        LogUtils.i("save app push info finish:" + JsonUtil.toJson(list));
    }

    public static void removeAppPushInfo(Store store, AppPushInfo appPushInfo) {
        String json = store.getString(APP_PUSH_INFO_KEY);
        List<AppPushInfo> list = JsonUtil.fromJson(json, new TypeToken<List<AppPushInfo>>(){}.getType());
        if (list == null) {
            return;
        }
        int index = list.indexOf(appPushInfo);
        if (index != -1) {
            list.remove(index);
        }
        store.put(APP_PUSH_INFO_KEY, JsonUtil.toJson(list));
        LogUtils.i("remove app push info finish:" + JsonUtil.toJson(list));
    }

    public static List<AppPushInfo> readAppPushInfo(Store store) {
        String json = store.getString(APP_PUSH_INFO_KEY);
        List<AppPushInfo> list = JsonUtil.fromJson(json, new TypeToken<List<AppPushInfo>>(){}.getType());
        LogUtils.i("read app push info:" + list);
        return list;
    }


    public static void clearDeviceInfo(Store store) {
        clearRegisterInfo(store);
        clearSyncSession(store);
        clearPublicKey(store);
        clearAliasAndTag(store);
    }

    public static void clearRegisterInfo(Store store) {
        if (store.remove(REGISTER_INFO_KEY)) {
            LogUtils.d("clear register info data success.");
        } else {
            LogUtils.e(TAG,"clear register info data fail.");
        }
    }

    public static void clearSyncSession(Store store) {
        if (store.remove(SYNC_SESSION_KEY)) {
            LogUtils.d("clear sync session data success.");
        } else {
            LogUtils.e(TAG,"clear sync session data fail.");
        }
    }

    public static void clearPublicKey(Store store) {
        if (store.remove(PUBLIC_KEY_KEY)) {
            LogUtils.d("clear public key data success.");
        } else {
            LogUtils.e(TAG,"clear public key data fail.");
        }
    }

    public static void clearAliasAndTag(Store store) {
        if (store.remove(ALIAS_TAGS_INFO_KEY)) {
            LogUtils.d("clear alias and tag data success.");
        } else {
            LogUtils.e( TAG, "clear alias and tag data fail.");
        }
    }

    public static void saveAliasAndTag(Store store,AliasAndTagsInfo aliasAndTagsInfo){
        if (aliasAndTagsInfo == null || TextUtils.isEmpty(aliasAndTagsInfo.getAlias())) {
            LogUtils.e(TAG,"save AliasAndTag fail:" + aliasAndTagsInfo);
            return;
        }
        store.put(ALIAS_TAGS_INFO_KEY, JsonUtil.toJson(aliasAndTagsInfo));
        LogUtils.i("save AliasAndTag success:" + JsonUtil.toJson(aliasAndTagsInfo));
    }

    public static AliasAndTagsInfo readAliasAndTag(Store store){
        String json = store.getString(ALIAS_TAGS_INFO_KEY);
        AliasAndTagsInfo aliasAndTagsInfo = JsonUtil.fromJson(json, AliasAndTagsInfo.class);
        if (aliasAndTagsInfo == null) {
            aliasAndTagsInfo = AliasAndTagsInfo.EMPTY_ALIAS_TAGS_INFO;
        }
        LogUtils.i("read AliasAndTagInfo:" + JsonUtil.toJson(aliasAndTagsInfo));
        return aliasAndTagsInfo;
    }

    public static boolean readIsStopPush(Store store){
        Boolean isStopPush = store.getBoolean(IS_STOP_PUSH_KEY);
        LogUtils.i("read isStopPush:" + isStopPush);
        return isStopPush;
    }

    public static void saveIsStopPush(Store store, boolean isStopPush){
        store.put(IS_STOP_PUSH_KEY, isStopPush);
        LogUtils.i("save isStopPush key success:" + isStopPush);
    }

    public static void saveHuaWeiPushToken(Context context, String token) {
        if (!TextUtils.isEmpty(token)) {
            DataStoreUtil.getInstance(context).edit().putString(HUAWEI_PUSH_TOKEN_KEY, token).apply();
            LogUtils.i("save huawei push token success:" + token);
        }
    }

    public static String readHuaWeiPushToken(Context context) {
        String token = DataStoreUtil.getInstance(context).getString(HUAWEI_PUSH_TOKEN_KEY, null);
        LogUtils.i("read huawei push token:" + token);
        return token;
    }
}
