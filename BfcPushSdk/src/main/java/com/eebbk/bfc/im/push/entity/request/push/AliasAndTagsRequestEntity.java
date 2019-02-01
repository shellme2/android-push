package com.eebbk.bfc.im.push.entity.request.push;

import android.text.TextUtils;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;

import java.util.Arrays;
import java.util.List;

@CommandValue(Command.PUSH_ALIAS_AND_TAG_REQUEST)
public class AliasAndTagsRequestEntity extends RequestEntity {

    @TagValue(1)
    private int RID;

    @TagValue(10)
    private long registerId;

    @TagValue(11)
    private String appKey;

    @TagValue(12)
    private String alias;

    @TagValue(13)
    private String tag;

    @Override
    public int getRID() {
        return RID;
    }

    @Override
    public void setRID(int RID) {
        this.RID = RID;
    }

    public long getRegisterId() {
        return registerId;
    }

    public void setRegisterId(long registerId) {
        this.registerId = registerId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getTag() {
        return tag;
    }

    public List<String> getTagsList() {
        if(TextUtils.isEmpty(tag)){
            return null;
        }else {
            return Arrays.asList(tag.split(","));
        }
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
