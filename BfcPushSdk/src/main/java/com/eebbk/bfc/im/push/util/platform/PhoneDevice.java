package com.eebbk.bfc.im.push.util.platform;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.eebbk.bfc.common.devices.DeviceUtils;
import com.eebbk.bfc.im.push.code.MD5Util;
import com.eebbk.bfc.im.push.util.IDUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 手机设备信息获取类
 */
public class PhoneDevice implements Device {
    private static final String TAG = "PhoneDevice";

    private Context context;

    public PhoneDevice(Context context) {
        this.context = context;
    }

    @Override
    public String getModelNumber() {
        return Build.MODEL;
    }

    @Override
    public int getSysSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    @Override
    public String getAndroidOsVersion() {
        return Build.VERSION.RELEASE;
    }

    @Override
    public String getScreenResolution() {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        String resolution = metric.widthPixels + "*" + metric.heightPixels;
        return resolution;
    }

    @Override
    public String getMacAddress() {
        String mac = null;

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            LogUtils.e(TAG, "获取Mac地址异常："+e);
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface iF = interfaces.nextElement();
            byte[] addr = new byte[0];
            try {
                addr = iF.getHardwareAddress();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            if (addr == null || addr.length == 0) {
                continue;
            }
            StringBuilder buf = new StringBuilder();
            for (byte b : addr) {
                buf.append(String.format("%02X:", b));
            }
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
             mac = buf.toString();
        }
        return mac;
    }

    @Override
    public String getDeviceId() {
        String deviceId ;
        // 如果有机器序列号的，用机器序列号作为DeviceId
        deviceId = DeviceUtils.getMachineId(context);
        if(!TextUtils.isEmpty(deviceId) && !TextUtils.equals(deviceId, DeviceUtils.DEFAULT_MACHINE_ID)){
            return MD5Util.md5(0 + deviceId);
        }
        String mac = getMacAddress();
        String imei = getImei();
        if (mac != null && imei != null) {
            // md5(platform + mac + imei +
            // randUUID)，randUUID由每个客户端生成（由于部分客户端无法得到mac和imei，因此只能自己生成唯一识别码）
            // 并保存在本地，建议多保存几个地方，避免被删除，当能获取到mac，imei信息时，不添加UUID。
            deviceId = MD5Util.md5(0 + mac + imei);
        } else if(mac != null){
            deviceId = MD5Util.md5(0 + mac);
        } else if(imei != null){
            deviceId = MD5Util.md5(0 + imei);
        } else{
            deviceId = readDeviceId();
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = MD5Util.md5(IDUtil.getUUID());
            }
            saveDeviceId(deviceId);
        }
        return deviceId;
    }

    private void saveDeviceId(String deviceId) {
        DataStore dataStore = DataStore.getInstance(context);
        SharedPreferences.Editor editor = dataStore.edit();
        editor.putString(DEVICE_ID_KEY, deviceId);
        editor.apply();
    }

    /**
     * 清除保存在本地的deviceId
     */
    public boolean clearDeviceId() {
        DataStore dataStore = DataStore.getInstance(context);
        SharedPreferences.Editor editor = dataStore.edit();
        editor.remove(DEVICE_ID_KEY);
        return editor.commit();
    }

    private String readDeviceId() {
        DataStore dataStore = DataStore.getInstance(context);
        String deviceId = dataStore.getString(DEVICE_ID_KEY, null);
        LogUtils.i("deviceId:" + deviceId);
        return deviceId;
    }

    @Override
    public String getImei() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    @Override
    public String getImsi() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }

    @Override
    public String getAndroidSysName() {
        return Build.MODEL + " " + Build.VERSION.RELEASE;
    }

    @Override
    public String getBaseBandVersion() {
        String result = null;
        try {
            Class<?> cl = Class.forName("android.os.SystemProperties");
            Object invoker = cl.newInstance();
            Method m = cl.getMethod("get", new Class[]{String.class, String.class});
            result = m.invoke(invoker, new Object[]{"gsm.version.baseband", "no message"}).toString();
        } catch (ClassNotFoundException e) {
            LogUtils.e(e);
        } catch (InvocationTargetException e) {
            LogUtils.e(e);
        } catch (NoSuchMethodException e) {
            LogUtils.e(e);
        } catch (InstantiationException e) {
            LogUtils.e(e);
        } catch (IllegalAccessException e) {
            LogUtils.e(e);
        }
        return result;
    }

    @Override
    public String getAppKeyFromMetaData() {
        ApplicationInfo appInfo ;
        String appKey = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            appKey = appInfo.metaData.getString("SYNC_APP_KEY");

            LogUtils.i("meta-data app key:" + appKey);
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        }
        if (appKey == null) {
            LogUtils.e( TAG, "appKey is null.");
        }
        return appKey;
    }

    @Override
    public String getBuildNumber() {
        return Build.DISPLAY;
    }

    @Override
    public Integer getRidTagFromMetaData() {
        ApplicationInfo appInfo ;
        Integer ridTag = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            ridTag = appInfo.metaData.getInt("SYNC_RID_TAG");
            LogUtils.i("meta-data rid_tag:" + ridTag);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(e);
        }
        if (ridTag == null) {
            throw new IllegalArgumentException("you must set SYNC_RID_TAG in AndroidManifest.xml");
        }
        return ridTag;
    }
}
