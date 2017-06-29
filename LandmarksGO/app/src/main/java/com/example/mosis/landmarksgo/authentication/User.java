package com.example.mosis.landmarksgo.authentication;

/**
 * Created by ugre9 on 20/05/2017.
 */

public class User {

    public String firstName;
    public String lastName;
    public Double lat;
    public Double lon;
    public Integer gpsrefresh;
    public Boolean showfriends;
    public Boolean showplayers;
    public Boolean workback;
    public String uid;

    public User()
    {

    }

    public User(String firstName, String lastName, String uid)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.lat = 0.0;
        this.lon = 0.0;
        this.gpsrefresh = 10;
        this.showfriends = true;
        this.showplayers = true;
        this.workback = true;
        this.uid = uid;
    }

    public void setLocation(Double lat, Double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }
}
