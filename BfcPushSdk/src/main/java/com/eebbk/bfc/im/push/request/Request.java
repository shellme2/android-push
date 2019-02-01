package com.eebbk.bfc.im.push.request;

import com.eebbk.bfc.im.push.PushApplication;
import com.eebbk.bfc.im.push.entity.request.RequestEntity;
import com.eebbk.bfc.im.push.listener.OnReceiveFinishListener;
import com.eebbk.bfc.im.push.listener.OnReceiveListener;
import com.eebbk.bfc.im.push.response.Response;
import com.eebbk.bfc.im.push.util.IDUtil;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.RandomUtil;
import com.eebbk.bfc.im.push.util.TimeFormatUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 发送请求的基类，该类负责实现对应的协议请求
 */
public class Request implements Cloneable {
    private static final String TAG = "Request";

    /**
     * 请求发送的时间
     */
    protected long sendTime;

    protected long retryDelayTime;

    protected AtomicInteger retryDelayTag = new AtomicInteger(0);

    /**
     * 发送的数据实体
     */
    protected RequestEntity requestEntity;

    protected PushApplication app;

    /**
     * 请求回调监听
     */
    protected OnReceiveListener onReceiveListener;

    /**
     * 是否已经执行过请求回调，默认false未执行
     */
    protected boolean hasOnReceive;

    /**
     * 默认的内置请求回调监听，为了控制请求回调监听只执行一次，以及避免null指针
     */
    private OnReceiveListener innerListener = new OnReceiveListener() {
        @Override
        public void onReceive(Request request, Response response) {
            if (hasOnReceive) {
                LogUtils.w("hasOnReceive:" + hasOnReceive);
                LogUtils.w("request:" + request.getRequestEntity());
                LogUtils.w("response:" + response.getResponseEntity());
                return;
            }
            if (onReceiveListener != null) {
                LogUtils.i("onReceive request:" + request.getRequestEntity());
                LogUtils.i("onReceive response:" + response.getResponseEntity());
                onReceiveListener.onReceive(request, response);
            }
            hasOnReceive = true;
        }
    };

    /**
     * 该请求是否需要回馈，默认true，需要反馈
     */
    protected boolean isNeedResponse;

    /**
     * 附加参数，用于额外描述请求
     */
    protected Object extra;

    /**
     * 请求超时时间
     */
    protected int timeout = 20000; // 默认的请求超时时间为20s

    /**
     * 请求失败后是否需要重试
     */
    protected boolean isNeedRetry;

    protected int retryCount;

    protected int maxRetryCount; // 默认超时重试次数为5次

    protected boolean onRetry;

    protected OnReceiveFinishListener onReceiveFinishListener = new OnReceiveFinishListener() {
        @Override
        public void onFinish() {
            // 默认监听，不需要处理，避免null指针问题
        }
    };

    /**
     * 请求回馈是否有多个回馈，默认false，只有一个回馈
     */
    protected boolean isMutiResponse;

    Request(PushApplication app, RequestEntity requestEntity) {
        this.app = app;
        this.requestEntity = requestEntity;
        this.isMutiResponse = false; // 默认请求只有一个响应
        this.isNeedResponse = true; // 默认请求是需要响应的
        this.isNeedRetry = true; // 默认请求失败需要重试
        this.maxRetryCount = 5; // 默认最大的重试次数为5次
    }

    public OnReceiveFinishListener getOnReceiveFinishListener() {
        return onReceiveFinishListener;
    }

    public void setOnReceiveFinishListener(OnReceiveFinishListener onReceiveFinishListener) {
        this.onReceiveFinishListener = onReceiveFinishListener;
    }

    public long getSendTime() {
        return sendTime;
    }

    public long getRetryDelayTime() {
        computeDelay();
        return retryDelayTime;
    }

    /**
     * 设置当前请求是否为多次响应请求
     */
    public void setMutiResponse(boolean isMutiResponse) {
        this.isMutiResponse = isMutiResponse;
    }

    /**
     * 判断是否为多次响应请求
     */
    public boolean isMutiResponse() {
        return isMutiResponse;
    }

    public void setNeedResponse(boolean isNeedResponse) {
        this.isNeedResponse = isNeedResponse;
    }

    public boolean isNeedResponse() {
        return isNeedResponse;
    }

    public int getRID() {
        return requestEntity.getRID();
    }

    public int getCommand() {
        return requestEntity.getCommand();
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public boolean isNeedRetry() {
        return isNeedRetry;
    }

    public void setNeedRetry(boolean isNeedRetry) {
        this.isNeedRetry = isNeedRetry;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setRequestEntity(RequestEntity requestEntity) {
        this.requestEntity = requestEntity;
    }

    public RequestEntity getRequestEntity() {
        return requestEntity;
    }

    /**
     * 设置请求响应超时时间，单位ms
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - sendTime > timeout;
    }

    public void setOnReceiveListener(OnReceiveListener onReceiveListener) {
        this.onReceiveListener = onReceiveListener;
    }

    public OnReceiveListener getOnReceiveListener() {
        return onReceiveListener;
    }

    public boolean isOnRetry() {
        return onRetry;
    }

    public void setOnRetry(boolean onRetry) {
        this.onRetry = onRetry;
    }

    /**
     * 异步发送请求数据，如果重复发送则会忽略前一个请求
     */
    public void send() {
        if (requestEntity == null) {
            LogUtils.e( TAG, "request entity is null.");
            return;
        }
        sendImpl();
    }

    protected void retry() {
        send();
    }

    protected void computeDelay() {
        int start = (int) (Math.pow(2, retryDelayTag.get()) * 1000);
        int end = (int) (Math.pow(2, retryDelayTag.get() + 1) * 1000);
        retryDelayTime = (long) RandomUtil.getRandom(start, end);
        if (retryDelayTime >= 5 * 60 * 1000) {
            retryDelayTime = 5 * 60 * 1000;
        } else {
            retryDelayTag.incrementAndGet();
            LogUtils.i("to increment the retryPeriodTag:" + retryDelayTag.get() + ",retryPeriod:" + retryDelayTime);
        }
        LogUtils.i("retry period is [" + TimeFormatUtil.format(retryDelayTime) + "]");
    }

    /**
     * 重发请求，这里有可能会复制失败的Request参数赋值到新clone的Request上，参数都是旧的参数
     * 所以可能导致一开始旧Request的参数不合法，而这些不合法的参数被复制到新的重试的Request
     * 上面，重试的Request的参数也是不合法的，所以重试Request是不可能成功的
     */
    public static void  retryRequest(Request request) {
        if (request == null) {
            throw new NullPointerException("retryRequest,request is null!");
        }

        LogUtils.d("to retry the request...");
        Request newReq;
        try {
            newReq = request.cloneRequest();
            newReq.retry();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(request.getClass().getName() + ":" + e.toString());
        }
    }

    private void sendImpl() {
        this.sendTime = System.currentTimeMillis();
        if (isNeedResponse) {
            app.getRequestManager().remove(this);
            app.getRequestManager().add(this);
        }
        app.enqueueRequest(this);
    }

    public static Request createRequest(PushApplication app, RequestEntity requestEntity) {
        return new Request(app, requestEntity);
    }

    public OnReceiveListener getInnerListener() {
        return innerListener;
    }

    private Request cloneRequest() throws CloneNotSupportedException {
        RequestEntity newRequestEntity = requestEntity.cloneRequestEntity();
        Request newReq = null;
        newRequestEntity.setRID(IDUtil.getRID());
        LogUtils.i("request entity:" + requestEntity);
        LogUtils.i("clone request entity:" + newRequestEntity);
        newReq = (Request) this.clone();
        newReq.hasOnReceive = false;
        newReq.setRequestEntity(newRequestEntity);
        LogUtils.i("request:" + this);
        LogUtils.i("clone request:" + newReq);
        return newReq;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Request{" +
                "sendTime=" + sendTime +
                ", retryDelayTime=" + retryDelayTime +
                ", requestEntity=" + requestEntity +
                ", app=" + app +
                ", onReceiveListener=" + onReceiveListener +
                ", hasOnReceive=" + hasOnReceive +
                ", innerListener=" + innerListener +
                ", isNeedResponse=" + isNeedResponse +
                ", extra=" + extra +
                ", timeout=" + timeout +
                ", isNeedRetry=" + isNeedRetry +
                ", retryCount=" + retryCount +
                ", maxRetryCount=" + maxRetryCount +
                ", onRetry=" + onRetry +
                ", onReceiveFinishListener=" + onReceiveFinishListener +
                ", isMutiResponse=" + isMutiResponse +
                '}';
    }
}
