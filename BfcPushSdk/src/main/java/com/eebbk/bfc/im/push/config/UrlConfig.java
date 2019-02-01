package com.eebbk.bfc.im.push.config;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2016/12/28 10:12
 * Email:  zengjingfang@foxmail.com
 */

public class UrlConfig {

    public static  String sHostNameDef = "gw.im.imoo.com";
    public static  int sPortDef = 1883;


    public static void setDebugMode(boolean isDebug) {
        if (isDebug) {
//            sHostNameDef = "gw.test.im.eebbk.com";
            sHostNameDef = "testgw.im.okii.com";
            sPortDef = 8000;
        }else{
            sHostNameDef = "gw.im.imoo.com";
            sPortDef = 1883;
        }
    }


}
