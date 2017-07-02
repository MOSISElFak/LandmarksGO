package com.example.mosis.landmarksgo.other;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by ugre9 on 01/07/2017.
 */

public class LandmarksDBAdapter {

    private SQLiteDatabase db;
    private final Context context;
    private LandmarksDatabaseHelper dbHelper;

    public static final String DATABASE_NAME = "LandmarksDb";

    public static final String LANDMARKS_TABLE = "Landmarks";
    public static final String FRIENDS_TABLE = "Friends";
    public static final int DATABASE_VERSION = 1;

    public static final String AUTO_ID = "ID";
    public static final String LANDMARK_ID = "LandmarkID";
    public static final String LANDMARK_TIME = "Time";
    public static final String FRIEND_ID = "FriendID";

    public LandmarksDBAdapter(Context cont)
    {
        this.context = cont;
        dbHelper = new LandmarksDatabaseHelper(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    public LandmarksDBAdapter open() throws SQLiteException
    {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        db.close();
    }

    public void insertVisitedLandmark(String landmark)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(LANDMARK_ID, landmark);
        //contentValues.put(LANDMARK_TIME, time);

        db.beginTransaction();
        try {
            db.insert(LANDMARKS_TABLE, null, contentValues);
            db.setTransactionSuccessful();
        }catch (SQLiteException e){
            Log.v("LandmarksDBAdapter", e.getMessage());
        }finally {
            db.endTransaction();
        }

    }

    public boolean checkLandmark(String id)
    {
        Cursor cursor = null;

        db.beginTransaction();
        try {
            cursor = db.query(LANDMARKS_TABLE, null,LANDMARK_ID + "='" + id + "'",null,null,null,null);
            db.setTransactionSuccessful();
        }catch (SQLiteException e){
            Log.v("LandmarksDBAdapter" , e.getMessage());
        }finally {
            db.endTransaction();
        }

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void insertFriendship(String friendID)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIEND_ID, friendID);
        //contentValues.put(LANDMARK_TIME, time);

        db.beginTransaction();
        try {
            db.insert(FRIENDS_TABLE, null, contentValues);
            db.setTransactionSuccessful();
        }catch (SQLiteException e){
            Log.v("LandmarksDBAdapter", e.getMessage());
        }finally {
            db.endTransaction();
        }

    }

    public boolean checkFriendship(String id)
    {
        Cursor cursor = null;

        db.beginTransaction();
        try {
            cursor = db.query(FRIENDS_TABLE, null,FRIEND_ID + "='" + id + "'",null,null,null,null);
            db.setTransactionSuccessful();
        }catch (SQLiteException e){
            Log.v("LandmarksDBAdapter" , e.getMessage());
        }finally {
            db.endTransaction();
        }

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                return true;
            }
            cursor.close();
        }
        return false;
    }

    /*public int updateEntry(long id, MyPlace myPlace)
    {
        String where = PLACE_ID + "=" + id;

        ContentValues contentValues = new ContentValues();

        contentValues.put(PLACE_NAME, myPlace.getName());
        contentValues.put(PLACE_DESCRIPTION, myPlace.getDesc());
        contentValues.put(PLACE_LONG, myPlace.getLongitude());
        contentValues.put(PLACE_LAT, myPlace.getLatitude());

        return db.update(DATABASE_TABLE,contentValues,where,null);
    }*/
}
