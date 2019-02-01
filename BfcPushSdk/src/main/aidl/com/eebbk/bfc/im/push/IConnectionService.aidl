// IConnectionService.aidl
package com.eebbk.bfc.im.push;

import com.eebbk.bfc.im.push.IConnectCallback;
import com.eebbk.bfc.im.push.bean.SyncRegisterInfo;
import com.eebbk.bfc.im.push.bean.AppPushInfo;
import com.eebbk.bfc.im.push.bean.AppBindInfo;
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
    SyncRegisterInfo getSyncRegisterInfo();

    /**
    * 判断设备是否登录
    */
    boolean isLogin();

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

    String getHostSDKVersion();

    void setBindAppInfo(in AppBindInfo appBindInfo);

    AppBindInfo getBindAppInfo();

    /**
    * 宿主真实绑定的app列表
    * 此接口和getBindAppInfo()区别：由于getBindAppInfo()添加了用于宿主切换的setBindAppInfo()接口，
    * 如果是老版本的推送库，不兼容宿主切换的（如4.0.5-bugfix），就会导致老版本app没切过来新宿主，但是却通过
    * setBindAppInfo()接口把信息绑定过来，导致的虚假信息。
    */
    AppBindInfo getCurrBindAppInfo();


}
