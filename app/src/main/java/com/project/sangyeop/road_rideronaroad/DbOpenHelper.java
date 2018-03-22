package com.project.sangyeop.road_rideronaroad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper {

    private static final String DATABASE_NAME = "InnerDatabase(SQLite).db";
    private static final int DATABASE_VERSION = 2;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataBases.CreateDB._CREATE0);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DataBases.CreateDB._TABLENAME0);
            onCreate(db);
        }
    }

    public DbOpenHelper(Context context) {
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void create() {
        mDBHelper.onCreate(mDB);
    }

    public void close() {
        mDB.close();
    }

    // Insert DB
    public long insertColumn(int courseID, double lat, double lng, float speed, double altitude, float bearing, long time, float distance) {
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.COURSE_ID, courseID);
        values.put(DataBases.CreateDB.LATITUDE, lat);
        values.put(DataBases.CreateDB.LONGITUDE, lng);
        values.put(DataBases.CreateDB.SPEED, speed);
        values.put(DataBases.CreateDB.ALTITUDE, altitude);
        values.put(DataBases.CreateDB.BEARING, bearing);
        values.put(DataBases.CreateDB.TOTAL_TIME, time);
        values.put(DataBases.CreateDB.TOTLA_DISTNACE, distance);
        return mDB.insert(DataBases.CreateDB._TABLENAME0, null, values);
    }

    // Update DB
//    public boolean updateColumn(long id, String userid, String name, long age, String gender) {
//        ContentValues values = new ContentValues();
//        values.put(DataBases.CreateDB.USERID, userid);
//        values.put(DataBases.CreateDB.NAME, name);
//        values.put(DataBases.CreateDB.AGE, age);
//        values.put(DataBases.CreateDB.GENDER, gender);
//        return mDB.update(DataBases.CreateDB._TABLENAME0, values, "_id=" + id, null) > 0;
//    }

    // Delete All
    public void deleteAllColumns() {
        mDB.delete(DataBases.CreateDB._TABLENAME0, null, null);
    }

    // Delete DB
    public boolean deleteColumn(long id) {
        return mDB.delete(DataBases.CreateDB._TABLENAME0, "_id=" + id, null) > 0;
    }

    // Select DB
    public Cursor selectColumns() {
        return mDB.query(DataBases.CreateDB._TABLENAME0, null, null, null, null, null, null);
    }

    // sort by column
    public Cursor sortColumn(String sort) {
        Cursor c = mDB.rawQuery("SELECT * FROM usertable ORDER BY " + sort + ";", null);
        return c;
    }

    public Cursor selectpreview() {
        Cursor c = mDB.rawQuery("SELECT * FROM courseInfo where totalTime > 0 ;", null);
        return c;
    }

    public Cursor selectCourseInfo(int courseID) {
        Cursor c = mDB.rawQuery("SELECT * FROM courseInfo where courseID =" + courseID + ";", null);
        return c;
    }
}
