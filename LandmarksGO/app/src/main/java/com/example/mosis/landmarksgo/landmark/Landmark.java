package com.example.mosis.landmarksgo.landmark;

/**
 * Created by ugre9 on 20/05/2017.
 */

public class Landmark {

    public String title;
    public String desc;
    public String type;
    public Double lon;
    public Double lat;
    public String uid;

    public Landmark(){

    }

    public Landmark(String title, String desc, String type, Double lon, Double lat, String uid)
    {
        this.title = title;
        this.desc = desc;
        this.type = type;
        this.lon = lon;
        this.lat = lat;
        this.uid = uid;
    }
}
