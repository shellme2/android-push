package com.eebbk.bfc.im.push.bean;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class AliasAndTagsInfo {

    public static AliasAndTagsInfo EMPTY_ALIAS_TAGS_INFO =new AliasAndTagsInfo();

    private String alias;

    private String tags;

    private boolean isSet;

    public AliasAndTagsInfo() {
    }

    public AliasAndTagsInfo(String alias, List<String> tags, boolean isSet) {
        this.alias = alias;
        this.isSet=isSet;
        this.tags=getTagString(tags);
    }

    public AliasAndTagsInfo(String alias, String tags, boolean isSet) {
        this.alias = alias;
        this.tags = tags;
        this.isSet = isSet;
    }

    private String getTagString(List<String> tags){
        if(tags==null||tags.isEmpty()){
            return null;
        }else{
            StringBuilder tagsTemp=new StringBuilder();
            int size=tags.size();
            for(int i=0;i<size;i++){
                if(i!=0){
                    tagsTemp.append(",");
                }
                tagsTemp.append(tags.get(i));
            }
            return tagsTemp.toString();
        }
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTags() {
        return tags;
    }

    public List<String> getTagsList(){
        if(TextUtils.isEmpty(tags)){
            return null;
        }else{
            return Arrays.asList(tags.split(","));
        }
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    public void setTags(List<String> tags) {
        this.tags = getTagString(tags);
    }

    public boolean isSet() {
        return isSet;
    }

    public void setSet(boolean set) {
        isSet = set;
    }

    @Override
    public String toString() {
        return "AliasAndTagsInfo{" +
                "alias='" + alias + '\'' +
                ", tags='" + tags + '\'' +
                ", isSet=" + isSet +
                '}';
    }
}
