package com.example.mosis.landmarksgo.highscore;

import android.graphics.Bitmap;

/**
 * Created by k on 5/22/2017.
 */

public class DataModel {

    String name;
    int points;
    Bitmap photo;
    int number;
    int friendNumberOnServer;

    public DataModel(String name, int points, Bitmap photo, int number, int friendNumberOnServer) {
        this.name=name;
        this.points= points;
        this.photo= photo;
        this.number= number;
        this.friendNumberOnServer = friendNumberOnServer;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public int getNumber() { return  number;}

    public int getFriendNumberOnServer() {
        return friendNumberOnServer;
    }
}
