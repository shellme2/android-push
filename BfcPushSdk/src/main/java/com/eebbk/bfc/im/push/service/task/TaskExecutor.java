package com.eebbk.bfc.im.push.service.task;

import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskExecutor {
    private static final String TAG = "TaskExecutor";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private final int MAX_TASK_SIZE;

    private final BlockingQueue<Runnable> sPoolWorkQueue;

    private volatile boolean started;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "Task Thread #" + mCount.getAndIncrement());
        }
    };

    private final ArrayDeque<Task> mTasks = new ArrayDeque<>();

    private final ArrayDeque<Task> cacheTasks = new ArrayDeque<>();

    private Task mActive;

    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR;


    public TaskExecutor() {
        this(128);
    }

    public TaskExecutor(int maxTaskSize) {
        this(maxTaskSize, false);
    }

    public TaskExecutor(int maxTaskSize, boolean singleTask) {
        if (maxTaskSize <= 0) {
            throw new IllegalArgumentException("max task size must not <= 0!");
        }
        MAX_TASK_SIZE = maxTaskSize;
        if (singleTask) {
            sPoolWorkQueue = new PriorityBlockingQueue(maxTaskSize);
            THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, sPoolWorkQueue, sThreadFactory);
        } else {
            sPoolWorkQueue = new LinkedBlockingQueue<>(maxTaskSize);
            THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
        }
    }


    private synchronized void scheduleNext() {
        if (!started) {
            LogUtils.w("task started status:" + started);
            return;
        }
        if ((mActive = mTasks.poll()) != null) {
            if (!THREAD_POOL_EXECUTOR.isShutdown()) {
                THREAD_POOL_EXECUTOR.execute(mActive);
            } else {
                LogUtils.e( TAG, "THREAD_POOL_EXECUTOR is shutdown!!!");
            }
        }
    }

    private synchronized int size() {
        return mTasks.size();
    }

    private synchronized Runnable abandonFirst() {
        return mTasks.poll();
    }

    public synchronized void cancelAll() {
        mTasks.clear();
        cacheTasks.clear();
        LogUtils.d("cancel task executor");
    }

    public synchronized void shutdown() {
        mTasks.clear();
        cacheTasks.clear();
        THREAD_POOL_EXECUTOR.shutdownNow();
        LogUtils.w("shutdown task executor");
    }

    public synchronized void execute(final Task task) {
        if (size() > MAX_TASK_SIZE) {
            abandonFirst();
            LogUtils.w("abandon a task,size:" + size());
        }
        mTasks.offer(new Task() {
            public void run() {
                try {
                    task.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (mActive == null) {
            scheduleNext();
        }
        LogUtils.v("execute a task,size:" + size());
    }

    public synchronized void cacheTask(Task task) {
        cacheTasks.offer(task);
        LogUtils.v("cache a task,size:" + cacheTasks.size());
    }

    public synchronized void executeCacheTasks() {
        Task task ;
        while ((task = cacheTasks.poll()) != null) {
            execute(task);
        }
    }

    public void start() {
        started = true;
        scheduleNext();
        executeCacheTasks();
        LogUtils.d("start task executor");
    }

    public void stop() {
        started = false;
        LogUtils.d("stop task executor");
    }
}
