package com.eebbk.bfc.im.push.error;

/**
 * 规则说明：
 * <p>1，项目代号：04</p>
 * <p>2，模块代号：01~FF</p>
 * <p>3，类代号：0001~FFFF</p>
 */
public class ErrorCode {

    private static final String PROJECT="04";

    private static final String IM_MODULE="01";
    private static final String PUSH_MODULE="02";
    private static final String SERVICE_MODULE="03";



    public static final String[] ERROR_CODE=new String[]{
            PROJECT+PUSH_MODULE+"0001"
    };

    public static final String[] ERROR_CODE_NAME=new String[]{

    };

    public static final String[] ERROR_CODE_DESCRIBE=new String[]{
            "启动服务失败",
            "绑定服务失败"
    };
}
