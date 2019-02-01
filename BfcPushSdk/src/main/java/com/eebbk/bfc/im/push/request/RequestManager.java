package com.eebbk.bfc.im.push.request;

import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.SyncApplication;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.response.ResponseCreator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 请求管理器，记录所有的请求，用于匹配用户的请求响应
 */
public class RequestManager {

    /**
     * 请求队列
     */
    private List<Request> requests = new ArrayList<>();

    private SyncApplication app;

    public RequestManager(SyncApplication app) {
        this.app = app;
    }

    public synchronized void clear() {
        requests.clear();
    }

    public synchronized void add(Request request) {
        if (requests.add(request)) {
            LogUtils.d("add a request,RID::command==" + request.getRID()+"::"+request.getCommand());
        }
        app.getRequestSweeper().start();
    }

    /**
     * 移除请求响应为单个的请求
     * @param request 请求
     */
    public synchronized void removeOnceResponse(Request request) {
        if (request.isMutiResponse()) {
            LogUtils.d("remove fail a request,RID::command==" + request.getRID()+"::"+request.getCommand());
            return;
        }
        if (requests.remove(request)) {
            LogUtils.d("remove a request,RID::command==" + request.getRID()+"::"+request.getCommand());
            // 这里调用的时候，request有可能已经在定时器中被移除，因此要判断
            if (request != null) {
                request.getOnReceiveFinishListener().onFinish(); // 请求已移除，标记完成
            }
        }
    }

    /**
     * 移除请求记录
     */
    public synchronized void remove(Request request) {
        if (requests.remove(request)) {
            LogUtils.d("remove a request,RID::command==" + request.getRID()+"::"+request.getCommand());
            // 这里调用的时候，request有可能已经在定时器中被移除，因此要判断
            if (request != null) {
                request.getOnReceiveFinishListener().onFinish(); // 请求已移除，标记完成
            }
        }
    }

    /**
     * 查找指定流水号的请求
     * @param rId 流水号
     */
    public synchronized Request find(int rId) {
        for (Request request : requests) {
            if (request.getRID() == rId) {
                return request;
            }
        }
        return null;
    }

    public synchronized List<Request> search(int command) {
        List<Request> list = new ArrayList<>();
        for (Request request : requests) {
            if (request.getCommand() == command) {
                list.add(request);
            }
        }
        return list;
    }

    public synchronized boolean isEmpty() {
        return requests.isEmpty();
    }

    /**
     * 分发队列中的超时请求
     */
    public synchronized int dispatchTimeoutRequest() {
        if (requests.isEmpty()) {
            return 0;
        }
        int dispatchCount = 0;
        int clearCount = 0;
        List<Request> timeoutRequests = new ArrayList<>();
        for (Iterator<Request> it = requests.iterator(); it.hasNext();) {
            Request r = it.next();
            if (r.isTimeout()) {
                timeoutRequests.add(r);
            }
        }
        dispatchCount = timeoutRequests.size();
        for (Request r : timeoutRequests) {
            r.getOnReceiveFinishListener().onFinish(); // 请求已移除，标记完成
            Response response = ResponseCreator.createTimeoutResponse(app, r);
            app.getDispatcher().dispatch(response);
            LogUtils.d("dispatch a timeout request,RID:" + r.getRID());
        }
        clearCount = timeoutRequests.size();
        if (clearCount > 0) {
            requests.removeAll(timeoutRequests);
            timeoutRequests.clear();
            LogUtils.d("clear [" + clearCount + "] requests.");
        }
        return dispatchCount;
    }

}
