package com.example.mosis.landmarksgo.landmark;

/**
 * Created by ugre9 on 20/05/2017.
 */

public class Landmark {

    public String name;
    public String desc;
    public String type;
    public Double lon;
    public Double lat;

    public Landmark(){

    }

    public Landmark(String name, String desc, String type, Double lon, Double lat)
    {
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.lon = lon;
        this.lat = lat;
    }
}
