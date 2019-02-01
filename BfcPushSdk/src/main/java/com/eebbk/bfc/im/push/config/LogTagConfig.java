package com.eebbk.bfc.im.push.config;

/**
 * Desc:   自定义的一些TAG,拼接到Log的msg里边，方便过滤查找Log(相对关键的LOG)
 * Author: ZengJingFang
 * Time:   2017/3/10 16:19
 * Email:  zengjingfang@foxmail.com
 */

public interface LogTagConfig {

    //--------------------流程--------------------------

    /**
     * 初始化相关             初始化
     */
    String LOG_TAG_FLOW_PUSH_INIT = "LOG_TAG_FLOW_PUSH_INIT";
    /**
     * 长连接相关             ConnectionService服务
     */
    String LOG_TAG_FLOW_CONNECT_SERVICE = "LOG_TAG_FLOW_CONNECT_SERVICE";
    /**
     * 长连接相关            TCP长连接
     */
    String LOG_TAG_FLOW_CONNECT_TCP = "LOG_TAG_FLOW_CONNECT_TCP";
    /**
     * 公钥
     */
    String LOG_TAG_FLOW_PUBLIC_KEY = "LOG_TAG_FLOW_PUBLIC_KEY";
    /**
     * 设置加密
     */
    String LOG_TAG_FLOW_SET_ENCRYPT = "LOG_TAG_FLOW_SET_ENCRYPT";
    /**
     * 设备注册
     */
    String LOG_TAG_FLOW_REGISTER = "LOG_TAG_FLOW_REGISTER";
    /**
     * 设备登录
     */
    String LOG_TAG_FLOW_LOGIN = "LOG_TAG_FLOW_LOGIN";
    /**
     * 别名设置
     */
    String LOG_TAG_FLOW_SET_ALIAS = "LOG_TAG_FLOW_SET_ALIAS";
    /**
     * 同步触发
     */
    String LOG_TAG_FLOW_SEND_TRIGGER = "LOG_TAG_FLOW_SEND_TRIGGER";
    /**
     * 心跳相关
     */
    String LOG_TAG_FLOW_HEARTBEAT = "LOG_TAG_FLOW_HEARTBEAT";



    //--------------------功能点--------------------------
    /**
     * 服务相关
     * 1、选择宿主服务
     * 2、绑定宿主服务
     */
    String LOG_TAG_POINT_SERVICE = "LOG_TAG_POINT_SERVICE";


    String LOG_TAG_POINT_TCP = "LOG_TAG_POINT_TCP";

    String LOG_TAG_POINT_SOCKET = "LOG_TAG_POINT_SOCKET";
    /**
     * 心跳相关
     * 1、稳定心跳
     * 2、每一次心跳
     */
    String LOG_TAG_POINT_HEARTBEAT = "LOG_TAG_POINT_HEARTBEAT";
    /**
     * 广播接收相关          外部事件
     */
    String LOG_TAG_POINT_RECEIVER = "LOG_TAG_POINT_RECEIVER";
    /**
     * 推送消息发送相关
     */
    String LOG_TAG_POINT_PUSH_MSG_SEND = "LOG_TAG_POINT_PUSH_MSG_SEND";

    /**
     * 推送消息接收相关
     */
    String LOG_TAG_POINT_PUSH_MSG_GET = "LOG_TAG_POINT_PUSH_MSG_GET";



    //--------------------异常点--------------------------
    /**
     * 服务相关
     */
    String LOG_TAG_ERROR_SERVICE = "LOG_TAG_ERROR_SERVICE";

    /**
     * 网络相关
     */
    String LOG_TAG_ERROR_NET = "LOG_TAG_ERROR_NET";

    /**
     * TCP连接相关
     */
    String LOG_TAG_ERROR_TCP = "LOG_TAG_ERROR_TCP";

    /**
     * key相关
     */
    String LOG_TAG_ERROR_KEY = "LOG_TAG_ERROR_KEY";

    //---------------------临时调试--------------------------
    /**
     * 临时调试
     */
    String LOG_TAG_ST = "LOG_TAG_ST \n ";

    String LOG_TAG_IO = "LOG_TAG_IO";


}
