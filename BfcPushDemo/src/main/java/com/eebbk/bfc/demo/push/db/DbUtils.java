package com.eebbk.bfc.demo.push.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eebbk.bfc.demo.push.basicfunction.MessageInfo;

import java.util.ArrayList;
import java.util.List;

public class DbUtils {

	private DbUtils(){}

	public static void saveMessage(MessageInfo info){
		insert(info);
	}

	public static List<MessageInfo> getAllMessage(){
		List<MessageInfo> datas=new ArrayList<>();
		Cursor cursor=query();

		while (cursor.moveToNext()){
			MessageInfo info=new MessageInfo();
			info.setMsg(cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_MESSAGE_NAME)));
			info.setReceiveTime(cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TIME_NAME)));
			datas.add(info);
		}
		cursor.close();
		return datas;
	}

	public static void deletAllMessage(){
		delete();
	}
	/**
	 * 插入数据
	 */
	private static long insert(MessageInfo info) {

		SQLiteDatabase db = DbManager.getInstance().openDatabase();

		ContentValues filteredValues = new ContentValues();

		filteredValues.put(DbHelper.COLUMN_MESSAGE_NAME, info.getMsg());
		filteredValues.put(DbHelper.COLUMN_TIME_NAME, info.getReceiveTime());

		long rowID = db.insert(DbHelper.TABLE_NAME, null, filteredValues);
		if (rowID == -1) {
			return -1;
		}
		
		DbManager.getInstance().closeDatabase();

		return rowID;
	}

	/**
	 * 查询数据
	 */
	private static Cursor query() {

		SQLiteDatabase db = DbManager.getInstance().openDatabase();

		Cursor ret = db.query(DbHelper.TABLE_NAME,null,null,null,null,null,null);

		return ret;
	}


	/**
	 * 删除数据
	 */
	private static int delete() {

		SQLiteDatabase db = DbManager.getInstance().openDatabase();
		int count ;
		
		count = db.delete(DbHelper.TABLE_NAME, null, null);

		DbManager.getInstance().closeDatabase();
		return count;
	}

}
