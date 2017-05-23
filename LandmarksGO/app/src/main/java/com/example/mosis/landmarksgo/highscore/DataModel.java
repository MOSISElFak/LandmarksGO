package com.example.mosis.landmarksgo.highscore;

import android.graphics.drawable.Drawable;

/**
 * Created by k on 5/22/2017.
 */

public class DataModel {

    String name;
    int points;
    Drawable photo;
    int number;

    public DataModel(String name, int points, Drawable photo, int number) {
        this.name=name;
        this.points= points;
        this.photo= photo;
        this.number= number;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public Drawable getPhoto() {
        return photo;
    }

    public int getNumber() { return  number;}
}
