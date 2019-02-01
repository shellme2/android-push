package com.eebbk.bfc.im.push.service.tcp;

import com.eebbk.bfc.im.push.IConnectCallback;
import com.eebbk.bfc.im.push.exception.SyncException;
import com.eebbk.bfc.im.push.exception.WriteDataException;

/**
 * 连接接口定义
 *
 * Created by lhd on 2015/8/21.
 */
public interface Connection {

    /**
     * 发起连接
     * @param hostname 主机
     * @param port 主机端口
     */
    void connect(String hostname, int port);

    /**
     * 发起连接
     * @param isAlarm 是否是闹钟(alarm)唤醒连接任务
     * @param iConnectCallback 连接aidl回调
     */
    void connect(String hostname, int port, boolean isAlarm, IConnectCallback iConnectCallback);

    void cancelConnect();

    /**
     * 发送数据
     * @param data 数据
     * @throws WriteDataException 发送数据失败将抛出异常
     */
    void send(byte[] data) throws WriteDataException;

    /**
     * 关闭并释放连接，该方法会导致连接端口并无法再次连上
     */
    void close();

    /**
     * 释放(断开)连接，该方法会导致连接断开，但是可以重连成功
     */
    void releaseConnection();

    /**
     * 判断连接是否正常连接到远程服务器
     * @return true表示正常连接，false表示断开
     */
    boolean isConnected();

    String getHostname();

    int getPort();

}
