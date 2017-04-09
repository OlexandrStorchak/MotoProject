package com.example.alex.motoproject.event;


import com.example.alex.motoproject.firebase.UserProfileFirebase;

public class CurrentUserProfileReadyEvent {
    private UserProfileFirebase userProfileFirebase;

    public CurrentUserProfileReadyEvent(UserProfileFirebase userProfileFirebase) {
        this.userProfileFirebase = userProfileFirebase;
    }

    public UserProfileFirebase getUserProfileFirebase() {
        return userProfileFirebase;
    }

}
