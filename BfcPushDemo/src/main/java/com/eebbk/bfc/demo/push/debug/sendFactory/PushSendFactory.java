package com.eebbk.bfc.demo.push.debug.sendFactory;

import android.os.Build;
import android.text.TextUtils;

import com.eebbk.bfc.demo.push.Constant;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/21 9:59
 * Email:  zengjingfang@foxmail.com
 */
public class PushSendFactory {
    private static String ALIASES;
    private static final AtomicInteger ID = new AtomicInteger();

    public static class PushSendDemoFactory implements IPushSendProduct {

        @Override
        public PushSendProduct createPushSendProduct() {
            int id = ID.incrementAndGet();
            return new DebugPushSendBasic( id, Constant.SendRelease.URL, getAliases()
                    , Constant.SendRelease.APP_NAME, getContent(id, Constant.SendRelease.CONTENT_TYPE), Constant.SendRelease.OFF_LINE);
        }
    }


    public static class PushSendDemoTestFactory implements IPushSendProduct {

        @Override
        public PushSendProduct createPushSendProduct() {
            int id = ID.incrementAndGet();
            return new DebugPushSendBasic( id, Constant.SendDebug.URL, getAliases()
                    , Constant.SendDebug.APP_NAME, getContent(id, Constant.SendDebug.CONTENT_TYPE), Constant.SendDebug.OFF_LINE);
        }
    }
    public static class PushSendDemoONFactory implements IPushSendProduct {

        @Override
        public PushSendProduct createPushSendProduct() {
            int id = ID.incrementAndGet();
            return new DebugPushSendBasic( id, Constant.SendRelease.URL, getAliases()
                    , Constant.SendRelease.APP_NAME, Constant.SendRelease.DEBUG_ON , Constant.SendRelease.OFF_LINE);
        }
    }
    public static class PushSendDemoOFFFactory implements IPushSendProduct {

        @Override
        public PushSendProduct createPushSendProduct() {
            int id = ID.incrementAndGet();
            return new DebugPushSendBasic( id, Constant.SendRelease.URL, getAliases()
                    , Constant.SendRelease.APP_NAME, Constant.SendRelease.DEBUG_OFF , Constant.SendRelease.OFF_LINE);
        }
    }
    public static class PushSendDemoTestONFactory implements IPushSendProduct {

        @Override
        public PushSendProduct createPushSendProduct() {
            int id = ID.incrementAndGet();
            return new DebugPushSendBasic( id, Constant.SendDebug.URL, getAliases()
                    , Constant.SendDebug.APP_NAME, Constant.SendDebug.DEBUG_ON , Constant.SendDebug.OFF_LINE);
        }
    }
    public static class PushSendDemoTestOFFFactory implements IPushSendProduct {

        @Override
        public PushSendProduct createPushSendProduct() {
            int id = ID.incrementAndGet();
            return new DebugPushSendBasic( id, Constant.SendDebug.URL, getAliases()
                    , Constant.SendDebug.APP_NAME, Constant.SendDebug.DEBUG_OFF, Constant.SendDebug.OFF_LINE);
        }
    }

    public static class PushSendSynchineseFactory implements IPushSendProduct {

        @Override
        public PushSendProduct createPushSendProduct() {
            int id = ID.incrementAndGet();
            return new DebugPushSendBasic( id, Constant.SendRelease.URL, getAliases()
                    , "Synchinese", "hello"+ id, Constant.SendRelease.OFF_LINE);
        }
    }

    private static String getContent(int id, String type){
        return type  + "_" +  id + "_" + System.currentTimeMillis() + "_" + "麻麻哈哈";
    }

    private static String getAliases(){
        if(TextUtils.isEmpty(ALIASES)){
            ALIASES = Build.SERIAL;
        }
        return ALIASES;
    }

}
