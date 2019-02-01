package com.eebbk.bfc.im.push.util.platform;

/**
 * 设备平台.
 */
public abstract class Platform  {

    protected Device device;

    protected Store store;

    public abstract Device getDevice();

    public abstract Store getStore();
}
