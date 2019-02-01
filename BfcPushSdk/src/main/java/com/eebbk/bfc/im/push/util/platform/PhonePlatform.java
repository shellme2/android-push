package com.eebbk.bfc.im.push.util.platform;

import android.content.Context;

/**
 * 手机平台
 */
public class PhonePlatform extends Platform {

    private static PhonePlatform phonePlatform;

    public PhonePlatform(Context context) {
        device = new PhoneDevice(context);
        store = new PhoneStore(context);
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public Store getStore() {
        return store;
    }
}
