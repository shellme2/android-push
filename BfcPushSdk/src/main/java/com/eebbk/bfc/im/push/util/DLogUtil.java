package com.eebbk.bfc.im.push.util;

import android.os.Environment;

import com.eebbk.bfc.bfclog.BfcLogger;
import com.eebbk.bfc.bfclog.inner.LogInner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Desc:    用于把log文件 记录到sd卡中
 * Author: ZengJingFang
 * Time:   2017/5/13 16:00
 * Email:  zengjingfang@foxmail.com
 */
public class DLogUtil {
    private final static String TAG = "DLogUtil";

    private final static String DLOG_HEARTBEAT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/config/debug/bfc/push/heartbeat";


    private boolean isThreadStarted = false;
    private BlockingQueue<LogSaveInfo> logStringQueue;

    private static DLogUtil sInstance;


    /**
     * 需要保存的log的信息
     */
    static class LogSaveInfo {

        String savePath;
        String log;

        LogSaveInfo(String log, String savePath) {
            this.log = log;
            this.savePath = savePath;
        }
    }

    /**
     * 确保保存log的文件存在, 或者能创建成功
     */
    static void ensureSaveLocationLegal(String saveLocation) {
        File targetFile = new File(saveLocation);
        if (targetFile.isDirectory()) {
            throw new IllegalArgumentException("传入的log保存路径是文件夹,请传具体的文件");
        }

        if (!createFileOrExists(targetFile)) {
            throw new IllegalArgumentException("传入的log保存文件路径不存在,并且自动创建失败; 请确认, 或者关闭BfcLog库的log保存");
        }
    }


    public static DLogUtil getInstance() {
        if (sInstance == null) {
            synchronized (DLogUtil.class) {
                if (sInstance == null) {
                    sInstance = new DLogUtil();
                }
            }
        }

        return sInstance;
    }

    private DLogUtil() {
        logStringQueue = new LinkedBlockingQueue<>();
        ensureSaveLocationLegal(DLOG_HEARTBEAT);

    }

    public void heartbeat(String heartbeat) {
        LogSaveInfo saveInfo = new LogSaveInfo("","");
        if (!isThreadStarted) {
            ExecutorsUtils.execute(new DLogUtil.WriteToStorageRunnable());
        }
        logStringQueue.add(saveInfo);
    }


    private Map<String, Writer> fileWriteMap = new HashMap<>();

    private Writer openFile(String filePath) {
        if (fileWriteMap.get(filePath) != null) {
            return fileWriteMap.get(filePath);
        }

        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
            } catch (IOException e) {
                BfcLogger.e(TAG, "创建log保存的文件失败", e);
            }
        }

        Writer os = null;
        try {
            os = new FileWriter(targetFile, true);
        } catch (IOException e) {
            BfcLogger.e(TAG, "创建log保存的输出流失败", e);
        }

        fileWriteMap.put(filePath, os);
        return os;
    }

    private class WriteToStorageRunnable implements Runnable {
        boolean needExit = false;

        @Override
        public void run() {
            isThreadStarted = true;

            while (true) {
                try {
                    final LogSaveInfo saveInfo = logStringQueue.take();
                    final Writer osw = openFile(saveInfo.savePath);

                    if (osw != null) {
                        osw.write(saveInfo.log);
                        osw.flush();
                    } else {
                        BfcLogger.e(TAG, "保存log文件的数据输出流未创建成功, 请检查或者关闭BfcLog库的log保存");
                    }
                } catch (InterruptedException e) {
                    BfcLogger.e(TAG, "保存log到文件,线程读取数据队列出错", e);
                } catch (IOException e) {
                    BfcLogger.e(TAG, "保存log到文件,写入出错", e);
                }

                if (needExit) {
                    isThreadStarted = false;
                    break;
                }
            }
        }
    }


    /**
     * 创建文件夹<br/>
     * <p>
     * 如果存在,则返回; 如果不存在则创建
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 创建失败
     */
    private static boolean createDirOrExists(File file) {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }


    /**
     * 创建文件
     * <p>
     * 如果文件存在,则返回; 不存在,就创建
     *
     * @param file 需要创建的文件
     * @return {@code true}: 存在或创建成功<br>
     * {@code false}: 创建文件失败
     */
    static boolean createFileOrExists(File file) {
        if (file == null) return false;
        // 如果存在，是文件则返回true，是目录则返回false
        if (file.exists()) return file.isFile();
        if (!createDirOrExists(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            LogInner.w(TAG, e, "创建文件失败");
            return false;
        }
    }

}
