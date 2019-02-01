package com.eebbk.bfc.im.push.util;

import java.util.Random;

public class RandomUtil {

    //构造函数私有，防止恶意新建
    private RandomUtil(){}

    public static float getRandom(int min, int max) {
        Random random = new Random();
        return (float) (random.nextDouble() * (max - min) + min);
    }
}
