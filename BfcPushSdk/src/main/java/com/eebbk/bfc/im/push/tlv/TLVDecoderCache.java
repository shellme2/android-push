package com.eebbk.bfc.im.push.tlv;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lhd on 2017/3/18.
 */

public class TLVDecoderCache {

    private byte[] tlvBytes;

    private TLVDecodeResult tlvDecodeResult;

    private AtomicInteger hitCount;

    public TLVDecoderCache() {
        hitCount = new AtomicInteger(0);
    }

    public TLVDecoderCache(byte[] tlvBytes, TLVDecodeResult tlvDecodeResult) {
        this.tlvBytes = tlvBytes;
        this.tlvDecodeResult = tlvDecodeResult;
        hitCount = new AtomicInteger(0);
    }

    public TLVDecodeResult get(byte[] tlvBytes) {
        if (tlvBytes != null && tlvBytes.length > 0 && Arrays.equals(tlvBytes, this.tlvBytes)) {
            hitCount.incrementAndGet();
            return tlvDecodeResult;
        }
        return null;
    }

    public int getHitCount() {
        return hitCount.get();
    }
}
