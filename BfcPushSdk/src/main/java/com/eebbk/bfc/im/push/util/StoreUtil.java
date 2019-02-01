package com.eebbk.bfc.im.push.util;

import android.text.TextUtils;

import com.eebbk.bfc.im.push.bean.AliasAndTagsInfo;
import com.google.gson.reflect.TypeToken;
import com.eebbk.bfc.im.push.bean.AppPushInfo;
import com.eebbk.bfc.im.push.bean.SyncRegistInfo;
import com.eebbk.bfc.im.push.util.platform.Store;

import java.util.ArrayList;
import java.util.List;

/**
 * 指定数据保存
 */
public class StoreUtil {

    private static final String REGIST_INFO_KEY = "regist_info";

    private static final String PUBLIC_KEY_KEY = "public_key";

    private static final String SYNC_SESSION_KEY = "sync_session";

    private static final String APP_PUSH_INFO_KEY = "app_push_info_key";

    private static final String ALIAS_TAGS_INFO_KEY="alias_tags_info_key";

    private static final String IS_STOP_PUSH_KEY="is_stop_push_key";

    //构造函数私有，防止恶意新建
    private StoreUtil(){}

    public static void saveRegistInfo(Store store, SyncRegistInfo syncRegistInfo) {
        long registId = syncRegistInfo.getRegistId();
        String registToken = syncRegistInfo.getRegistToken();
        if (registId == 0 || TextUtils.isEmpty(registToken)) {
            LogUtils.e("registId == 0 or registToken is empty.");
            return;
        }
        String saveJson = GsonUtil.toJSON(syncRegistInfo);
        store.put(REGIST_INFO_KEY, saveJson);
        LogUtils.i("save registInfo finished,regist_info:" + saveJson);
    }

    public static void saveRegistInfo(Store store, boolean login) {
        String json = store.getString(REGIST_INFO_KEY);
        SyncRegistInfo syncRegistInfo = GsonUtil.fromJSON(json, SyncRegistInfo.class);
        if (syncRegistInfo == null || syncRegistInfo.getRegistId() == 0 || TextUtils.isEmpty(syncRegistInfo.getRegistToken())) {
            LogUtils.e("save login [" + login + "] to regist_info fail:" + syncRegistInfo);
            return;
        }
        syncRegistInfo.setLogin(login);
        store.put(REGIST_INFO_KEY, GsonUtil.toJSON(syncRegistInfo));
        LogUtils.i("save login [" + login + "] to regist_info success:" + GsonUtil.toJSON(syncRegistInfo));
    }

    public static SyncRegistInfo readRegistInfo(Store store) {
        String json = store.getString(REGIST_INFO_KEY);
        SyncRegistInfo syncRegistInfo = GsonUtil.fromJSON(json, SyncRegistInfo.class);
        if (syncRegistInfo == null) {
            syncRegistInfo = SyncRegistInfo.EMPTY_SYNC_REGISTINFO;
        }
        LogUtils.i("read syncRegistInfo:" + GsonUtil.toJSON(syncRegistInfo));
        return syncRegistInfo;
    }

    public static void savePublicKey(Store store, byte[] publicKey) {
        if (publicKey == null || publicKey.length == 0) {
            LogUtils.e("public key is empty.");
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
            LogUtils.e("appPushInfo pkgName is empty.");
            return;
        }
        String json = store.getString(APP_PUSH_INFO_KEY);
        List<AppPushInfo> list = GsonUtil.fromJSON(json, new TypeToken<List<AppPushInfo>>(){}.getType());
        if (list == null) {
            list = new ArrayList<>();
        }
        int index = list.indexOf(appPushInfo);
        if (index != -1) {
            list.set(index, appPushInfo);
        } else {
            list.add(appPushInfo);
        }
        store.put(APP_PUSH_INFO_KEY, GsonUtil.toJSON(list));
        LogUtils.i("save app push info finish:" + GsonUtil.toJSON(list));
    }

    public static void removeAppPushInfo(Store store, AppPushInfo appPushInfo) {
        String json = store.getString(APP_PUSH_INFO_KEY);
        List<AppPushInfo> list = GsonUtil.fromJSON(json, new TypeToken<List<AppPushInfo>>(){}.getType());
        if (list == null) {
            return;
        }
        int index = list.indexOf(appPushInfo);
        if (index != -1) {
            list.remove(index);
        }
        store.put(APP_PUSH_INFO_KEY, GsonUtil.toJSON(list));
        LogUtils.i("remove app push info finish:" + GsonUtil.toJSON(list));
    }

    public static List<AppPushInfo> readAppPushInfo(Store store) {
        String json = store.getString(APP_PUSH_INFO_KEY);
        List<AppPushInfo> list = GsonUtil.fromJSON(json, new TypeToken<List<AppPushInfo>>(){}.getType());
        LogUtils.i("read app push info:" + list);
        return list;
    }


    public static void clearDeviceInfo(Store store) {
        clearRegistInfo(store);
        clearSyncSession(store);
        clearPublicKey(store);
    }

    public static void clearRegistInfo(Store store) {
        if (store.remove(REGIST_INFO_KEY)) {
            LogUtils.d("clear regist info data success.");
        } else {
            LogUtils.e("clear regist info data fail.");
        }
    }

    public static void clearSyncSession(Store store) {
        if (store.remove(SYNC_SESSION_KEY)) {
            LogUtils.d("clear sync session data success.");
        } else {
            LogUtils.e("clear sync session data fail.");
        }
    }

    public static void clearPublicKey(Store store) {
        if (store.remove(PUBLIC_KEY_KEY)) {
            LogUtils.d("clear public key data success.");
        } else {
            LogUtils.e("clear public key data fail.");
        }
    }

    public static void saveAliasAndTag(Store store,AliasAndTagsInfo aliasAndTagsInfo){
        if (aliasAndTagsInfo == null || TextUtils.isEmpty(aliasAndTagsInfo.getAlias())) {
            LogUtils.e("save AliasAndTag fail:" + aliasAndTagsInfo);
            return;
        }
        store.put(ALIAS_TAGS_INFO_KEY, GsonUtil.toJSON(aliasAndTagsInfo));
        LogUtils.i("save AliasAndTag success:" + GsonUtil.toJSON(aliasAndTagsInfo));
    }

    public static AliasAndTagsInfo readAliasAndTag(Store store){
        String json = store.getString(ALIAS_TAGS_INFO_KEY);
        AliasAndTagsInfo aliasAndTagsInfo = GsonUtil.fromJSON(json, AliasAndTagsInfo.class);
        if (aliasAndTagsInfo == null) {
            aliasAndTagsInfo = AliasAndTagsInfo.EMPTY_ALIASANDTAGSINFO;
        }
        LogUtils.i("read AliasAndTagInfo:" + GsonUtil.toJSON(aliasAndTagsInfo));
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

}
