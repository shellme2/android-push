package com.eebbk.bfc.im.push.tlv;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lhd on 2017/3/18.
 */

public class TLVCache {

    private static List<TLVEncoderCache> tlvEncoderCacheList = new ArrayList<>();

    private static List<TLVDecoderCache> tlvDecoderCacheList = new ArrayList<>();

    private static Map<String, TLVEntityCache> tlvEntityCacheMap = new ConcurrentHashMap<>();


    public static TLVEncodeResult getTLVEncodeResult(int frameType, int dataType, int tagValue, byte[] value) {
        synchronized (tlvEncoderCacheList) {
            for (TLVEncoderCache tlvEncoderCache : tlvEncoderCacheList) {
                TLVEncodeResult tlvEncodeResult = tlvEncoderCache.get(frameType, dataType, tagValue, value);
                if (tlvEncodeResult != null) {
                    return tlvEncodeResult;
                }
            }
        }
        return null;
    }

    public static void addTlvEncoderCache(int frameType, int dataType, int tagValue, byte[] value, TLVEncodeResult tlvEncodeResult) {
        if (value == null || value.length == 0 || tlvEncodeResult == null) {
            return;
        }
        TLVEncoderCache tlvEncoderCache = new TLVEncoderCache(frameType, dataType, tagValue, value, tlvEncodeResult);
        synchronized (tlvEncoderCacheList) {
            while (tlvEncoderCacheList.size() > 20) {
                tlvEncoderCacheList.remove(0);
            }
            tlvEncoderCacheList.add(tlvEncoderCache);
        }
    }

    public static TLVDecodeResult getTLVDecodeResult(byte[] tlvBytes) {
        synchronized (tlvDecoderCacheList) {
            for (TLVDecoderCache tlvDecoderCache : tlvDecoderCacheList) {
                TLVDecodeResult tlvDecodeResult = tlvDecoderCache.get(tlvBytes);
                if (tlvDecodeResult != null) {
                    return tlvDecodeResult;
                }
            }
        }
        return null;
    }

    public static void addTlvDecoderCache(byte[] tlvBytes, TLVDecodeResult tlvDecodeResult) {
        if (tlvBytes == null || tlvBytes.length == 0 || tlvDecodeResult == null) {
            return;
        }
        TLVDecoderCache tlvDecoderCache = new TLVDecoderCache(tlvBytes, tlvDecodeResult);
        synchronized (tlvDecoderCacheList) {
            while (tlvDecoderCacheList.size() > 20) {
                tlvDecoderCacheList.remove(0);
            }
            tlvDecoderCacheList.add(tlvDecoderCache);
        }
    }

    public static void addTlvEntityCache(String entityName, int tagValue, Field field) {
        if (TextUtils.isEmpty(entityName) || tagValue == 0 || field == null) {
            return;
        }
        TLVEntityCache tlvEntityCache = tlvEntityCacheMap.get(entityName);
        if (tlvEntityCache == null) {
            tlvEntityCache = new TLVEntityCache(entityName);
            tlvEntityCacheMap.put(entityName, tlvEntityCache);
        }
        tlvEntityCache.putField(tagValue, field);
    }

    public static Field getField(String entityName, int tagValue) {
        if (TextUtils.isEmpty(entityName) || tagValue == 0) {
            return null;
        }
        TLVEntityCache tlvEntityCache = tlvEntityCacheMap.get(entityName);
        if (tlvEntityCache != null) {
            return tlvEntityCache.getField(tagValue);
        }
        return null;
    }

    public static int getTagValue(String entityName, String fieldName) {
        if (TextUtils.isEmpty(entityName) || TextUtils.isEmpty(fieldName)) {
            return 0;
        }
        TLVEntityCache tlvEntityCache = tlvEntityCacheMap.get(entityName);
        if (tlvEntityCache != null) {
            return tlvEntityCache.getTagValue(fieldName);
        }
        return 0;
    }

    public static void clear() {
        synchronized (tlvEncoderCacheList) {
            tlvEncoderCacheList.clear();
        }
        synchronized (tlvDecoderCacheList) {
            tlvDecoderCacheList.clear();
        }
        tlvEntityCacheMap.clear();
    }
}
