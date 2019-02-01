package com.eebbk.bfc.im.push.entity.request;

import android.os.Build;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.SDKVersion;
import com.eebbk.bfc.im.push.entity.PushType;
import com.eebbk.bfc.im.push.entity.request.push.AliasAndTagsRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncFinAckRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncRequestEntity;
import com.eebbk.bfc.im.push.entity.request.push.PushSyncTriggerRequestEntity;
import com.eebbk.bfc.im.push.util.IDUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.StoreUtil;
import com.eebbk.bfc.im.push.util.platform.Device;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

/**
 * 请求实体工厂
 * 用于生成各种请求实体类
 */
public class RequestEntityFactory {

    private static final String TAG = "RequestEntityFactory";

    private PushApplication app;

    public RequestEntityFactory(PushApplication app) {
        this.app = app;
    }


    public PublicKeyRequestEntity createPublicKeyRequestEntity() {
        PublicKeyRequestEntity entity = new PublicKeyRequestEntity();
        entity.setRID(IDUtil.getRID());
        LogUtils.d(TAG,"create PublicKeyRequestEntity:" + entity.toString());
        return entity;
    }

    public EncryptSetRequestEntity createEncryptSetRequestEntity() {
        EncryptSetRequestEntity entity = new EncryptSetRequestEntity();
        entity.setRID(IDUtil.getRID());
        entity.setEncryptType(1);
        LogUtils.d(TAG,"create EncryptSetRequestEntity:" + entity.toString());
        return entity;
    }

    public RegisterRequestEntity createRegisterRequestEntity(String appKey) {
        Device device = app.getPlatform().getDevice();
        RegisterRequestEntity requestEntity = new RegisterRequestEntity();
        requestEntity.setRID(IDUtil.getRID());
        if (appKey == null) {
            appKey = device.getAppKeyFromMetaData();
        }
        requestEntity.setAppKey(appKey);
        requestEntity.setBaseBandVersion(device.getBaseBandVersion());
        requestEntity.setBuildNumber(device.getBuildNumber());
        requestEntity.setDeviceId(device.getDeviceId());
        requestEntity.setImei(device.getImei());
        requestEntity.setImsi(device.getImsi());
        requestEntity.setMac(device.getMacAddress());
        requestEntity.setModelNumber(device.getModelNumber());
        requestEntity.setPkgName(app.getPackageName());
        requestEntity.setPlatform(1);//android平台是1
        requestEntity.setResolution(device.getScreenResolution());
        requestEntity.setSdkVersion(device.getSysSDKVersion());
        requestEntity.setSysName(device.getAndroidSysName());
        requestEntity.setSysVersion(device.getAndroidOsVersion());
        checkRegisterRequestEntity(requestEntity);

        LogUtils.d(TAG,"create RegisterRequestEntity:" + requestEntity.toString());
        return requestEntity;
    }

    public RegisterRequestEntity createRegisterRequestEntity() {
        return createRegisterRequestEntity(null);
    }

    private void checkRegisterRequestEntity(RegisterRequestEntity requestEntity) {
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

    public LoginRequestEntity createLoginRequestEntity(long registerId, String registerToken) {
        LoginRequestEntity requestEntity = new LoginRequestEntity();
        long accountId = 0;
        String accountToken = null;
        requestEntity.setRID(IDUtil.getRID());
        requestEntity.setRegisterId(registerId);
        requestEntity.setRegisterToken(registerToken);
        requestEntity.setImAccountId(accountId);
        requestEntity.setAccountToken(accountToken);
        requestEntity.setSdkVersion(SDKVersion.getSDKInt());
        requestEntity.setImSdkVersionName(SDKVersion.getVersionName());

        LogUtils.d(TAG,"create LoginRequestEntity:" + requestEntity.toString());
        return requestEntity;
    }


    public AliasAndTagsRequestEntity createAliasAndTagRequestEntity(String alias, List<String> tags, long registerId) {
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
            entity.setTag("");//设为null，不会改变标签;设为空，则会覆盖;首次最好设空，以后优化
        }

        entity.setRegisterId(registerId);
        entity.setAppKey(device.getAppKeyFromMetaData());
        LogUtils.d("create createAliasAndTagRequestEntity:" + entity.toString());
        return entity;
    }

    public PushSyncTriggerRequestEntity createPushSyncTriggerRequestEntity(String alias, long registerId) {
        Device device = app.getPlatform().getDevice();
        PushSyncTriggerRequestEntity entity = new PushSyncTriggerRequestEntity();
        entity.setRID(IDUtil.getRID());
        entity.setAppKey(device.getAppKeyFromMetaData());
        entity.setAlias(alias);
        entity.setRegisterId(registerId);

        String deviceToken = null;
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equalsIgnoreCase(PushType.HUAWEI_PUSH_TAG)) { // 华为推送
            deviceToken = StoreUtil.readHuaWeiPushToken(app.getContext());
            entity.setPushType(PushType.HUAWEI_PUSH);
            LogUtils.i("Get HuaWeiPush token:" + deviceToken);
        } else if (manufacturer.equalsIgnoreCase(PushType.XIAOMI_PUSH_TAG)) { // 小米推送
            deviceToken = MiPushClient.getRegId(app.getContext());
            entity.setPushType(PushType.XIAOMI_PUSH);
            LogUtils.i("Get XiaoMiPush regId:" + deviceToken);
        }

        if (!TextUtils.isEmpty(deviceToken)) {
            entity.setAndroidToken(deviceToken);
        }

        LogUtils.d(TAG,"create createPushSyncTriggerRequestEntity:" + entity.toString());
        return entity;
    }

    public PushSyncRequestEntity createPushSyncRequestEntity(String alias, long syncKey, int mode, int pageSize, long registerId) {
        Device device = app.getPlatform().getDevice();
        PushSyncRequestEntity entity = new PushSyncRequestEntity();
        entity.setRID(IDUtil.getRID());
        entity.setAppKey(device.getAppKeyFromMetaData());
        entity.setAlias(alias);
        entity.setSyncKey(syncKey);
        entity.setMode(mode);
        entity.setRegisterId(registerId);
        entity.setPageSize(pageSize);
        LogUtils.d(TAG,"create createPushSyncRequestEntity:" + entity.toString());
        return entity;
    }

    public PushSyncFinAckRequestEntity createPushSyncFinAckRequestEntity(String alias, long syncKey, long registerId) {
        Device device = app.getPlatform().getDevice();
        PushSyncFinAckRequestEntity entity = new PushSyncFinAckRequestEntity();
        entity.setRID(IDUtil.getRID());
        entity.setAlias(alias);
        entity.setSyncKey(syncKey);
        entity.setRegisterId(registerId);
        entity.setAppKey(device.getAppKeyFromMetaData());
        LogUtils.d(TAG,"create createPushSyncFinAckRequestEntity:" + entity.toString());
        return entity;
    }

}
