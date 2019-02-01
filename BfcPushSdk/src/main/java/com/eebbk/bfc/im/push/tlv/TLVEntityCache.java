package com.eebbk.bfc.im.push.tlv;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lhd on 2017/3/18.
 */

public class TLVEntityCache {

    private String entityName;

    private Map<Integer, Field> fieldMap = new ConcurrentHashMap<>();

    public TLVEntityCache(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void putField(int tagValue, Field field) {
        fieldMap.put(tagValue, field);
    }

    public Field getField(int tagValue) {
        return fieldMap.get(tagValue);
    }

    public int getTagValue(String fieldName) {
        for (Map.Entry<Integer, Field> entry : fieldMap.entrySet()) {
            Field field = entry.getValue();
            if (field != null && field.getName().equals(fieldName)) {
                return entry.getKey();
            }
        }
        return 0;
    }
}
