package com.eebbk.bfc.im.push.tlv;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lhd on 2017/3/18.
 */

public class TLVEncoderCache {

    private int frameType;

    private int dataType;

    private int tagValue;

    private byte[] value;

    private TLVEncodeResult tlvEncodeResult;

    private AtomicInteger hitCount;

    public TLVEncoderCache() {
        hitCount = new AtomicInteger(0);
    }

    public TLVEncoderCache(int frameType, int dataType, int tagValue, byte[] value, TLVEncodeResult tlvEncodeResult) {
        this.frameType = frameType;
        this.dataType = dataType;
        this.tagValue =tagValue;
        this.value = value;
        this.tlvEncodeResult = tlvEncodeResult;
        hitCount = new AtomicInteger(0);
    }

    public TLVEncodeResult get(int frameType, int dataType, int tagValue, byte[] value) {
        if (frameType == this.frameType && dataType == this.dataType
                && tagValue == this.tagValue && Arrays.equals(value, this.value)) {
            hitCount.incrementAndGet();
            return tlvEncodeResult;
        }
        return null;
    }

    public int getHitCount() {
        return hitCount.get();
    }
}
