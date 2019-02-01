package com.eebbk.bfc.im.push.entity.request.push;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;

@CommandValue(Command.PUSH_SYNC_FIN_ACK)
public class PushSyncFinAckRequestEntity extends RequestEntity {

    @TagValue(1)
    private int RID;

    @TagValue(10)
    private long registerId;

    @TagValue(11)
    private String appKey;

    @TagValue(12)
    private String alias;

    @TagValue(13)
    private long syncKey;

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

    public long getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(long syncKey) {
        this.syncKey = syncKey;
    }
}
