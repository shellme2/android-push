package com.eebbk.bfc.demo.push.debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.eebbk.bfc.common.app.ToastUtils;
import com.eebbk.bfc.common.devices.MediaLibraryUtils;
import com.eebbk.bfc.common.file.FileUtils;
import com.eebbk.bfc.common.tools.DateUtils;
import com.eebbk.bfc.demo.push.Constant;
import com.eebbk.bfc.demo.push.debug.sendFactory.PushSendProduct;
import com.eebbk.bfc.demo.push.performance.IView;
import com.eebbk.bfc.demo.push.util.ExecutorsUtils;
import com.eebbk.bfc.demo.push.util.TimeUtils;
import com.eebbk.bfc.im.push.debug.DebugBaseTool;
import com.eebbk.bfc.im.push.debug.DebugEventCode;
import com.eebbk.bfc.im.push.debug.DebugEventInfo;
import com.eebbk.bfc.im.push.debug.DebugEventTool;
import com.eebbk.bfc.im.push.util.DateFormatUtil;
import com.eebbk.bfc.im.push.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Desc:
 * Author: ZengJingFang
 * Time:   2017/7/21 17:40
 * Email:  zengjingfang@foxmail.com
 */
public class PushAnalysisTool extends DebugBaseTool{

    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/config/push/debug";

    private static int SEND_TIME_OUT = 60 * 1000;

    private DebugPushSendTool mDebugPushSendTool;
    /**
     * 暂存发送成功，但是没有成功接收的消息
     */
    private List<PushSendProduct> mSendWaiteReceiveList = new CopyOnWriteArrayList<>();
    /**
     * 所有接收未超时的消息(没有匹配上发送请求的)
     */
    private List<DebugEventInfo> mReceivedSuccessCacheList = new CopyOnWriteArrayList<>();
    private IView mIView;

    private Context mContext;
    private String saveFileName;
    private int cacheNum = 0;
    private static final int CACHE_THRESHOLD = 20;

    private static int mSendSuccessCount;
    private static int mSendFailedCount;
    private static int mReceivedCount;
    private static int mReceivedTimeOutCount;

    private static final String SAVE_FILE_CONTENT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String SAVE_FILE_CONTENT_SEPATATOR = "\n";
    private static final String SAVE_FILE_CONTENT_SEND_CONTENT = "发送内容：";
    private static final String SAVE_FILE_CONTENT_SEND_TIME = "发送时间：";
    private static final String SAVE_FILE_CONTENT_RECEIVED_CONTENT = "接收内容：";
    private static final String SAVE_FILE_CONTENT_RECEIVED_TIME = "接收时间：";
    private static final String SAVE_FILE_CONTENT_RECEIVED_DELAYED = "延时时间：";

    Handler mainHandler = new Handler(Looper.getMainLooper());

    public PushAnalysisTool(Context context, IView IView) {
        super("PushAnalysisTool");
        mContext = context.getApplicationContext();
        mDebugPushSendTool = new DebugPushSendTool();

        mDebugPushSendTool.registerPushSendResultCallBack(mPushSendProductResultCallBack);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DebugEventTool.DEBUG_PUSH_EVENT_PUBLISH_ACTION);
        mContext.registerReceiver(mDebugEventReceiver, intentFilter);

        mIView = IView;
        saveFileName = DateFormatUtil.format(DateFormatUtil.FORMAT_2, System.currentTimeMillis());
    }

    public void turnOnDebugMode() {
        mDebugPushSendTool.turnOnDebugMode();
    }

    public void turnOffDebugMode() {
        mDebugPushSendTool.turnOffDebugMode();
    }
    public void setPushDebugMode(int urlMode, String appName,int count, long period){
        mDebugPushSendTool.setPushDebugMode(urlMode,appName,  count,period);
    }

    public void startTest() {
        mDebugPushSendTool.scheduleTask();
    }

    public void pauseTest() {
        mDebugPushSendTool.PauseTask();
    }
    public void endTest() {
        mDebugPushSendTool.PauseTask();
        saveTestReport();
        reset();
    }

    public void saveTestReport() {
        saveTestReport(false);
    }

    public void saveTestReport(boolean isCache) {
        cacheNum++;
        if(isCache){
            if(cacheNum <= CACHE_THRESHOLD){
                return;
            }else {
                cacheNum = 0;
            }
        }
        calibration();
        String filePath;
        if(isCache){
            // 添加一个数据缓存，避免横竖屏，或者各种原因导致最终数据丢失。
            filePath = FILE_PATH + File.separator + saveFileName + File.separator + "cache";
        }else {
            filePath = FILE_PATH +  File.separator + saveFileName;
        }
        String waitReceivedStr = JsonUtil.toJson(new ArrayList<>(mSendWaiteReceiveList));
        String receivedSuccessCacheStr = JsonUtil.toJson(new ArrayList<>(mReceivedSuccessCacheList));

        writeData2SDCard(filePath, "waitReceive.txt", waitReceivedStr);
        writeData2SDCard(filePath, "receivedSuccessCache.txt", receivedSuccessCacheStr);

        String report = "发送成功：" + mSendSuccessCount + " 条"
                +"\n发送失败：" + mSendFailedCount + " 条"
                +"\n正常接收：" + mReceivedCount + " 条"
                +"\n超时接收：" + mReceivedTimeOutCount + " 条"
                +"\n接收失败："+(mSendSuccessCount-mReceivedCount-mReceivedTimeOutCount)+" 条"
                +"\n本次测试成功率："+((float)mReceivedCount*100/(float)mSendSuccessCount)+" %";

        if(!isCache) {
            ToastUtils.getInstance(mContext).s(report);
        }
        writeData2SDCard(filePath, "pushtestreport.txt", report);

        MediaLibraryUtils.scanFile(mContext, filePath);
    }

    private void writeData2SDCard(String path, String fileName, String data){
        if(TextUtils.isEmpty(data)){
            return;
        }
        try {
            File debugFile = new File(path + File.separator + fileName);
            FileUtils.createFileOrExists(debugFile);
            FileUtils.writeFile(debugFile, data, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateSendAndReceived() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                //已在主线程中，可以更新UI
                mIView.onSendUpdate(mSendSuccessCount, mSendFailedCount, mReceivedCount,mReceivedTimeOutCount);
            }
        });
        ExecutorsUtils.execute(new Runnable() {
            @Override
            public void run() {
                saveTestReport(true);
            }
        });
    }

    private void updateTCPConnState(final String state) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                //已在主线程中，可以更新UI
                mIView.onTcpConnStateUpdate(state);
            }
        });
    }

    @Override
    protected synchronized void onHandleMessage(Object obj) {
        // 缓存所有接收到的消息
        DebugEventInfo info = (DebugEventInfo) obj;
        saveDebugEvent2SDCard(info);
        switch (info.getCode()) {
            case DebugEventCode.DEBUG_EVENT_CODE_MSG_RECEIVE:
                // 消息接收
//                List<PushSendProduct> sendProductList = Arrays.asList(new PushSendProduct[mSendWaiteReceiveList.size()]);
//                Collections.copy(sendProductList, mSendWaiteReceiveList);
                List<PushSendProduct> sendProductList = new ArrayList<>(mSendWaiteReceiveList);
                boolean isHad = false;
                for (PushSendProduct sendProduct : sendProductList) {
                    if (dealReceivedSuccess(sendProduct, info)) {
                        isHad = true;
                        break;//结束循环
                    }
                }
                if(!isHad && !TextUtils.isEmpty(info.getContent()) &&
                        (info.getContent().startsWith(Constant.SendRelease.CONTENT_TYPE) || info.getContent().startsWith(Constant.SendDebug.CONTENT_TYPE))){
                    // 先收到推送消息，后更新发送消息保存的列表mSendWaiteReceiveList，导致匹配不上显示异常
                    mReceivedSuccessCacheList.add(info);
                }
                calibration();
                updateSendAndReceived();
                break;
            case DebugEventCode.DEBUG_EVENT_CODE_TCP_CONN_STATE:
                updateTCPConnState(info.getContent());
                break;
            default:

                break;
        }

    }

    /**
     * 检查内容符合要求，但是没有匹配发送列表的推送消息，重新检查一次
     */
    private synchronized void calibration(){
        if(mReceivedSuccessCacheList.isEmpty()){
            return;
        }
        List<DebugEventInfo> cacheList = new ArrayList<>(mReceivedSuccessCacheList);
        // 消息接收
        List<PushSendProduct> sendProductList = new ArrayList<>(mSendWaiteReceiveList);
        for (DebugEventInfo info : cacheList) {
            for (PushSendProduct sendProduct : sendProductList) {
                if (dealReceivedSuccess(sendProduct, info)) {
                    mReceivedSuccessCacheList.remove(info);
                    break;//结束循环
                }
            }
        }
    }

    private synchronized boolean dealReceivedSuccess(PushSendProduct sendProduct, DebugEventInfo info){
        if (sendProduct.getContent().equals(info.getContent())) {
            //本条消息接收成功
            long sendTime = sendProduct.getSendTime();
            long receivedTime = info.getTime();
            //是否超时(>1min)
            if (receivedTime - sendTime < SEND_TIME_OUT) {
                mReceivedCount++;
            } else {
                mReceivedTimeOutCount++;
            }
            //清除缓存中这条记录
            mSendWaiteReceiveList.remove(sendProduct);
            saveReceived2SDCard(sendProduct, info);
            return true;
        }
        return false;
    }

    private String getTimeFormat(long start, long end){
        return TimeUtils.formatTime(end - start);
    }

    // 消息发送回调
    private ResultCallBack<PushSendProduct> mPushSendProductResultCallBack=new ResultCallBack<PushSendProduct>() {
        @Override
        public void onSuccess(PushSendProduct sendProduct) {
            if (DebugEventTool.PUSH_DEBUG_ON.equals(sendProduct.getContent()) || DebugEventTool.PUSH_DEBUG_OFF.equals(sendProduct.getContent())) {
                //
                return;
            }
            saveSendSuccess2SDCard(sendProduct);
            mSendWaiteReceiveList.add(sendProduct);
            mSendSuccessCount++;
            updateSendAndReceived();
        }

        @Override
        public void onFailed(PushSendProduct sendProduct) {
            if (DebugEventTool.PUSH_DEBUG_ON.equals(sendProduct.getContent()) || DebugEventTool.PUSH_DEBUG_OFF.equals(sendProduct.getContent())) {
                //
                return;
            }
            saveSendFailed2SDCard(sendProduct);
            mSendFailedCount++;
            updateSendAndReceived();
        }
    };

    // 广播消息接收器
    private BroadcastReceiver mDebugEventReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            DebugEventInfo debugEventInfo = intent.getParcelableExtra(DebugEventTool.DEBUG_PUSH_EVENT_PUBLISH_KEY);

            sendMessage(debugEventInfo);
        }
    };

    public void reset() {
        mSendSuccessCount=0;
        mSendFailedCount=0;
        mReceivedCount=0;
        mReceivedTimeOutCount=0;
        mSendWaiteReceiveList.clear();
        mReceivedSuccessCacheList.clear();
        mDebugPushSendTool.reset();
    }

    @Override
    public void destroy() {
        super.destroy();
        saveTestReport();
        mDebugPushSendTool.unRegisterPushSendResultCallBack();
        mContext.unregisterReceiver(mDebugEventReceiver);
        reset();
        mDebugPushSendTool.destroy();
        mContext = null;
    }

    /**
     * 成功接收统计
     */
    private void saveReceived2SDCard(final PushSendProduct sendProduct, final DebugEventInfo receivedProduct){
        ExecutorsUtils.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (PushAnalysisTool.class) {
                    long sendTimeL = sendProduct.getSendTime();
                    long receivedTimeL = receivedProduct.getTime();
                    String content = getContentFormat(
                            receivedProduct.getContent(),
                            SAVE_FILE_CONTENT_SEND_CONTENT + JsonUtil.toJson(sendProduct),
                            SAVE_FILE_CONTENT_RECEIVED_CONTENT + JsonUtil.toJson(receivedProduct),
                            SAVE_FILE_CONTENT_SEND_TIME + DateUtils.format(sendTimeL, SAVE_FILE_CONTENT_DATE_FORMAT),
                            SAVE_FILE_CONTENT_RECEIVED_TIME + DateUtils.format(receivedTimeL, SAVE_FILE_CONTENT_DATE_FORMAT),
                            SAVE_FILE_CONTENT_RECEIVED_DELAYED + getTimeFormat(sendTimeL, receivedTimeL) + (receivedTimeL - sendTimeL < SEND_TIME_OUT ? "" : " (timeout)"));
                    saveContent2SDCard(getSDCardSavePath("received.txt"), content, true);
                }
            }
        });
    }

    /**
     * 保存发送成功信息
     */
    private void saveSendSuccess2SDCard(final PushSendProduct sendProduct){
        ExecutorsUtils.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (PushAnalysisTool.class) {
                    String content = getContentFormat(
                            sendProduct.getContent(),
                            SAVE_FILE_CONTENT_SEND_CONTENT + JsonUtil.toJson(sendProduct),
                            SAVE_FILE_CONTENT_SEND_TIME + DateUtils.format(sendProduct.getSendTime(), SAVE_FILE_CONTENT_DATE_FORMAT));
                    saveContent2SDCard(getSDCardSavePath("sendSuccess.txt"), content, true);
                }
            }
        });
    }

    /**
     * 保存发送失败信息
     */
    private void saveSendFailed2SDCard(final PushSendProduct sendProduct){
        ExecutorsUtils.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (PushAnalysisTool.class) {
                    String content = getContentFormat(
                            sendProduct.getContent(),
                            SAVE_FILE_CONTENT_SEND_CONTENT + JsonUtil.toJson(sendProduct),
                            SAVE_FILE_CONTENT_SEND_TIME + DateUtils.format(sendProduct.getSendTime(), SAVE_FILE_CONTENT_DATE_FORMAT));
                    saveContent2SDCard(getSDCardSavePath("sendFailed.txt"), content, true);
                }
            }
        });
    }

    /**
     * 保存收到的信息
     */
    private void saveDebugEvent2SDCard(final DebugEventInfo debugEventInfo){
        ExecutorsUtils.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (PushAnalysisTool.class) {
                    String content = getContentFormat(
                            debugEventInfo.getContent(),
                            SAVE_FILE_CONTENT_RECEIVED_CONTENT + JsonUtil.toJson(debugEventInfo),
                            SAVE_FILE_CONTENT_RECEIVED_TIME + DateUtils.format(debugEventInfo.getTime(), SAVE_FILE_CONTENT_DATE_FORMAT));
                    saveContent2SDCard(getSDCardSavePath("debugEvents.txt"), content, true);
                }
            }
        });
    }

    private void saveContent2SDCard(String path, String content, boolean append){
        try {
            FileUtils.createFileOrExists(path);
            FileUtils.writeFile(new File(path), content, append);
            MediaLibraryUtils.scanFile(mContext, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getContentFormat(String...contents){
        if(contents == null || contents.length <= 0){
            return "";
        }
        StringBuffer sb = new StringBuffer(contents.length * 10);
        for (String content : contents) {
            sb.append(content).append(SAVE_FILE_CONTENT_SEPATATOR);
        }
        sb.append(SAVE_FILE_CONTENT_SEPATATOR).append(SAVE_FILE_CONTENT_SEPATATOR).append(SAVE_FILE_CONTENT_SEPATATOR);
        return sb.toString();
    }

    private String getSDCardSavePath(String fileName){
        return FILE_PATH +  File.separator + saveFileName + File.separator + fileName;
    }
}
