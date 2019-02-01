package com.eebbk.bfc.im.push.util;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncExecutorUtil {

    //构造函数私有，防止恶意新建
    private AsyncExecutorUtil(){}

    private static AsyncExecutor asyncExecutor = create();

    private static final int MAX_ACTIVE_COUNT = 100;

    private static AsyncExecutor create() {
        LogUtils.d("create a new async executor...");
        return new AsyncExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }

    public static void runOnBackground(final Runnable r) {
        // 用线程池执行线程任务并且限制线程池同一时间执行任务的数量，避免过多线程任务执行造成内存溢出
        int activeCount = asyncExecutor.getActiveCount();
        if (activeCount > MAX_ACTIVE_COUNT) {
            LogUtils.w("async executor active count:" + activeCount + ",max active count:" + MAX_ACTIVE_COUNT + ",to shutdown all executing task...");
            asyncExecutor.shutdownNow();
            asyncExecutor = create();
        }
        asyncExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.d("AsyncExecutorUtil start a thread");
                r.run();
                LogUtils.d("AsyncExecutorUtil finish a thread");
            }
        });
    }

    public static void runOnMainThread(Runnable r) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(r);
    }

    static class AsyncExecutor extends ThreadPoolExecutor {

        public AsyncExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        public void execute(Runnable command) {
            try {
                super.execute(command);
                LogUtils.d("execute a task...");
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }

        @Override
        public int getPoolSize() {
            int poolSize = super.getPoolSize();
            LogUtils.i("async executor pool size:" + poolSize);
            return poolSize;
        }

        @Override
        public int getActiveCount() {
            int activeCount = super.getActiveCount();
            LogUtils.i("async executor active count:" + activeCount);
            return activeCount;
        }

        @Override
        public List<Runnable> shutdownNow() {
            List<Runnable> shutdownRunnableList = super.shutdownNow();
            if (shutdownRunnableList != null) {
                LogUtils.i("shutdown the executing task size:" + shutdownRunnableList.size());
            }
            return shutdownRunnableList;
        }
    }
}
