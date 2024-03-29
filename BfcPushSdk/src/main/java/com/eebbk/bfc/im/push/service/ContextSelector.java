package com.eebbk.bfc.im.push.service;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.bean.AppPushInfo;
import com.eebbk.bfc.im.push.entity.Command;
import com.eebbk.bfc.im.push.entity.response.ResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncFinResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncInformResponseEntity;
import com.eebbk.bfc.im.push.entity.response.push.PushSyncResponseEntity;
import com.eebbk.bfc.im.push.error.ErrorCode;
import com.eebbk.bfc.im.push.util.AppUtil;
import com.eebbk.bfc.im.push.util.IDUtil;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextSelector {

    private static final String TAG = "ContextSelector";

    public static Context selectContextByPkgName(Context hostCtx, String pkgName) {
        if(hostCtx == null || TextUtils.isEmpty(pkgName)){
            return null;
        }
        Context targetContext = null;
        try {
            LogUtils.w(TAG,"selectContextByPkgName context and packageName is :"+hostCtx+"::"+pkgName);
            targetContext = hostCtx.createPackageContext(pkgName, Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG, ErrorCode.EC_RECEIVE_DATA_PACKAGE_NULL,e);
        }
        return targetContext;
    }

    public static Context selectContextByRid(Context hostCtx, int rid, Map<String, AppPushInfo> bindPkgNameMap) {
//        LogUtils.i("bindPkgNameMap:" + bindPkgNameMap);
        Context targetContext = null;
        Map<String, AppPushInfo> map = new HashMap<>(bindPkgNameMap);
        for (Map.Entry<String, AppPushInfo> entry : map.entrySet()) {
            AppPushInfo appPushInfo = entry.getValue();
            if (appPushInfo == null) {
                LogUtils.w("appPushInfo is null,pkgName:" + entry.getKey());
                continue;
            }
            Context ctx = selectContextByPkgName(hostCtx, appPushInfo.getPkgName());
            if (ctx != null && AppUtil.checkRidTag(ctx, rid)) {
                targetContext = ctx;
                LogUtils.i("selectContextByRid,rid:" + rid + ",pkgName:" + targetContext.getPackageName());
                break;
            }
        }
        return targetContext;
    }

    /**
     * Class: ContextSelector
     * Tag: 宿主选择
     * Ref: ConnectionService.deliverResponseEntity()
     * Fun: 筛选出对应的app
     */
    public static List<Context> selectReceiveContexts(Context context, ResponseEntity responseEntity
            , Map<String, AppPushInfo> bindPkgNameMap) {
        int command = responseEntity.getCommand();
        List<Context> contextList = new ArrayList<>();
        if (IDUtil.hasRIDField(responseEntity)) {
            if (command == Command.PUSH_SYNC_RESPONSE) { // 推送同步响应用包名来分发
                PushSyncResponseEntity pushSyncResponseEntity = (PushSyncResponseEntity) responseEntity;
                Context pkgNameContext = ContextSelector.selectContextByPkgName(context, pushSyncResponseEntity.getPkgName());
                if (pkgNameContext != null) {
                    contextList.add(pkgNameContext);
                }
                LogUtils.d("select context by packageName:" + pushSyncResponseEntity.getPkgName());
            } else if (command == Command.PUSH_SYNC_FIN) { // 推送同步响应结束通过包名来分发
                PushSyncFinResponseEntity pushSyncFinResponseEntity = (PushSyncFinResponseEntity) responseEntity;
                Context pkgNameContext = ContextSelector.selectContextByPkgName(context, pushSyncFinResponseEntity.getPkgName());
                if (pkgNameContext != null) {
                    contextList.add(pkgNameContext);
                }
                LogUtils.d("select context by packageName:" + pushSyncFinResponseEntity.getPkgName());
            }

            if (contextList.isEmpty()) {
                Context ridContext = ContextSelector.selectContextByRid(context, responseEntity.getRID(), bindPkgNameMap);
                if (ridContext != null) {
                    contextList.add(ridContext);
                }
                LogUtils.d("select context by rid:" + responseEntity.getRID());
            } else {

            }

        } else {
            if (command == Command.PUSH_SYNC_INFORM) { // 推送同步通知用包名来分发
                PushSyncInformResponseEntity pushSyncInformResponseEntity = (PushSyncInformResponseEntity) responseEntity;

                LogUtils.e(TAG, "response for dispatcher package name:  " + pushSyncInformResponseEntity.getPkgName());

                Context pkgNameContext = ContextSelector.selectContextByPkgName(context, pushSyncInformResponseEntity.getPkgName());
                if (pkgNameContext != null) {
                    contextList.add(pkgNameContext);
                }
            } else {
                // 原则上这里是永远不会跑到的
                LogUtils.e(TAG, "error response:" + responseEntity);
            }
        }
        return contextList;
    }
}
