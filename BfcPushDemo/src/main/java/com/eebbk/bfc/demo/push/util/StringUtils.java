package com.eebbk.bfc.demo.push.util;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class StringUtils {
    private StringUtils(){}

    public static String listToString(List<String> strs){
        if(strs==null){
            return null;
        }

        int size=strs.size();
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<size;i++){
            if(i!=0){
                builder.append(",");
            }
            builder.append(strs.get(i));
        }
        return builder.toString();
    }

    public static List<String> stringtoList(String str){
        if(TextUtils.isEmpty(str)){
            return null;
        }

        return Arrays.asList(str.split(","));
    }
}
