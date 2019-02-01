package com.eebbk.bfc.im.push.debug.da;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hesn
 * 2018/7/4
 */
public class DaInfo {
    private String functionName;
    private String moduleDetail;
    private String trigValue;
    private String extendSdkVersion;
    private String extendRemoteIp;
    private String extendRemotePort;
    private String extend;

    public DaInfo setFunctionName(String functionName) {
        this.functionName = functionName;
        return this;
    }

    public String getFunctionName() {
        return functionName;
    }

    public DaInfo setModuleDetail(String moduleDetail) {
        this.moduleDetail = moduleDetail;
        return this;
    }

    public String getModuleDetail() {
        return moduleDetail;
    }

    public String getTrigValue() {
        return trigValue;
    }

    public DaInfo setTrigValue(String trigValue) {
        this.trigValue = trigValue;
        return this;
    }

    public DaInfo setExtendSdkVersion() {
        this.extendSdkVersion = com.eebbk.bfc.im.push.version.Build.VERSION.VERSION_NAME;
        return this;
    }

    public DaInfo setExtendRemoteIp(String extendRemoteIp) {
        this.extendRemoteIp = extendRemoteIp;
        return this;
    }

    public DaInfo setExtendRemotePort(String extendRemotePort) {
        this.extendRemotePort = extendRemotePort;
        return this;
    }

    public String getExtend() {
        return extend;
    }

    public DaInfo setExtend(String extend) {
        this.extend = extend;
        return this;
    }

    public String getExtendJson(Context context){
        Map<String, String> extend = new HashMap<>();
        extend.put(Da.extend.package_name, context.getPackageName());
        extend.put(Da.extend.PID, String.valueOf(android.os.Process.myPid()));
        if(!TextUtils.isEmpty(extendSdkVersion)){
            extend.put(Da.extend.SDK_VERSION, extendSdkVersion);
        }
        if(!TextUtils.isEmpty(extendRemoteIp)){
            extend.put(Da.extend.IP, extendRemoteIp);
        }
        if(!TextUtils.isEmpty(extendRemotePort)){
            extend.put(Da.extend.PORT, extendRemotePort);
        }
        setExtend(map2Json(extend));
        return getExtend();
    }

    private static String map2Json(Map map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        return new JSONObject(map).toString();
    }
}
