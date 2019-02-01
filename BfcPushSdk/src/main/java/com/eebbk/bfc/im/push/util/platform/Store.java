package com.eebbk.bfc.im.push.util.platform;

/**
 * 设备数据保存
 */
public interface Store {

    void put(String key, int value);

    void put(String key, long value);

    void put(String key, String value);

    void put(String key, Object obj);

    void put(String key, boolean value);

    int getInt(String key);

    long getLong(String key);

    boolean getBoolean(String key);

    String getString(String key);

    Object getObject(String key, Class<?> cls);

    boolean remove(String key);

    boolean clear();
}
