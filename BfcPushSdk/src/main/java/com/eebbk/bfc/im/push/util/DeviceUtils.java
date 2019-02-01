package com.eebbk.bfc.im.push.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

/**
 * @author hesn
 * 2018/9/19
 */
public class DeviceUtils {
    private static String MACHINE_ID;
    public static final String INVALID_MACHINE_ID = "0123456789ABCDEF";

    public static String getMachineId(Context context) {
        if(!TextUtils.isEmpty(MACHINE_ID) && !TextUtils.equals(Build.UNKNOWN, MACHINE_ID) && !TextUtils.equals(INVALID_MACHINE_ID, MACHINE_ID)){
            return MACHINE_ID;
        }
        MACHINE_ID = Build.SERIAL;
        if (TextUtils.isEmpty(MACHINE_ID) || TextUtils.equals(Build.UNKNOWN, MACHINE_ID) || TextUtils.equals(INVALID_MACHINE_ID, MACHINE_ID)) {
            MACHINE_ID = com.eebbk.bfc.common.devices.DeviceUtils.getMachineId(context);
        }
        return MACHINE_ID;
    }

    public static void clearMachineId(){
        MACHINE_ID = "";
    }
}
