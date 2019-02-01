package com.eebbk.bfc.im.push.code;

import android.content.Context;

import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.Entity;
import com.eebbk.bfc.im.push.entity.encrypt.EncryptWapper;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.exception.DecodeException;
import com.eebbk.bfc.im.push.service.tcp.ConnectionProcessContext;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.SyncToastUtil;
import com.eebbk.bfc.im.push.util.TLVObjectUtil;

/**
 * 对数据进行编码和解码
 */
public class DataCoderUtil {

    private DataCoderUtil(){}

    public static byte[] encodeData(Entity entity) {
        byte[] data = entity.toByteArray();
        int command = entity.getCommand();
        if (command == Command.PUBLICKEY_REQUEST || command == Command.ENCRYPT_SET_REQUEST) { // 这两个协议包数据不进行加密
            return data;
        }

        boolean encrypt = ConnectionProcessContext.getInstance().isEncrypt();
        if (encrypt) {
            EncryptWapper encryptWapper = new EncryptWapper();
            byte[] secretKey = ConnectionProcessContext.getInstance().getSecretKey();
            byte[] encryptSrcData = EncryptUtil.encrypt(data, secretKey);
            encryptWapper.setPayload(encryptSrcData);
            data = encryptWapper.toByteArray();
            LogUtils.i("encrypt data:" + encryptWapper+"  ::entity=="+entity.toString());
        } else  {
            LogUtils.d("no encrypt the data...");
        }
        return data;
    }

    public static ResponseEntity decodeData(Context context, byte[] data) throws DecodeException {
        Entity entity = null;
        try {
            entity = TLVObjectUtil.parseEntity(data);
        } catch (Exception e) {
            //tlv数据解析报错，此时应该及时断开连接进行重连，否则后面的数据将有可能全部都乱了导致解析报错
            LogUtils.e(e);
            throw new DecodeException("decode data error:" + e.toString());
        }

        if (entity == null) {
            LogUtils.e("parse entity is null");
            return null;
        }

        ResponseEntity responseEntity = null;
        if (entity.getCommand() == Command.ENCRYPT_WAPPER) {
            EncryptWapper encryptWapper = (EncryptWapper) entity;
            byte[] secretKey = ConnectionProcessContext.getInstance().getSecretKey();
            byte[] decryptData = EncryptUtil.decrypt(encryptWapper.getPayload(), secretKey);
            try {
                responseEntity = TLVObjectUtil.parseResponseEntity(decryptData);
            } catch (Exception e) {
                LogUtils.e(e);
                throw new DecodeException("decode data error:" + e.toString());
            }
        } else {
            if (entity instanceof ResponseEntity) {
                responseEntity = (ResponseEntity) entity;
            } else {
                if (LogUtils.isDebug()) {
                    SyncToastUtil.showToast(context, "IM类型转换错误," + entity.getClass().getName() + " can not be cast to " + ResponseEntity.class.getName());
                }
                LogUtils.e("a strange error happened such as response class cast error:" + entity.getClass().getName() + " can not be cast to " + ResponseEntity.class.getName());
            }
        }
        if (responseEntity != null) {
            LogUtils.i("decode data:" + responseEntity);
        } else {
            LogUtils.i("decode data:null");
        }
        return responseEntity;
    }
}
