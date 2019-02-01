package com.eebbk.bfc.im.push.util;


import com.eebbk.bfc.im.push.entity.Entity;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.tlv.ReqRespRelationship;
import com.eebbk.bfc.im.push.tlv.TLVCache;
import com.eebbk.bfc.im.push.tlv.TLVDecodeResult;
import com.eebbk.bfc.im.push.tlv.TLVDecoder;
import com.eebbk.bfc.im.push.tlv.TLVEncoder;
import com.eebbk.bfc.im.push.tlv.TLVObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;


/**
 * TLV格式转换工具
 *
 * Created by lhd on 2015/8/22.
 */
public class TLVObjectUtil {

    private static final String TAG = "TLVObjectUtil";

    /**
     * 创建心跳包tlv格式的
     *
     * @param tagValue
     * @return
     */
    public static byte[] createHeartBeatByteArray(int tagValue){
        TLVObject tlvObj = new TLVObject();
        try {
            tlvObj.put(tagValue, (byte[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tlvObj.toByteArray();
    }

    /**
     * 把实体转化为TLV格式的对象
     *
     * @param entity
     * @return
     */
    public static TLVObject parseTLVObject(Entity entity) {
        TLVObject tlvObj = new TLVObject();
        try {
            tlvObj.put(entity.getCommand(), parseTLVObjectImpl(entity));
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        }
        return tlvObj;
    }

    public static byte[] parseByteArray(Entity entity) {
        return parseTLVObject(entity).toByteArray();
    }

    private static TLVObject parseTLVObjectImpl(Entity entity)
            throws Exception {
        TLVObject tlvObj = new TLVObject();
        Field[] requestFields = entity.getClass().getDeclaredFields();
        for (Field targetField : requestFields) {
            int tagValue = entity.getTagValue(targetField.getName());
            putValueToTLVObject(tagValue, targetField, entity, tlvObj);
        }
        return tlvObj;
    }

    /**
     * 把值put进TLV对象中，事实上是把每个值都转化成一个TLV对象再拼接到一个总的父TLV对象中
     *
     * @param targetField
     * @param entity
     * @param tlvObj
     */
    private static void putValueToTLVObject(int tagValue, Field targetField, Entity entity,
                                            TLVObject tlvObj) throws Exception {
        targetField.setAccessible(true);
        Class<?> valueClsType = targetField.getType();
        Object valueObj = targetField.get(entity);
        if (valueObj == null) {
            LogUtils.w(TAG, "formatdata error(value is null),name:"
                    + targetField.getName() + ",type:" + targetField.getType());
            return;
        }
        if (valueClsType == int.class) {
            int value = (int) valueObj;
            if (value != 0) {
                tlvObj.put(tagValue, value);
            } else {
                LogUtils.w(TAG, "formatdata error(" + valueObj.getClass().getSimpleName() + " value is 0):"
                        + targetField.getName());
            }
        } else if (valueClsType == long.class) {
            long value = (long) valueObj;
            if (value != 0) {
                tlvObj.put(tagValue, value);
            } else {
                LogUtils.w(TAG, "formatdata error(" + valueObj.getClass().getSimpleName() + " value is 0):"
                        + targetField.getName());
            }
        } else if (valueClsType == String.class) {
            tlvObj.put(tagValue, valueObj.toString());
        } else if (valueClsType == byte[].class) {
            tlvObj.put(tagValue, (byte[]) valueObj);
        } else {

            if (valueObj instanceof Entity) {
                tlvObj.put(tagValue, parseTLVObject((Entity) valueObj));
            } else {
                LogUtils.w(TAG, "formatdata error(unsupport type " + valueObj.getClass().getName() + "):"
                        + targetField.getName());
            }
        }
    }

    public static ResponseEntity parseResponseEntity(byte[] data) throws Throwable {
        return (ResponseEntity) parseEntity(data);
    }

    public static RequestEntity parseRequestEntity(byte[] data) throws Throwable {
        return (RequestEntity) parseEntity(data);
    }

    public static Entity parseEntity(byte[] data) throws Throwable {
        TLVDecodeResult result = TLVDecoder.decode(data);
        Entity entity = parseEntity(result, ReqRespRelationship.COMMAND_CLASS_MAP.get(result.getTagValue()));
        return entity;
    }

    public static <T> T parseEntity(byte[] data, Class<?> cls) throws Throwable {
        TLVDecodeResult result = TLVDecoder.decode(data);
        return parseEntity(result, cls);
    }

    private static <T> T parseEntity(TLVDecodeResult result, Class<?> cls) throws Throwable {
        Entity entity = (Entity) cls.newInstance();
        parseEntityImpl(result, entity);
        return (T) entity;
    }

    private static void parseEntityImpl(TLVDecodeResult result, Entity entity) throws NoSuchFieldException, IllegalAccessException {
        if (result.getDataType() == TLVEncoder.ConstructedData) {
            List<TLVDecodeResult> list = (List<TLVDecodeResult>) result.getValue();
            initEntityFieldValue(list, entity);
        } else {
            initEntityFieldValueImpl(result, entity);
        }
    }

    private static void initEntityFieldValue(List<TLVDecodeResult> list, Entity entity) throws IllegalAccessException, NoSuchFieldException {
        if (list == null) {
            return;
        }
        for (TLVDecodeResult r : list) {
            initEntityFieldValueImpl(r, entity);
        }
    }

    private static void initEntityFieldValueImpl(TLVDecodeResult r, Entity entity) throws IllegalAccessException, NoSuchFieldException {
        Field field = getFieldByTagValueFromEntity(r, entity);
        if (field != null) {
            setEntityFieldValue(r, entity, field);
        }
    }

    private static Field getFieldByTagValueFromEntity(TLVDecodeResult r, Entity entity) throws NoSuchFieldException {
        Field tagValueField = null;
        tagValueField = TLVCache.getField(entity.getClass().getName(), r.getTagValue());
        if (tagValueField != null) {
            return tagValueField;
        }

        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (r.getTagValue() == entity.getTagValue(field.getName())) {
                tagValueField = field;
                TLVCache.addTlvEntityCache(entity.getClass().getName(), r.getTagValue(), tagValueField);
                break;
            }
        }

        return tagValueField;
    }

    private static void setEntityFieldValue(TLVDecodeResult r, Entity entity, Field field) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(true);
        Class<?> typeCls = field.getType();
        if (typeCls == int.class) {
            field.setInt(entity, r.getIntValue());
        } else if (typeCls == long.class) {
            field.setLong(entity, r.getLongValue());
        } else if (typeCls == String.class) {
            field.set(entity, r.getStringValue());
        } else if (typeCls == byte[].class) {
            field.set(entity, r.getValue());
        } else {

            if (typeCls == Entity.class || typeCls.getSuperclass() == Entity.class) {
                if (r.getDataType() == TLVEncoder.ConstructedData) {
                    List<TLVDecodeResult> list = (List<TLVDecodeResult>) r.getValue();
                    if (list == null) {
                        return;
                    }
                    for (TLVDecodeResult tlvDecodeResult : list) {
                        Class<?> cls = ReqRespRelationship.COMMAND_CLASS_MAP.get(tlvDecodeResult.getTagValue());
                        if (cls != null) {
                            try {
                                Entity sonEntity = (Entity) cls.newInstance();
                                parseEntityImpl(tlvDecodeResult, sonEntity);
                                field.set(entity, sonEntity);
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        } else {
                            LogUtils.w(TAG, "未找到该类型:" + field.getName() + " " + typeCls);
                        }
                    }
                } else {
                    LogUtils.w(TAG, "TLV数据类型错误!");
                }
            } else {
                LogUtils.w(TAG, "不支持值类型:" + field.getName() + " " + typeCls);
            }
        }
    }
}
