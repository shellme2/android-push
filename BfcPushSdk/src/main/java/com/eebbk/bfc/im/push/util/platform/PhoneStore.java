package com.eebbk.bfc.im.push.util.platform;

import android.content.Context;
import android.content.SharedPreferences;

import com.eebbk.bfc.im.push.util.LogUtils;

/**
 * 手机数据保存仓库
 */
public class PhoneStore implements Store {

    private DataStore dataStore;

    public PhoneStore(Context context) {
        dataStore = DataStore.getInstance(context);
    }

    @Override
    public void put(String key, int value) {
        SharedPreferences.Editor editor = dataStore.edit();
        editor.putInt(key, value);
        boolean commit = editor.commit();
        LogUtils.d("commit:" + commit);
    }

    @Override
    public void put(String key, long value) {
        SharedPreferences.Editor editor = dataStore.edit();
        editor.putLong(key, value);
        boolean commit = editor.commit();
        LogUtils.d("commit:" + commit);
    }

    @Override
    public void put(String key, boolean value) {
        SharedPreferences.Editor editor = dataStore.edit();
        editor.putBoolean(key, value);
        boolean commit = editor.commit();
        LogUtils.d("commit:" + commit);
    }

    @Override
    public void put(String key, String value) {
        SharedPreferences.Editor editor = dataStore.edit();
        editor.putString(key, value);
        boolean commit = editor.commit();
        LogUtils.d("commit:" + commit);
    }

    @Override
    public void put(String key, Object obj) {
        dataStore.putObject(key, obj);
    }

    @Override
    public int getInt(String key) {
        return dataStore.getInt(key, 0);
    }

    @Override
    public long getLong(String key) {
        return dataStore.getLong(key, 0);
    }

    @Override
    public boolean getBoolean(String key) {
        return dataStore.getBoolean(key, false);
    }

    @Override
    public String getString(String key) {
        return dataStore.getString(key, null);
    }

    @Override
    public Object getObject(String key, Class<?> cls) {
        return dataStore.getObject(key, cls);
    }

    @Override
    public boolean remove(String key) {
        SharedPreferences.Editor editor = dataStore.edit();
        editor.remove(key);
        return editor.commit();
    }

    @Override
    public boolean clear() {
        SharedPreferences.Editor editor = dataStore.edit();
        editor.clear();
        return  editor.commit();
    }
}
