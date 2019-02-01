// IConnectionService.aidl
package com.eebbk.bfc.im.push;

import com.eebbk.bfc.im.push.IConnectCallback;
import com.eebbk.bfc.im.push.bean.SyncRegistInfo;
// Declare any non-default types here with import statements

interface IConnectionService {

    /**
    * 连接IM服务器
    */
    void connect(String hostname, int port, IConnectCallback iConnectCallback);

    /**
    * 把发送任务压入任务队列
    */
    void enqueueSendTask(in long sendTime, in byte[] data, int timeout);

    /**
    * 心跳包
    */
    void heartbeat();

    /**
    * 获取registId
    */
    SyncRegistInfo getSyncRegisitInfo();

    /**
    * 判断设备是否登录
    */
    boolean isLogined();

    /**
    * 关闭TCP连接并停止服务
    */
    void close();

    /**
    *连接是否关闭
    */
    boolean isClosed();

    /**
    * 增加服务器预埋ip信息
    */
    void addBackupServerInfo(in String[] serverInfo, boolean clearBefore);

    /**
    * 清除预埋ip信息
    */
    void clearBackupServerInfo();

    /**
    * 获取连接主机
    */
    String getHostname();

    /**
    * 获取连接端口
    */
    int getPort();

    /**
    * 是否存在公钥
    */
    boolean hasPublicKey();

    void setHeartbeatPeriod(int minHeart, int maxHeart, int heartStep);
}
