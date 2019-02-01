package com.eebbk.bfc.im.push.listener;

public interface OnFinishListener {

    /**
     * 上传成功
     */
    void onSuccess(Long syncKey);

    /**
     * 上传失败
     */
    void onFailed();
}
