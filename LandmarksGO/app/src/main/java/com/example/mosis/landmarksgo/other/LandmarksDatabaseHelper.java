package com.example.mosis.landmarksgo.other;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ugre9 on 01/07/2017.
 */

public class LandmarksDatabaseHelper extends SQLiteOpenHelper {

    // SQL naredba za kreiranje Landmars tabele
    private static final String DATABASE_CREATE_LANDMARKS = "create table " + LandmarksDBAdapter.LANDMARKS_TABLE + " ("
            + LandmarksDBAdapter.AUTO_ID + " integer primary key autoincrement, "
            + LandmarksDBAdapter.LANDMARK_ID + " text unique not null);";

    // SQL naredba za kreiranje Friends tabele
    private static final String DATABASE_CREATE_FRIENDS = "create table " + LandmarksDBAdapter.FRIENDS_TABLE + " ("
            + LandmarksDBAdapter.AUTO_ID + " integer primary key autoincrement, "
            + LandmarksDBAdapter.FRIEND_ID + " text unique not null);";

    public LandmarksDatabaseHelper(Context context, String name,
                                  SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(DATABASE_CREATE_LANDMARKS);
            db.execSQL(DATABASE_CREATE_FRIENDS);
        }catch (SQLiteException e){
            Log.v("LandmarksDatabaseHelper", e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LandmarksDBAdapter.LANDMARKS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LandmarksDBAdapter.FRIENDS_TABLE);
        onCreate(db);
    }
}
