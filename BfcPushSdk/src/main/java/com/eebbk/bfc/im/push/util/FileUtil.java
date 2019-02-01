package com.eebbk.bfc.im.push.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * @author liuyewu
 * @company EEBBK
 * @function FileUtil
 * @date 2016/12/12
 */
public class FileUtil {


    /**
     * 写入数据到SD卡中
     */
    public static boolean writeData2SDCard(String path, String fileName, String data) {
        File file = new File(path);
        if(!file.exists()){
            if(file.mkdirs()){
                return false;
            }
        }

        boolean flag=false;
        BufferedWriter wt = null;
        try {
            wt=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+File.separator+fileName)));
            wt.write(data);
            flag=true;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(wt!=null){
                try {
                    wt.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

    /**
     * 从SD卡中读文件
     */
    public static String readData2SDCard(String path, String fileName) {
        File file = new File(path+File.separator+fileName);
        if(!file.exists()){
            return null;
        }

        BufferedReader rd = null;
        String result=null;
        try {
            rd=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            result=rd.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(rd!=null){
                try {
                    rd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
