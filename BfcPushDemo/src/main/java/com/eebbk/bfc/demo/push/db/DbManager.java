package com.eebbk.bfc.demo.push.db;

import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbManager {
	private final AtomicInteger mOpenCounter = new AtomicInteger();

	private static DbManager instance;
	private static SQLiteOpenHelper mDatabaseHelper;
	private SQLiteDatabase mDatabase;

	/**
	 *初始化DbManager，得到所需DbManager、SQLiteOpenHelper、SystemFacade的对象，一定要在调用前初始化
     */
	public static void initializeInstance(Context context) {
		if (instance == null) {
			synchronized (DbManager.class){
				if(instance == null){
					instance = new DbManager();
					mDatabaseHelper = new DbHelper(context);
				}
			}
		}
	}

	/**
	 *获取DbManager实例，如果没有初始化会IllegalStateException异常
     */
	public static DbManager getInstance() {
		if (instance == null) {
			throw new IllegalStateException(DbManager.class.getSimpleName() +
					" is not initialized, call initializeInstance(..) method first.");
		}
		return instance;
	}

	/**
	 *同步打开数据库
     */
	public synchronized SQLiteDatabase openDatabase() {
		if (mOpenCounter.addAndGet(1) == 1) {
			mDatabase = mDatabaseHelper.getWritableDatabase();
		}
		return mDatabase;
	}
	/**
	 *同步关闭数据库
	 */
	public synchronized void closeDatabase() {
		if (mOpenCounter.addAndGet(-1) == 0) {
			mDatabase.close();
		}
	}
	   
}
