package com.note.note;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/4/9.
 */

public class DBOpen extends SQLiteOpenHelper {
    /******************************创建四个表，分别是USER,NOTE,REMEMBER,SORT************************/


    public static final String USER = "create table USER(" +
            "username varchar(50) primary key," +
            "password varchar(50) ," +

            "answer varchar(20))";
    public static final String NOTE = "create table NOTE(" +
            "id integer primary key autoincrement," +
            "username varchar(50)," +
            "note text, " +
            " sort varchar(50)," +
            " title text ," +
            "flag varchar(20))";
    public static final String REM = "create table REMEMBER(" +
            "username varchar(50) primary key," +
            "state varchar(20) ," +
            "password varchar(50)," + " flag varchar(20) )";
    public static final String SORT = "create table SORT(" +
            "id Integer primary key autoincrement," +
            "username varchar(50) ," +
            "sort varchar(50) not null)";


    public DBOpen(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(USER);
        db.execSQL(NOTE);
        db.execSQL(REM);
        db.execSQL(SORT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists USER");
        db.execSQL("drop table if exists NOTE");
        db.execSQL("drop table if exists REMEMBER ");
        db.execSQL("drop table if exists SORT");
        onCreate(db);

    }
}
