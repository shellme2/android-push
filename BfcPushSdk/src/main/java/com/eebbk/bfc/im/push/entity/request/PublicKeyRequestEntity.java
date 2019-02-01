package com.eebbk.bfc.im.push.entity.request;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.entity.Command;

@CommandValue(Command.PUBLIC_KEY_REQUEST)
public class PublicKeyRequestEntity extends RequestEntity {

    @TagValue(1)
    private int RID;

    @Override
    public int getRID() {
        return RID;
    }

    @Override
    public void setRID(int RID) {
        this.RID = RID;
    }
}
