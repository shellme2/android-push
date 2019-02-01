package com.eebbk.bfc.im.push.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.eebbk.bfc.im.push.communication.SyncAction;

import java.util.List;

public class IntentUtil {

    //构造函数私有，防止恶意新建
    private IntentUtil(){}

    public static Intent createIntent(Context context, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.addCategory(SyncAction.TAG);
        intent.setPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app

        if (Build.VERSION.SDK_INT > 20) {
            Intent intent1 = getExplicitIntent(context, intent);
            if (intent1 !=null) {
                intent = intent1;
            }else{
                LogUtils.e("IntentUtil","getExplicitIntent is null !!! ");
            }
        }
        return intent;
    }


    private static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

}
