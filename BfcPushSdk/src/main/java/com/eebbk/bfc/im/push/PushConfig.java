package com.eebbk.bfc.im.push;

import android.text.TextUtils;

import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PushConfig {

    private String[] mBackupServerInfo;//预埋ip和端口号,必须是xx.xx.xx.xx:port格式,否则无法添加
    private boolean mClearBefore;//true表示会把之前的预埋ip都清除后再添加现在的预埋ip，false表示直接添加现在的预埋ip

    private int mMinHeart;
    private int mMaxHeart;
    private int mHeartStep;


    private PushConfig(){

    }

    public static class Builder{

        private final PushConfig config;

        public Builder(){
            config=new PushConfig();
        }

        public PushConfig creat(){
            return config;
        }

        public Builder setBackupServerInfo(String[] backupServerInfo, boolean clearBefore){
            String[] filterInfo=filterServerInfo(backupServerInfo);
            if(filterInfo.length>0){
                config.mBackupServerInfo=filterInfo;
                config.mClearBefore=clearBefore;
            }else{
                LogUtils.e("backupServerInfo is error");
            }
            return this;
        }
        private String[] filterServerInfo(String[] serverInfo) {
            List<String> serverInfoList = new ArrayList<>();
            if (serverInfo != null && serverInfo.length > 0) {
                for(String s : serverInfo) {
                    String[] ip_port = s.split(":");
                    if (ip_port.length != 2) {
                        continue;
                    }
                    try {
                        String m_ip = ip_port[0];
                        int m_port = Integer.parseInt(ip_port[1]);
                        if (!checkHost(m_ip, m_port)) {
                            LogUtils.test("hostname:" + m_ip + ",port:" + m_port);
                            continue;
                        }
                        serverInfoList.add(s);
                    } catch (Exception e) {
                        LogUtils.e(e);
                    }
                }
            }
            LogUtils.i("serverInfoArray:" + Arrays.toString(serverInfoList.toArray(new String[]{})));
            return serverInfoList.toArray(new String[]{});
        }
        private boolean checkHost(String hostname, int port) {
            return !(TextUtils.isEmpty(hostname) || port <= 0 || port > 65535);
        }

        public Builder setHeartbeatPeriod(int minHeart,int maxHeart, int heartStep){
            config.mHeartStep=heartStep;
            config.mMinHeart=minHeart;
            config.mMaxHeart=maxHeart;
            // TODO: 2016/10/6 check
            return this;
        }
    }

    public String[] getBackupServerInfo() {
        return mBackupServerInfo;
    }

    public boolean isClearBefore() {
        return mClearBefore;
    }

    public int getMinHeart() {
        return mMinHeart;
    }

    public int getMaxHeart() {
        return mMaxHeart;
    }

    public int getHeartStep() {
        return mHeartStep;
    }
}
