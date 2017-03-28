package com.example.alex.motoproject.event;


import com.example.alex.motoproject.firebase.UsersProfileFirebase;

public class OnlineUserProfileReadyEvent {
    private UsersProfileFirebase mUserProfileFirebase;

    public OnlineUserProfileReadyEvent(UsersProfileFirebase usersProfileFirebase) {
        this.mUserProfileFirebase = usersProfileFirebase;
    }

    public UsersProfileFirebase getUserProfileFirebase() {
        return mUserProfileFirebase;
    }
}
