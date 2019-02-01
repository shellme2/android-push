package com.eebbk.bfc.im.push.listener;

/**
 * IM SDK初始化监听
 */
public interface OnInitSateListener {
    void onSuccess();
    void onFail(String errorMsg);
}
