package com.eebbk.bfc.im.push.bean;

public class ImHeartConf {

    public static final String KEY = "ImHeartConf";

    private int minHeart;

    private int maxHeart;

    private int curHeart;

    private int heartStep;

    public int getMinHeart() {
        return minHeart;
    }

    public void setMinHeart(int minHeart) {
        this.minHeart = minHeart;
    }

    public int getMaxHeart() {
        return maxHeart;
    }

    public void setMaxHeart(int maxHeart) {
        this.maxHeart = maxHeart;
    }

    public int getCurHeart() {
        return curHeart;
    }

    public void setCurHeart(int curHeart) {
        this.curHeart = curHeart;
    }

    public int getHeartStep() {
        return heartStep;
    }

    public void setHeartStep(int heartStep) {
        this.heartStep = heartStep;
    }

    @Override
    public String toString() {
        return "ImHeartConf{" +
                "minHeart=" + minHeart +
                ", maxHeart=" + maxHeart +
                ", curHeart=" + curHeart +
                ", heartStep=" + heartStep +
                '}';
    }
}
