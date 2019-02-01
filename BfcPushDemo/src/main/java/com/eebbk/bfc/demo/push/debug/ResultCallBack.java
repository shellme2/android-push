package com.eebbk.bfc.demo.push.debug;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/24 17:03
 * Email:  zengjingfang@foxmail.com
 */
public interface ResultCallBack<T> {

    void onSuccess(T t);

    void onFailed(T t);
}
