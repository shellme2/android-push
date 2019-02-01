package com.eebbk.bfc.im.push.service.task;

public interface TaskType {

    String TAG = "TaskType";

    int DEFAULT = 0;
    int HEART_BEAT = 1; // 心跳
    int ALARM_CONNECT = 2; // 闹钟唤醒连接
    int APP_REMOVED = 3; // app卸载
    int DEBUG_MODE = 4; // 日志开关

}
