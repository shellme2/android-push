package com.eebbk.bfc.im.push.voice;

import com.eebbk.bfc.im.push.entity.voice.VoiceSliceEntity;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 语音切片接收器
 * 描述一个语音切片响应数据
 */
public class SliceReceiver {

    /**
     * 创建时间
     */
    private long createAt;

    private Long dialogId;

    private Long accountId;

    private Long registId;


    private Map<Integer, VoiceSliceEntity> sliceMap = new HashMap<>();

    public SliceReceiver(Long dialogId, Long accountId, Long registId) {
        this.dialogId = dialogId;
        this.accountId = accountId;
        this.registId = registId;
        createAt = System.currentTimeMillis();
    }

    public void clear() {
        sliceMap.clear();
    }

    /**
     * 3分钟超时
     *
     * @return
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - createAt > 60 * 1000 * 3;
    }

    public void addVoiceSliceEntity(VoiceSliceEntity sliceEntity) {
        LogUtils.d("add a voice slice entity:" + sliceEntity);
        sliceMap.put(sliceEntity.getIndex(), sliceEntity);
    }

    /**
     * 构建完整的语音数据
     *
     * @param lastIndex 最后一个语音切片的索引
     * @return 完整的语音字节数据
     */
    protected byte[] buildVoice(int lastIndex) {
        if (lastIndex < 1) {
            return null;
        }

        byte[] fullVoice = null;
        if (sliceMap.size() == lastIndex) {
            // 拼接所有的语音切片
            fullVoice = sliceMap.get(1).getVoc();
            for (int i = 2; i <= lastIndex; i++) {
                fullVoice = append(fullVoice, sliceMap.get(i).getVoc());
            }
        }
        if (fullVoice == null || fullVoice.length == 0) {
            LogUtils.e("receiver info:" + this.toString() + ",lastIndex:" + lastIndex);
        }
        return fullVoice;
    }

    /**
     * 向一个byte数组后面追加byte数组
     * @param org
     * @param to
     * @return
     */
    public byte[] append(byte[] org, byte[] to) {
        if (org == null || org.length == 0
                || to == null || to.length == 0) {
            return null;
        }
        byte[] newByte = new byte[org.length + to.length];
        System.arraycopy(org, 0, newByte, 0, org.length);
        System.arraycopy(to, 0, newByte, org.length, to.length);

        return newByte;
    }

    @Override
    public String toString() {
        return "SliceReceiver{" +
                "accountId=" + accountId +
                ", registId=" + registId +
                ", sliceMap=" + sliceMap +
                '}';
    }
}
