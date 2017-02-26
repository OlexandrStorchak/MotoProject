package com.example.alex.motoproject.event;


import com.example.alex.motoproject.firebase.UsersProfileFirebase;

public class OnlineUserProfileReadyEvent {
    private UsersProfileFirebase usersProfileFirebase;

    public OnlineUserProfileReadyEvent(UsersProfileFirebase usersProfileFirebase) {
        this.usersProfileFirebase = usersProfileFirebase;
    }

    public UsersProfileFirebase getUsersProfileFirebase() {
        return usersProfileFirebase;
    }

    public void setUsersProfileFirebase(UsersProfileFirebase usersProfileFirebase) {
        this.usersProfileFirebase = usersProfileFirebase;
    }
}
