package com.eebbk.bfc.im.push.entity.response.push;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;

@CommandValue(Command.PUSH_SYNC_RESPONSE)
public class PushSyncResponseEntity extends ResponseEntity {

    @TagValue(1)
    private int RID;

    @TagValue(2)
    private int code;

    @TagValue(3)
    private String desc;

    @TagValue(10)
    private String pkgName;

    @TagValue(11)
    private String alias;

    @TagValue(12)
    private byte[] message;

    @TagValue(13)
    private long syncKey;

    @TagValue(14)
    private String msgId;

    @TagValue(15)
    private long createTime;

    @TagValue(16)
    private String module;

    @Override
    public int getRID() {
        return RID;
    }

    @Override
    public void setRID(int RID) {
        this.RID = RID;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public long getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(long syncKey) {
        this.syncKey = syncKey;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getModule(){
        return module;
    }

    public void setModule(String module){
        this.module=module;
    }

    //"RID":100003,
    // "alias":"M161000600",
    // "code":200,
    // "createTime":1479469091185,"
    // message":[97,102,100,97,115,100,102,115,100,102,115,100,102],"
    // msgId":"3e41b26692af11e5ab0cecf4bbdf1a90",
    // "pkgName":"com.eebbk.bfc.demo.push",
    // "syncKey":90
}
