package com.eebbk.bfc.demo.push.util;

import android.os.Environment;

public class FileUtils {

    private static final String EXTERNAL_PATH= Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String Message_store_Path=EXTERNAL_PATH+"/.im/message.txt";

    private FileUtils(){}

    public static void saveRecieverMessage(){

    }

    public static void readRecieverMessage(){

    }
}
