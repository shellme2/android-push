package com.eebbk.bfc.im.push.voice;

import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语音切片接收管理器
 */
public class SliceReceiverManager {

    /**
     * 当前接收到的所有语音切片
     */
    private Map<String, SliceReceiver> receiveMap = new ConcurrentHashMap<>();

    public SliceReceiverManager() {

    }

    /**
     * 保存一个语音切片接收器
     *
     * @param groupId 语音组id
     * @param sliceReceiver 语音切片接收器
     */
    public void putSliceReceiver(String groupId, SliceReceiver sliceReceiver) {
        LogUtils.d("put a slice receiver,groupId:" + groupId);
        receiveMap.put(groupId, sliceReceiver);
    }

    /**
     * 获取一个语音切片接收器
     *
     * @param groupId
     * @return
     */
    public SliceReceiver getSliceReceiver(String groupId) {
        return receiveMap.get(groupId);
    }


    /**
     * 构建完整语音
     *
     * @param groupId
     * @param lastIndex
     * @return
     */
    public byte[] buildVoice(String groupId, int lastIndex) {
        byte[] fullVoice = null;
        SliceReceiver sliceReceiver = getSliceReceiver(groupId);
        if (sliceReceiver != null) {
            fullVoice = sliceReceiver.buildVoice(lastIndex);
        } else {
            LogUtils.e("sliceReceiver is null,groupId:" + groupId);
        }

        if (fullVoice != null && fullVoice.length > 0) {
            // 构建完语音后，移除
            // 避免累计大量未处理的切片接收器
            SliceReceiver remove = receiveMap.remove(groupId);
            if (remove != null) {
                remove.clear();
                LogUtils.d("remove a slice receiver,groupId:" + groupId);
            }
        } else {
            LogUtils.e("buildVoice error:fullVoice is null.");
        }

        return fullVoice;
    }
}
