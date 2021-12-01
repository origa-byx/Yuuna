package com.origami.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;

/**
 * @by: origami
 * @date: {2021-06-07}
 * @info:
 *
 **/
public class Db extends SQLiteOpenHelper {

    public static final String TAG = "Db";

    public static String DB_MANE = "ori.db";

    public static final String FACE_IOC = "face_ioc";

    private final static String CREATE_FACE_IOC = "create table if not exists face_ioc("
            + "id integer primary key autoincrement,"
            + "time varchar(255) default \"\","
            + "userName varchar(255) default \"\","
            + "personId varchar(255) default \"\","
            + "personName varchar(255) default \"\","
            + "sex integer default 0,"
            + "personImage varchar(255) default \"\","
            + "cardNo varchar(255) default \"\","
            + "startTime varchar(255) default \"\","
            + "limitType integer default 0,"
            + "endTime varchar(255) default \"\","
            + "content text default \"\","
            + "status integer default 0)";


    private static Db DBHelper;

    public static void init(Db context) {
        if(DBHelper != null){ return; }
        DBHelper = context;
    }

    public Db(@Nullable Context context) {
        super(context, DB_MANE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FACE_IOC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        switch (oldVersion) { }
    }

    //-----------------------------------------static-----------------------------------

    public static void doWriteSql(SQLiteOpenHelper db, String sql,Object[] ...s){
        if(s.length != 0){
            db.getWritableDatabase().execSQL(sql,s[0]);
        }else {
            db.getWritableDatabase().execSQL(sql);
        }
    }

    public static Cursor doReadSql(SQLiteOpenHelper db, String sql,String[] prams){
        return db.getReadableDatabase().rawQuery(sql,prams);
    }

    public static void closeDb(SQLiteOpenHelper db) {
        db.getWritableDatabase().close();
    }

    /**
     * 方法1：检查某表列是否存在
     *
     * @param db         database
     * @param tableName  表名
     * @param columnName 列名
     * @return true已存在，false不存在
     */
    public static boolean checkColumnExist(SQLiteDatabase db, String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            //查询一行
            cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0"
                    , null);
            result = cursor != null && cursor.getColumnIndex(columnName) != -1;
        } catch (Exception e) {
            Log.e(TAG, "checkColumnExists..." + e.getMessage());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }


}
