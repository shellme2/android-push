package com.eebbk.bfc.im.push.communication;

public interface SyncAction {

    String TAG = "com.eebbk.bfc.im";

    /**
     * 设备连上网络
     */
    String NETWORK_CONNECTED_ACTION = TAG + ".network.connected";

    /**
     * 设备关机
     */
    String DEVICE_SHUTDOWN_ACTION = TAG + ".device.shutdown";

    /**
     * 收到推送数据广播
     */
    String READ_DATA_ACTION = TAG + ".read_data";

    /**
     * 推送成功连接
     */
    String SYNC_CONNECTED_ACTION = TAG + ".connected";

    /**
     * 推送断开连接
     */
    String SYNC_DISCONNECTED_ACTION = TAG + ".disconnected";

    /**
     * 推送连接中
     */
    String SYNC_CONNECTING_ACTION = TAG + ".connecting";

    /**
     * 同步请求响应数据广播
     */
    String SYNC_RESPONSE_ACTION = TAG + ".sync_response";

    /**
     * TCP连接失败广播，未使用
     */
    String SYNC_CONNECT_FAIL_ACTION = TAG + ".connect_fail";

    /**
     * 心跳包广播
     */
    String HEARTBEAT_REQUEST = TAG + ".heartbeat";

    /**
     * 停止推送连接
     */
    String STOP_CONN_SERVICE_ACTION = TAG + ".stop_conn_service";

    /**
     * 设备登录
     */
    String PUSH_LOGIN_ACTION = TAG + ".push.login";

    /**
     * 检查宿主service
     */
    String PUSH_HOST_SERVICE_CHECK = ".push.host_service.check";

    /**
     * 宿主切换更新通知
     */
    String PUSH_HOST_SERVICE_UPDATE = ".push.host_service.update";

    /**
     * 推送相关信息搜集
     */
    String PUSH_INFO_COLLECTION = ".push.info.collection";

    /**
     * kill push进程
     */
    String KILL_PUSH_PROCESS = ".kill.push_process";

    /**
     * 重启服务
     */
    String RESTART_SERVICE_ACTION=TAG+".wake_action";

    String CONNECT_SWITCH_SERVICE_ACTION =TAG+".connect_switch_service";

    /**
     * 埋点服务
     */
    String DA_SERVICE_ACTION = TAG + ".da_service";

    /**
     * log服务
     */
    String LOG_SERVICE_ACTION = TAG + ".log_service";
}
