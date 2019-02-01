package com.eebbk.bfc.im.push.util;

import com.eebbk.bfc.im.push.entity.Entity;
import com.eebbk.bfc.im.push.entity.ReqRespRelationship;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.tlv.TLVDecodeResult;
import com.eebbk.bfc.im.push.tlv.TLVDecoder;
import com.eebbk.bfc.im.push.tlv.TLVEncoder;
import com.eebbk.bfc.im.push.tlv.TLVObject;

import java.lang.reflect.Field;
import java.util.List;

/**
 * TLV格式转换工具
 */
public class TLVObjectUtil {

    //构造函数私有，防止恶意新建
    private TLVObjectUtil(){}

    /**
     * 创建心跳包tlv格式的
     */
    public static byte[] createHeartBeatByteArray(int tagValue) {
        TLVObject tlvObj = new TLVObject();
        tlvObj.put(tagValue, (byte[]) null);
        return tlvObj.toByteArray();
    }

    /**
     * 把实体转化为TLV格式的对象
     */
    public static TLVObject parseTLVObject(Entity entity) {
        TLVObject tlvObj = new TLVObject();
        try {
            tlvObj.put(entity.getCommand(), parseTLVObjectImpl(entity));
        } catch (Exception e) {
            e.printStackTrace();
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
                                            TLVObject tlvObj) throws IllegalAccessException {
        targetField.setAccessible(true);
        Class<?> valueClsType = targetField.getType();
        Object valueObj = targetField.get(entity);
        if (valueObj == null) {
            LogUtils.w("formatdata error(value is null),name:"
                    + targetField.getName() + ",type:" + targetField.getType());
            return;
        }
        if (valueClsType == int.class) {
            int value = (int) valueObj;
            if (value != 0) {
                tlvObj.put(tagValue, value);
            } else {
                LogUtils.w("formatdata error(" + valueObj.getClass().getSimpleName() + " value is 0):"
                        + targetField.getName());
            }
        } else if (valueClsType == long.class) {
            long value = (long) valueObj;
            if (value != 0) {
                tlvObj.put(tagValue, value);
            } else {
                LogUtils.w("formatdata error(" + valueObj.getClass().getSimpleName() + " value is 0):"
                        + targetField.getName());
            }
        } else if (valueClsType == String.class) {
            tlvObj.put(tagValue, valueObj.toString());
        } else if (valueClsType == byte[].class) {
            tlvObj.put(tagValue, (byte[]) valueObj);
        } else {
            /*LogUtils.w("formatdata error(unsupport type " + valueObj.getClass().getName() + "):"
                    + targetField.getName());*/
            if (valueObj instanceof Entity) {
                tlvObj.put(tagValue, parseTLVObject((Entity) valueObj));
            } else {
                LogUtils.w("formatdata error(unsupport type " + valueObj.getClass().getName() + "):"
                        + targetField.getName());
            }
        }
    }

    public static ResponseEntity parseResponseEntity(byte[] data) throws Exception {
        return (ResponseEntity) parseEntity(data);
    }

    public static RequestEntity parseRequestEntity(byte[] data) throws Exception {
        return (RequestEntity) parseEntity(data);
    }

    public static Entity parseEntity(byte[] data) throws Exception {
        TLVDecodeResult result = TLVDecoder.decode(data);
        Entity entity = parseEntity(data, ReqRespRelationship.COMMAND_CLASS_MAP.get(result.getTagValue()));
        return entity;
    }

    public static <T> T parseEntity(byte[] data, Class<?> cls) throws Exception {
        TLVDecodeResult result = TLVDecoder.decode(data);
        Entity entity = null;
        try {
            entity = (Entity) cls.newInstance();
            parseEntityImpl(result, entity);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) entity;
    }

    private static void parseEntityImpl(TLVDecodeResult result, Entity entity) {
        if (result.getDataType() == TLVEncoder.ConstructedData) {
            List<TLVDecodeResult> list = (List<TLVDecodeResult>) result.getValue();
            try {
                initEntityFieldValue(list, entity);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                initEntityFieldValueImpl(result, entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void initEntityFieldValue(List<TLVDecodeResult> list, Entity entity) throws IllegalAccessException {
        for (TLVDecodeResult r : list) {
            initEntityFieldValueImpl(r, entity);
        }
    }

    private static void initEntityFieldValueImpl(TLVDecodeResult r, Entity entity) throws IllegalAccessException {
        Field field = getFieldByTagValueFromEntity(r, entity);
        if (field != null) {
            setEntityFieldValue(r, entity, field);
        }
    }

    private static Field getFieldByTagValueFromEntity(TLVDecodeResult r, Entity entity) {
        Field tagValueField = null;
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (r.getTagValue() == entity.getTagValue(field.getName())) {
                tagValueField = field;
                break;
            }
        }
        /*if (r.getDataType() == TLVEncoder.PrimitiveData) {
            for (Field field : fields) {
                if (r.getTagValue() == entity.getTagValue(field.getName())) {
                    tagValueField = field;
                    break;
                }
            }
        } else {
            LogUtils.w("不支持嵌套TLV结构体解析!");
            *//*for (Field field : fields) {
                if (r.getTagValue() == entity.getTagValue(field.getName())) {
                    tagValueField = field;
                    break;
                }
            }*//*
        }*/
        return tagValueField;
    }

    private static void setEntityFieldValue(TLVDecodeResult r, Entity entity, Field field) throws IllegalAccessException {
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
//            LogUtils.w("不支持值类型:" + field.getName() + " " + typeCls);
            if (typeCls == Entity.class || typeCls.getSuperclass() == Entity.class) {
                if (r.getDataType() == TLVEncoder.ConstructedData) {
                    List<TLVDecodeResult> list = (List<TLVDecodeResult>) r.getValue();
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
                            LogUtils.w("未找到该类型:" + field.getName() + " " + typeCls);
                        }
                    }
                } else {
                    LogUtils.w("TLV数据类型错误!");
                }
            } else {
                LogUtils.w("不支持值类型:" + field.getName() + " " + typeCls);
            }
        }
    }
}
