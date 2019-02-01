package com.eebbk.bfc.demo.push.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper{

    public final static String DB_NAME="push.db";
    public final static String TABLE_NAME="message_data";
    public final static String COLUMN_MESSAGE_NAME ="message";
    public final static String COLUMN_TIME_NAME ="time";
    private final static int VERSION=1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+TABLE_NAME+" (_id integer primary key autoincrement,"
                        + COLUMN_MESSAGE_NAME +" text,"
                        + COLUMN_TIME_NAME +" text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("create table "+TABLE_NAME+" ( integer primary key autoincrement,"
                + COLUMN_MESSAGE_NAME +" text,"
                + COLUMN_TIME_NAME +" text);");
    }


}
