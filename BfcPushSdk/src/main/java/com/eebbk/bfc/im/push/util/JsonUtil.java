package com.eebbk.bfc.im.push.util;


import com.eebbk.bfc.sequence.SequenceTools;

import java.lang.reflect.Type;

public class JsonUtil {

    //构造函数私有，防止恶意新建
    private JsonUtil(){}

    public static String toJson(Object obj) {
        try {
            return SequenceTools.serialize(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T fromJson(String json, Class<T> cls) {
        try {
            return SequenceTools.deserialize(json, cls);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T fromJson(String json, Type type) {
        try {
            return SequenceTools.deserialize(json, type);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
