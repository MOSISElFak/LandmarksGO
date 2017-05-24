package com.example.mosis.landmarksgo.friends;

import java.util.List;

/**
 * Created by k on 5/22/2017.
 */

public class Friendship {

/*
    public String uid1;
    public String uid2;

    public Friendship(){
    }

    public Friendship(String uid1, String uid2)
    {
        this.uid1 = uid1;
        this.uid2 = uid2;
    }
*/

    private String uid;
    private List<String> friends;

    public Friendship(String uid, List<String> friends) {
        this.uid = uid;
        this.friends = friends;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public Friendship(String uid1, String uid2){
        //do nothing, this is for Friends.java
    }
}
