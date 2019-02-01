package com.eebbk.bfc.im.push.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.bean.AliasAndTagsInfo;
import com.eebbk.bfc.im.push.error.ErrorCode;
import com.eebbk.bfc.im.push.util.platform.PhoneStore;
import com.eebbk.bfc.im.push.util.platform.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liuyewu
 * @company EEBBK
 * @function parameter check utils
 * @date 2016/11/16
 */
public class ParameterCheckUtils {
    private ParameterCheckUtils(){}

    /**
     *标签规则检测
     * @param tags 标签集合
     */
    public static void checkTags(List<String> tags){
        if(tags!=null&&!tags.isEmpty()){
            for (String tag:tags) {
                if(tag.length()>40){
                    throw new RuntimeException(ErrorCode.EC_TAGS_LENGTH_MORE_40+
                            "::Tags to illegal, character length is no more than 40, please check the tags set!");
                }
                if(!tag.matches("^[a-zA-Z0-9_]+$")){
                    throw new RuntimeException(ErrorCode.EC_TAGS_NOT_NAMING_RULE+
                            "::Tags to illegal, tags consist of digital, 26 English letters or underline, please check the tags set!");
                }
            }
        }
    }

    /**
     * 检测储存别名标签与要设定别名标签的关系
     * @param context 上下文对象
     * @param alias 别名
     * @param tags 标签
     * @return 别名标签参数集合
     */
    public static AliasAndTagsInfo checkStoreAndEntryValue(Context context, String alias, List<String> tags){

        Store store=new PhoneStore(context);
        AliasAndTagsInfo aliasAndTagsInfo=StoreUtil.readAliasAndTag(store);

        if(aliasAndTagsInfo.isSet()){

            String alisTemp=aliasAndTagsInfo.getAlias();

            if(TextUtils.isEmpty(alias)&&(tags==null||tags.isEmpty())){
                LogUtils.i("check alias is empty,use the store alias is: "+alisTemp);
                return null;
            }

            if(TextUtils.equals(alias, alisTemp)&&(tags==null||tags.isEmpty())){
                LogUtils.i("check alias is equals the store alias,alisa is: "+alisTemp);
                return null;
            }

            if(!TextUtils.isEmpty(alias)){
                aliasAndTagsInfo.setAlias(alias);
            }

            aliasAndTagsInfo.setTags(tags);

        }else{
            if(alias==null){
                alias= DeviceUtils.getMachineId(context);
            }
            aliasAndTagsInfo.setAlias(alias);
            aliasAndTagsInfo.setTags(tags);
        }

        LogUtils.i("check alias and tags finish,alias and tags is: "+aliasAndTagsInfo.getAlias()+" :: "+aliasAndTagsInfo.getTags());

        return aliasAndTagsInfo;
    }

    /**
     * 预埋ip参数过滤器
     * @param serverInfo 预埋IP数组
     * @return 过滤后的预埋IP数组
     */
    public static String[] filterServerInfo(String[] serverInfo) {
        List<String> serverInfoList = new ArrayList<>();
        if (serverInfo != null && serverInfo.length > 0) {
            for(String s : serverInfo) {
                String[] ip_port = s.split(":");
                if (ip_port.length != 2) {
                    LogUtils.ec("the ip format error,ip is:: "+s,
                            ErrorCode.EC_IP_ILLEGAL);
                    continue;
                }
                try {
                    String m_ip = ip_port[0];
                    int m_port = Integer.parseInt(ip_port[1]);
                    if (!checkHost(m_ip, m_port)) {
                        LogUtils.ec("the ip can not be empty or port rang of 0~65535,ip is:: "+s,
                                ErrorCode.EC_IP_ILLEGAL);
                        continue;
                    }
                    serverInfoList.add(s);
                } catch (Exception e) {
                    LogUtils.ec("the port can not format to int,ip is:: "+s +"  error message"+e.getMessage(),
                            ErrorCode.EC_IP_ILLEGAL);
                }
            }
        }
        LogUtils.i("serverInfoArray:" + Arrays.toString(serverInfoList.toArray(new String[]{})));
        return serverInfoList.toArray(new String[]{});
    }

    /**
     * 检测IP域名，端口号规范
     * @param hostname IP域名
     * @param port 端口号
     * @return true，符合规范；false，不合规范
     */
    public static boolean checkHost(String hostname, int port) {
        return !(TextUtils.isEmpty(hostname) || port <= 0 || port > 65535);
    }

}
