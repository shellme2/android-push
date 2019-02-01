package com.eebbk.bfc.im.push.entity;

public interface FetchOrder {

    /**
     * 读取syncKey之后的pageSize条数据
     */
    int FOLLOW = 1;

    /**
     * 读取会话最新的pageSize条数据
     */
    int LAST = 2;

    /**
     * 读取历史消息数据，传输的syncKey之前的pagesize数，不包含起始值
     */
    int HISTORY = 3;
}
