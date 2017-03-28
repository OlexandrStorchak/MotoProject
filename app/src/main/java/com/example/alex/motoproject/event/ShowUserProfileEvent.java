package com.example.alex.motoproject.event;


public class ShowUserProfileEvent {
   private String mUserId;

    public String getUserId() {
        return mUserId;
    }

    public ShowUserProfileEvent(String userId) {
        mUserId = userId;
    }
}
