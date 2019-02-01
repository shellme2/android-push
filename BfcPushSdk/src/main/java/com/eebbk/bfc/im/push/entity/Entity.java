package com.eebbk.bfc.im.push.entity;

import com.eebbk.bfc.im.push.anotation.CommandValue;
import com.eebbk.bfc.im.push.anotation.TagValue;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.GsonUtil;
import com.eebbk.bfc.im.push.util.TLVObjectUtil;

import java.lang.reflect.Field;

/**
 * 请求实体和响应实体的基类
 */
@CommandValue(0)
public abstract class Entity {

    public int getCommand() {
        CommandValue commandValue = this.getClass().getAnnotation(CommandValue.class);
        return commandValue == null ? 0 : commandValue.value();
    }

    public int getTagValue(String fieldName) {
        Field f = null;
        try {
            f = this.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            LogUtils.e(e);
            return 0;
        } catch (SecurityException e) {
            LogUtils.e(e);
            return 0;
        }
        TagValue tagValue = f.getAnnotation(TagValue.class);
        return tagValue == null ? 0 : tagValue.value();
    }

    public byte[] toByteArray() {
        int command = getCommand();
        if (command == Command.HEART_BEAT_REQUEST) {
            return TLVObjectUtil.createHeartBeatByteArray(command);
        } else {
            return TLVObjectUtil.parseByteArray(this);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + GsonUtil.toJSON(this);
    }
}
