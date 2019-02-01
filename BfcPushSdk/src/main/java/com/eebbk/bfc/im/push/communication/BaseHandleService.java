package com.eebbk.bfc.im.push.communication;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.SparseArray;

import com.eebbk.bfc.im.push.util.LogUtils;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseHandleService extends IntentService {

    private String name;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public BaseHandleService(String name) {
        super(name);
        this.name = name;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(name + " created...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(name + " startId:" + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.d(name + " onHandleIntent...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(name + " destroy...");
    }

    /**
     * Helper for the common pattern of implementing a {@link BroadcastReceiver}
     * that receives a device wakeup event and then passes the work off
     * to a {@link android.app.Service}, while ensuring that the
     * device does not go back to sleep during the transition.
     *
     * <p>This class takes care of creating and managing a partial wake lock
     * for you; you must request the {@link android.Manifest.permission#WAKE_LOCK}
     * permission to use it.</p>
     *
     * <h3>Example</h3>
     *
     * <p>A {@link WakefulBroadcastReceiver} uses the method
     * {@link WakefulBroadcastReceiver#startWakefulService startWakefulService()}
     * to start the service that does the work. This method is comparable to
     * {@link android.content.Context#startService startService()}, except that
     * the {@link WakefulBroadcastReceiver} is holding a wake lock when the service
     * starts. The intent that is passed with
     * {@link WakefulBroadcastReceiver#startWakefulService startWakefulService()}
     * holds an extra identifying the wake lock.</p>
     *
     * {@sample development/samples/Support4Demos/src/com/example/android/supportv4/content/SimpleWakefulReceiver.java complete}
     *
     * <p>The service (in this example, an {@link IntentService}) does
     * some work. When it is finished, it releases the wake lock by calling
     * {@link WakefulBroadcastReceiver#completeWakefulIntent
     * completeWakefulIntent(intent)}. The intent it passes as a parameter
     * is the same intent that the {@link WakefulBroadcastReceiver} originally
     * passed in.</p>
     *
     * {@sample development/samples/Support4Demos/src/com/example/android/supportv4/content/SimpleWakefulService.java complete}
     */
    public abstract static class WakefulBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "WakefulBroadcastReceiver";

        private static final String EXTRA_WAKE_LOCK_ID = "com.xtc.sync.wakelockid";

        private static final SparseArray<PowerManager.WakeLock> mActiveWakeLocks
                = new SparseArray<PowerManager.WakeLock>();
        private static AtomicInteger mNextId = new AtomicInteger(1);

        /**
         * Do a {@link android.content.Context#startService(Intent)
         * Context.startService}, but holding a wake lock while the service starts.
         * This will modify the Intent to hold an extra identifying the wake lock;
         * when the service receives it in {@link android.app.Service#onStartCommand
         * Service.onStartCommand}, it should pass back the Intent it receives there to
         * {@link #completeWakefulIntent(Intent)} in order to release
         * the wake lock.
         *
         * @param context The Context in which it operate.
         * @param intent The Intent with which to start the service, as per
         * {@link android.content.Context#startService(Intent)
         * Context.startService}.
         */
        public static ComponentName startWakefulService(Context context, Intent intent) {
            synchronized (mActiveWakeLocks) {
                int id = mNextId.getAndIncrement();
                if (mNextId.get() <= 0) {
                    mNextId.set(1);
                }

                intent.putExtra(EXTRA_WAKE_LOCK_ID, id);
                ComponentName comp = context.startService(intent);
                if (comp == null) {
                    return null;
                }

                PowerManager pm = (PowerManager)context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "wake:" + comp.flattenToShortString());
                wl.setReferenceCounted(false);
                wl.acquire(60*1000);
                mActiveWakeLocks.put(id, wl);
                LogUtils.d(TAG, "acquire a wake lock id #" + id);
                return comp;
            }
        }

        /**
         * Finish the execution from a previous {@link #startWakefulService}.  Any wake lock
         * that was being held will now be released.
         *
         * @param intent The Intent as originally generated by {@link #startWakefulService}.
         * @return Returns true if the intent is associated with a wake lock that is
         * now released; returns false if there was no wake lock specified for it.
         */
        public static boolean completeWakefulIntent(Intent intent) {
            if (intent == null) {
                return false;
            }

            final int id = intent.getIntExtra(EXTRA_WAKE_LOCK_ID, 0);
            if (id == 0) {
                return false;
            }
            synchronized (mActiveWakeLocks) {
                PowerManager.WakeLock wl = mActiveWakeLocks.get(id);
                if (wl != null) {
                    wl.release();
                    mActiveWakeLocks.remove(id);
                    LogUtils.i(TAG, "release the wake lock id #" + id);
                    LogUtils.i(TAG, "mActiveWakeLocks:" + mActiveWakeLocks);
                    return true;
                }
                // We return true whether or not we actually found the wake lock
                // the return code is defined to indicate whether the Intent contained
                // an identifier for a wake lock that it was supposed to match.
                // We just log a warning here if there is no wake lock found, which could
                // happen for example if this function is called twice on the same
                // intent or the process is killed and restarted before processing the intent.
                LogUtils.w(TAG, "No active wake lock id #" + id);
                return true;
            }
        }

    }
}
