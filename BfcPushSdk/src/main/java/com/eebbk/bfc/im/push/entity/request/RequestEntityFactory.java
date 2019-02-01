package com.eebbk.bfc.im.push.entity.request;

import android.text.TextUtils;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncFinAckRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncTriggerRequestEntity;
import com.eebbk.bfc.im.push.util.platform.Device;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.util.IDUtil;

import java.util.List;

/**
 * 请求实体工厂
 * 用于生成各种请求实体类
 */
public class RequestEntityFactory {

    private SyncApplication app;

    public RequestEntityFactory(SyncApplication app) {
        this.app = app;
    }


    public PublicKeyRequestEntity createPublicKeyRequestEntity() {
        PublicKeyRequestEntity entity = new PublicKeyRequestEntity();
        entity.setRID(IDUtil.getRID());
        LogUtils.d("create PublicKeyRequestEntity:" + entity.toString());
        return entity;
    }

    public EncryptSetRequestEntity createEncryptSetRequestEntity() {
        EncryptSetRequestEntity entity = new EncryptSetRequestEntity();
        entity.setRID(IDUtil.getRID());
        entity.setEncryptType(1);
        LogUtils.d("create EncryptSetRequestEntity:" + entity.toString());
        return entity;
    }

    public RegistRequestEntity createRegistRequestEntity(String appKey) {
        Device device = app.getPlatform().getDevice();
        RegistRequestEntity requestEntity = new RegistRequestEntity();
        requestEntity.setRID(IDUtil.getRID());
        if (appKey == null) {
            appKey = device.getAppKeyFromMetaData();
        }
        requestEntity.setAppKey(appKey);
        requestEntity.setBasebandVersion(device.getBasebandVersion());
        requestEntity.setBuildNumber(device.getBuildNumber());
        requestEntity.setDeviceId(device.getDeviceId());
        requestEntity.setImei(device.getImei());
        requestEntity.setImsi(device.getImsi());
        requestEntity.setMac(device.getMacAddress());
        requestEntity.setModelNumber(device.getModelNumber());
        requestEntity.setPkgName(app.getPackageName());
        requestEntity.setPlatform(1);//android平台是1
        requestEntity.setResolution(device.getScreenResolution());
        requestEntity.setSdkVerison(device.getSysSDKVersion());
        requestEntity.setSysName(device.getAndroidSysName());
        requestEntity.setSysVersion(device.getAndroidOsVersion());
        checkRegistRequestEntity(requestEntity);

        LogUtils.d("create RegistRequestEntity:" + requestEntity.toString());
        return requestEntity;
    }

    public RegistRequestEntity createRegistRequestEntity() {
        return createRegistRequestEntity(null);
    }

    private void checkRegistRequestEntity(RegistRequestEntity requestEntity) {
        if (TextUtils.isEmpty(requestEntity.getAppKey())) {
            throw new NullPointerException("appKey is null.");
        }
        if (TextUtils.isEmpty(requestEntity.getDeviceId())) {
            throw new NullPointerException("deviceId is null.");
        }
        if (TextUtils.isEmpty(requestEntity.getPkgName())) {
            throw new NullPointerException("packageName is null.");
        }
    }

    public LoginRequestEntity createLoginRequestEntity(long registId, String registToken) {
        LoginRequestEntity requestEntity = new LoginRequestEntity();
        long accountId = 0;
        String accountToken = null;
        requestEntity.setRID(IDUtil.getRID());
        requestEntity.setRegistId(registId);
        requestEntity.setRegistToken(registToken);
        requestEntity.setImAccountId(accountId);
        requestEntity.setAccountToken(accountToken);
        requestEntity.setSdkVersion(app.getSyncSDKVersionCode());
        requestEntity.setImSdkVersionName(app.getSyncSDKVersionName());

        LogUtils.d("create LoginRequestEntity:" + requestEntity.toString());
        return requestEntity;
    }


    public AliasAndTagsRequestEntity createAliasAndTagRequestEntity(String alias, List<String> tags, long registId) {
        Device device = app.getPlatform().getDevice();
        AliasAndTagsRequestEntity entity = new AliasAndTagsRequestEntity();
        entity.setRID(IDUtil.getRID());
        entity.setAlias(alias);

        if(tags!=null&&(!tags.isEmpty())){
            StringBuilder tagsBuilder=new StringBuilder();
            int len=tags.size();
            for (int i=0;i<len;i++) {
                if(i!=0){
                    tagsBuilder.append(",");
                }
                tagsBuilder.append(tags.get(i));
            }
            entity.setTag(tagsBuilder.toString());
        }else {
            entity.setTag(null);
        }

        entity.setRegistId(registId);
        entity.setAppKey(device.getAppKeyFromMetaData());
        LogUtils.d("create createAliasAndTagRequestEntity:" + entity.toString());
        return entity;
    }

    public PushSyncTriggerRequestEntity createPushSyncTriggerRequestEntity(String alias, long registId) {
        Device device = app.getPlatform().getDevice();
        PushSyncTriggerRequestEntity entity = new PushSyncTriggerRequestEntity();
        entity.setRID(IDUtil.getRID());
        entity.setAppKey(device.getAppKeyFromMetaData());
        entity.setAlias(alias);
        entity.setRegistId(registId);
        LogUtils.d("create createPushSyncTriggerRequestEntity:" + entity.toString());
        return entity;
    }

    public PushSyncRequestEntity createPushSyncRequestEntity(String alias, long syncKey, int mode, int pageSize, long registId) {
        Device device = app.getPlatform().getDevice();
        PushSyncRequestEntity entity = new PushSyncRequestEntity();
        entity.setRID(IDUtil.getRID());
        entity.setAppKey(device.getAppKeyFromMetaData());
        entity.setAlias(alias);
        entity.setSyncKey(syncKey);
        entity.setMode(mode);
        entity.setRegistId(registId);
        entity.setPageSize(pageSize);
        LogUtils.d("create createPushSyncRequestEntity:" + entity.toString());
        return entity;
    }

    public PushSyncFinAckRequestEntity createPushSyncFinAckRequestEntity(String alias, long syncKey, long registId) {
        Device device = app.getPlatform().getDevice();
        PushSyncFinAckRequestEntity entity = new PushSyncFinAckRequestEntity();
        entity.setRID(IDUtil.getRID());
        entity.setAlias(alias);
        entity.setSyncKey(syncKey);
        entity.setRegistId(registId);
        entity.setAppKey(device.getAppKeyFromMetaData());
        LogUtils.d("create createPushSyncFinAckRequestEntity:" + entity.toString());
        return entity;
    }

}
