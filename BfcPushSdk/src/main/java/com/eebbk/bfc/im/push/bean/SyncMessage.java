package com.eebbk.bfc.im.push.bean;

import java.util.Arrays;

public class SyncMessage {

    private boolean thirdSyncMsg;

    private boolean notify;

    private long dialogId;

    private long imAccountId;

    private long registerId;

    private int msgType;

    private byte[] msg;

    private long syncKey;

    private String msgId;

    private String pkgName;

    private String alias;

    private long createTime;

    private String module;

    public SyncMessage() {

    }

    public boolean isThirdSyncMsg() {
        return thirdSyncMsg;
    }

    public void setPushSyncMsg(boolean thirdSyncMsg) {
        this.thirdSyncMsg = thirdSyncMsg;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public long getDialogId() {
        return dialogId;
    }

    public void setDialogId(long dialogId) {
        this.dialogId = dialogId;
    }

    public long getImAccountId() {
        return imAccountId;
    }

    public void setImAccountId(long imAccountId) {
        this.imAccountId = imAccountId;
    }

    public long getRegisterId() {
        return registerId;
    }

    public void setRegisterId(long registerId) {
        this.registerId = registerId;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
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

    @Override
    public String toString() {
        return "SyncMessage{" +
                "thirdSyncMsg=" + thirdSyncMsg +
                ", notify=" + notify +
                ", dialogId=" + dialogId +
                ", imAccountId=" + imAccountId +
                ", registerId=" + registerId +
                ", msgType=" + msgType +
                ", msg=" + Arrays.toString(msg) +
                ", syncKey=" + syncKey +
                ", msgId='" + msgId + '\'' +
                ", pkgName='" + pkgName + '\'' +
                ", alias='" + alias + '\'' +
                ", createTime=" + createTime +
                ", module=" + module +
                '}';
    }
}
