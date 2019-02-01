package com.eebbk.bfc.im.push.entity.response.push;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;

@CommandValue(Command.PUSH_SYNC_INFORM)
public class PushSyncInformResponseEntity extends ResponseEntity {

    @TagValue(10)
    private String pkgName;

    @TagValue(11)
    private String alias;

    @TagValue(12)
    private long syncKey;

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

    public long getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(long syncKey) {
        this.syncKey = syncKey;
    }
}
