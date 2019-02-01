package com.eebbk.bfc.im.push.util;

import android.os.Environment;

import com.eebbk.bfc.im.push.SDKVersion;

import java.lang.reflect.Method;

public class PublicValueStoreUtil {

    //构造函数私有，防止恶意新建
    private PublicValueStoreUtil() {
    }

    //    private static final String HOST_PACKAGE_NAME="com.eebbk.bfc.im.host_package_name";
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.push";
    private static final String FILE_NAME = "value.txt";
    private static final String PUSH_VERSION = "version.txt";
    private static final String SYSTEM_SAVE_CLASS = "android.ovum.OvumManager";
    private static final String SYSTEM_SET_METHOD = "setPushHostPackageName";
    private static final String SYSTEM_GET_METHOD = "getPushHostPackageName";

    public static boolean putHostPackageName(String value) {
        //写入版本号 方便以后处理宿主同步升级问题
        String version = SDKVersion.getSdkInfo();
        FileUtil.writeData2SDCard(FILE_PATH, PUSH_VERSION, version);
        boolean ws = FileUtil.writeData2SDCard(FILE_PATH, FILE_NAME, value);
        savePushHostPackageName4System(value);
        return ws;
    }

    public static String getHostPackageName() {
        return FileUtil.readData2SDCard(FILE_PATH, FILE_NAME);
    }

    /**
     * 为系统保存宿主包名
     * <p>
     *     此功能5.0.11-bugfix加入。
     *     系统要做一个强杀的功能，由于eebbk.push是免杀进程，如果多个app集成不同版本的推送库等原因，会造成多个eebbk.push进程，
     *     此保存就是告诉系统真实宿主是谁，然后其他的app的eebbk.push和该app都可以杀
     * </p>
     * @param packageName
     */
    public static void savePushHostPackageName4System(String packageName){
        try {
            Class<?> cls = Class.forName(SYSTEM_SAVE_CLASS);
            Method set = cls.getDeclaredMethod(SYSTEM_SET_METHOD, String.class);
            set.setAccessible(true);
            set.invoke(cls.newInstance(), packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPushHostPackageName4System(){
        try {
            Class<?> cls = Class.forName(SYSTEM_SAVE_CLASS);
            Method get = cls.getDeclaredMethod(SYSTEM_GET_METHOD);
            get.setAccessible(true);
            return  (String) get.invoke(cls.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
