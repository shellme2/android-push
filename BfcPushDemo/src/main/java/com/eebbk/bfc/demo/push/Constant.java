package com.eebbk.bfc.demo.push;

/**
 * @author hesn
 *         2017/12/8
 */

public class Constant {

    public interface SendBase{
        String DEBUG_ON = "PUSH_DEBUG_ON";

        String DEBUG_OFF = "PUSH_DEBUG_OFF";

        String OFF_LINE = "1";
    }

    public interface SendRelease extends SendBase{
        String URL = "http://push.eebbk.net/im_push/api/push/sendMessage";

        String APP_NAME = "bfcpushdemo1";

        String CONTENT_TYPE = "bfcpushdemotest";
    }

    public interface SendDebug extends SendBase{
        String URL = "http://test.eebbk.net/im_push/api/push/sendMessage";

        String APP_NAME = "pushtest";

        String CONTENT_TYPE = "pushtest";
    }
}
