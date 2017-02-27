package com.example.alex.motoproject.event;


public class ShowUserProfileEvent {
   String UserId;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public ShowUserProfileEvent(String userId) {

        UserId = userId;
    }
}
