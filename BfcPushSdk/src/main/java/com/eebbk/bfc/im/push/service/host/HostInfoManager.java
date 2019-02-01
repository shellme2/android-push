package com.eebbk.bfc.im.push.service.host;

import android.text.TextUtils;

import com.eebbk.bfc.im.push.bean.HostInfo;
import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HostInfoManager {

    private List<HostInfo> hostInfoList = new ArrayList<>();

    private volatile static HostInfoManager hostInfoManager;

    public static HostInfoManager getInstance() {
        if (hostInfoManager == null) {
            synchronized (HostInfoManager.class) {
                if (hostInfoManager == null) {
                    hostInfoManager = new HostInfoManager();
                }
            }
        }
        return hostInfoManager;
    }

    private HostInfoManager() {

    }

    public synchronized boolean add(HostInfo hostInfo) {
        if (!checkHostInfo(hostInfo)) {
            return false;
        }
        if (hostInfoList.contains(hostInfo)) {
            LogUtils.w("There is a host info ("+ hostInfo +") with the same hostname and port in the host info list.");
            return false;
        }
        LogUtils.i("add host info success:" + hostInfo);
        return hostInfoList.add(hostInfo);
    }

    private boolean checkHostInfo(HostInfo hostInfo) {
        if (hostInfo == null) {
            return false;
        }
        if (TextUtils.isEmpty(hostInfo.getHostname()) || hostInfo.getPort() <= 0 || hostInfo.getPort() > 65535) {
            LogUtils.test("hostInfo:" + hostInfo);
            return false;
        }
        return true;
    }

    public synchronized HostInfo get(String hostname, int port) {
        if (TextUtils.isEmpty(hostname) || port <= 0 || port > 65535) {
            LogUtils.test("hostname or port is error,hostname:" + hostname + ",port:" + port);
            return null;
        }
        for (HostInfo h : hostInfoList) {
            if (hostname.equals(h.getHostname()) && port == h.getPort()) {
                return h;
            }
        }
        return null;
    }

    public synchronized HostInfo switchNextHost(HostInfo hostInfo) {
        if (hostInfoList.size() == 0) {
            return hostInfo;
        }
        HostInfo nextHostInfo = null;
        int index = 0;
        if (hostInfo != null) {
            index = hostInfoList.indexOf(hostInfo);
            if (index == -1) {
                index = 0;
            }
            if (index + 1 >= hostInfoList.size()) {
                nextHostInfo = hostInfoList.get(0);
            } else {
                nextHostInfo = hostInfoList.get(index + 1);
            }
        } else {
            nextHostInfo = hostInfoList.get(0);
        }
        LogUtils.i("switchNextHost:" + nextHostInfo);
        return nextHostInfo;
    }

    public synchronized boolean isLast(HostInfo hostInfo) {
        if (hostInfoList.size() == 0) {
            return true;
        }
        return hostInfoList.indexOf(hostInfo) == (hostInfoList.size() - 1);
    }

    public synchronized int indexOf(HostInfo hostInfo) {
        return hostInfoList.indexOf(hostInfo);
    }

    public synchronized void clear() {
        for (HostInfo h : hostInfoList) {
            h.clear();
        }
        hostInfoList.clear();
        LogUtils.d("host info has been clear.");
    }

    public synchronized HostInfo getHostInfoOfLeastFailExcept(HostInfo hostInfo) {
        HostInfo hi = null;
        List<HostInfo> list = sortByFailCountExcept(hostInfo);
        int size = list.size();
        if (size <= 0) {
            LogUtils.i("default host info:" + hi);
        } else {
            hi = list.get(0);
        }
        list.clear();
        if (hi == null) {
            hi = hostInfo;
        }
        LogUtils.i("switch host info:" + hi);
        return hi;
    }

    private List<HostInfo> sortByFailCountExcept(HostInfo hostInfo) {
        return sortExcept(hostInfo, 0);
    }

    private List<HostInfo> sortExcept(HostInfo hostInfo, final int type) {
        List<HostInfo> list = new ArrayList<>(hostInfoList);
        if (hostInfo != null) {
            list.remove(hostInfo);
        }
        if (list.isEmpty()) {
            return list;
        }
        Collections.sort(list, new Comparator<HostInfo>() {
            @Override
            public int compare(HostInfo lhs, HostInfo rhs) {
                boolean compare;
                if (type == 0) {
                    compare = lhs.getFailCount() >= rhs.getFailCount();
                } else if (type == 1) {
                    compare = lhs.getDisconnectedCount() >= rhs.getDisconnectedCount();
                } else {
                    compare = lhs.getSuccessCount() <= rhs.getSuccessCount();
                }
                if (compare) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return list;
    }

    @Override
    public String toString() {
        synchronized (this) {
            return hostInfoList.toString();
        }
    }
}
