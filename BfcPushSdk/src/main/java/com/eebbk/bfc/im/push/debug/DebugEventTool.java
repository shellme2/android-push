package com.eebbk.bfc.im.push.debug;

import android.content.Context;
import android.content.Intent;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/19 18:02
 * Email:  zengjingfang@foxmail.com
 */
public class DebugEventTool extends DebugBaseTool{

    public final static String PUSH_DEBUG_ON = "PUSH_DEBUG_ON";

    public final static String PUSH_DEBUG_OFF = "PUSH_DEBUG_OFF";

    public final static String DEBUG_PUSH_EVENT_PUBLISH_ACTION = "com.eebbk.bfc.im.debug_push_event_publish_action";

    public final static String DEBUG_PUSH_EVENT_PUBLISH_KEY = "DEBUG_PUSH_EVENT_PUBLISH_KEY";

    public static boolean pushDebugMode = false;

    private static Context mContext;

    public static DebugEventTool sTool;

    public static DebugEventTool init(Context context) {
        if (sTool == null || mContext == null) {
            sTool = null;
            sTool = new DebugEventTool(context);
        }
        return sTool;
    }

    public static DebugEventTool getInstance() {
        if (sTool == null) {
            //保险起见
            DLog.e(TAG, "error !!!");
            sTool = new DebugEventTool();
        }
        return sTool;
    }
    private DebugEventTool() {
        super("DebugEventTool_Error");
    }

    public DebugEventTool(Context context) {
        super("DebugEventTool");
        mContext = context;
    }

    @Override
    public void destroy() {
        mContext = null;
        sTool = null;
        super.destroy();
    }

    @Override
    protected void onHandleMessage(Object obj) {
        DebugEventInfo debugEventInfo = (DebugEventInfo) obj;
        Intent intent = new Intent(DEBUG_PUSH_EVENT_PUBLISH_ACTION);
        intent.putExtra(DEBUG_PUSH_EVENT_PUBLISH_KEY, debugEventInfo);
        if (mContext != null) {
            mContext.sendBroadcast(intent);
            DLog.i(TAG, "publish this event " + debugEventInfo.toString());
        }else {
            DLog.e(TAG, " context is null !!!");
        }
    }

    public static boolean setPushMode(String pushModeMsg) {
        if (PUSH_DEBUG_ON.equals(pushModeMsg)) {
            pushDebugMode = true;
            DLog.i(TAG, "turn on debug mode !!!");
        } else if (PUSH_DEBUG_OFF.equals(pushModeMsg)) {
            DLog.i(TAG, "turn off debug mode !!!");
            pushDebugMode = false;
        }else {
            pushDebugMode = false;
            DLog.e(TAG, "set push mode error !!!");
        }
        return pushDebugMode;
    }

    public  void event(long time, String process, int code, String content, String extend) {
        if (pushDebugMode) {
            DebugEventInfo debugEventInfo = new DebugEventInfo(time, process, code, content, extend);
            sendMessage(debugEventInfo);
        }else {
            DLog.e(TAG, " pushDebugMode is false !!!");
        }
    }

    
    


}
