package com.example.alex.motoproject.events;


public class ShowUserProfile {
   String UserId;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public ShowUserProfile(String userId) {

        UserId = userId;
    }
}
