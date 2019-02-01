package com.eebbk.bfc.im.push.service.heartbeat;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.eebbk.bfc.im.push.communication.SyncAction;
import com.eebbk.bfc.im.push.util.LogUtils;
import com.eebbk.bfc.im.push.util.PublicValueStoreUtil;

/**
 * @author liuyewu
 * @company EEBBK
 * @function keep connection service live
 * @date 2016/10/26
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class KeepLiveService extends JobService {

    private static final String TAG = "KeepLiveService";
    private boolean isServiceExit = false;
    private volatile  Service mKeepLiveService = null;
    private volatile static boolean isStart = true;
//    public volatile static boolean isFirst=false;

    public  boolean isServiceLive() {
        return mKeepLiveService != null;
    }

    /**
     * false: 该系统假设任何任务运行不需要很长时间并且到方法返回时已经完成。
     * true: 该系统假设任务是需要一些时间并且当任务完成时需要调用jobFinished()告知系统。
     */
    @Override
    public boolean onStartJob(JobParameters params) {

        if (!isServiceLive() && !isServiceExit) {
            LogUtils.i(TAG, "connection service restart from job service---");
            startConnectService();
            return true;
        }
//        isFirst=false;
        LogUtils.i(TAG, "job service start return false ---");
        return true;
    }

    /**
     * 当收到取消请求时，该方法是系统用来取消挂起的任务的。
     * 如果onStartJob()返回false，则系统会假设没有当前运行的任务，故不会调用该方法。
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        LogUtils.i(TAG, "job service stop return false ---");
        return false;
    }

    private void startConnectService() {
        String hostPackageName = PublicValueStoreUtil.getHostPackageName();
        if (TextUtils.isEmpty(hostPackageName)) {
            LogUtils.e(TAG, "host package name is empty, so we can not start connect service for keep live !!!");
            return;
        }
        if (isStart) {
            Intent intent = new Intent();
            intent.setPackage(hostPackageName);
            intent.setAction(SyncAction.CONNECT_SWITCH_SERVICE_ACTION);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES); // 唤醒被强制停止的app
            intent.putExtra(ConnectSwitchService.BUNDLE_KEY_SERVICE_SWITCH, ConnectSwitchService.BUNDLE_VALUE_SERVICE_SWITCH_ON);
            intent.setComponent(new ComponentName(intent.getPackage(), ConnectSwitchService.class.getName()));
            startService(intent);
            isStart = false;
        }
    }

    public  void startJobScheduler(Service service) {
        mKeepLiveService = service;
        Context context = service.getApplicationContext();
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int jobId = 1;
        JobInfo jobInfo = new JobInfo.Builder(jobId, new ComponentName(context, KeepLiveService.class))
//                .setMinimumLatency(5000)// 设置任务运行最少延迟时间
//                .setOverrideDeadline(60000)// 设置deadline，若到期还没有达到规定的条件则会开始执行
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)// 设置网络条件
                .setRequiresCharging(true)// 设置是否充电的条件
//                .setRequiresDeviceIdle(false)// 设置手机是否空闲的条件
                .setPeriodic(2*60*60*1000).setPersisted(true).build();
        scheduler.schedule(jobInfo);
        LogUtils.i(TAG, "job schedule start ---");
    }

}
