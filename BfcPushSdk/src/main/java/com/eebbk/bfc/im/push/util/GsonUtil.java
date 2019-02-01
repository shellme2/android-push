package com.eebbk.bfc.im.push.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class GsonUtil {

    //构造函数私有，防止恶意新建
    private GsonUtil(){}

    public static String toJSON(Object obj) {
        return createGson().toJson(obj);
    }

    public static String toJSON(Object obj, Type type) {
        return createGson().toJson(obj, type);
    }

    public static <T> T fromJSON(String json, Class<T> cls) {
        try {
            return createGson().fromJson(json, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T fromJSON(String json, Type type) {
        try {
            return createGson().fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        return gsonBuilder.create();
    }

}
