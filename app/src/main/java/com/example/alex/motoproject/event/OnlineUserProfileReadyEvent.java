package com.example.alex.motoproject.event;


import com.example.alex.motoproject.firebase.UserProfileFirebase;

public class OnlineUserProfileReadyEvent {
    private UserProfileFirebase mUserProfileFirebase;

    public OnlineUserProfileReadyEvent(UserProfileFirebase userProfileFirebase) {
        this.mUserProfileFirebase = userProfileFirebase;
    }

    public UserProfileFirebase getUserProfileFirebase() {
        return mUserProfileFirebase;
    }
}
