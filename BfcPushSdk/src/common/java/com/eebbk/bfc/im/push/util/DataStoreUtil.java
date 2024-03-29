package com.eebbk.bfc.im.push.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据持久化工具,可将List,对象以及基本数据类型持久化到文件中保存
 *
 * Created by lhd on 2015/09/26.
 */
public class DataStoreUtil implements SharedPreferences {

    private String TAG = "DataStoreUtil";

    private SharedPreferences sharedPreferences;

    private volatile static DataStoreUtil dataStoreUtil;

    private DataStoreUtil(Context context) {
        int mode = Activity.MODE_PRIVATE;
        if(Build.VERSION.SDK_INT > 11) {
            mode = Activity.MODE_MULTI_PROCESS;
        }
        sharedPreferences = context.getSharedPreferences("com.xtc.sync", mode);
    }

    /**
     * 线程安全
     *
     * @param context
     * @return
     */
    public static DataStoreUtil getInstance(Context context) {
        if (dataStoreUtil == null) {
            synchronized (DataStoreUtil.class) {
                if (dataStoreUtil == null) {
                    dataStoreUtil = new DataStoreUtil(context);
                }
            }
        }
        return dataStoreUtil;
    }

    /**
     * 持久化对象
     *
     * @param <T>
     * @param key
     * @param t
     * @return
     */
    public <T> boolean putObject(String key, T t) {
        boolean result = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(t);
            String objectDataString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            if (sharedPreferences != null) {
                Editor editor = this.edit();
                editor.putString(key, objectDataString);
                result = editor.commit();
            } else {
                Log.e(TAG, "sharedPreferences is null.");
                result = false;
            }
        } catch (IOException e) {
            LogUtils.e(e);
        } finally {
            try {
                baos.close();
                if (oos != null)
                    oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取持久化对象
     *
     * @param key
     * @param cls
     * @return
     */
    public <T> T getObject(String key, Class<T> cls) {
        String data = sharedPreferences.getString(key, null);
        T t = null;
        if (data == null)
            return null;
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(data, Base64.DEFAULT));
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            t = (T) ois.readObject();
        } catch (StreamCorruptedException e) {
            LogUtils.e(e);
        } catch (IOException e) {
            LogUtils.e(e);
        } catch (ClassNotFoundException e) {
            LogUtils.e(e);
        } finally {
            try {
                bais.close();
                if (ois != null)
                    ois.close();
            } catch (IOException e) {
                LogUtils.e(e);
            }
        }
        return t;
    }

    /**
     * 持久化列表List
     *
     * @param key
     * @param list
     * @return
     */
    public <T> boolean putList(String key, List<T> list) {
        return putObject(key, list);
    }

    /**
     * 获取持久化列表List
     *
     * @param key
     * @param t
     * @return
     */
    public <T> List<T> getList(String key, Class<T> t) {
        String data = sharedPreferences.getString(key, null);
        List<T> l = null;
        if (data == null)
            return null;
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(data, Base64.DEFAULT));
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            l = (List<T>) ois.readObject();
        } catch (StreamCorruptedException e) {
            LogUtils.e(e);
        } catch (IOException e) {
            LogUtils.e(e);
        } catch (ClassNotFoundException e) {
            LogUtils.e(e);
        } finally {
            try {
                bais.close();
                if (ois != null)
                    ois.close();
            } catch (IOException e) {
                LogUtils.e(e);
            }
        }
        return l;
    }

    @Override
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    @Override
    public String getString(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    @SuppressLint("NewApi")
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        return sharedPreferences.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return sharedPreferences.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return sharedPreferences.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return sharedPreferences.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    @Override
    public Editor edit() {
        return sharedPreferences.edit();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
