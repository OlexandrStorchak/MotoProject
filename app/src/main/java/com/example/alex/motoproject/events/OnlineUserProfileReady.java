package com.example.alex.motoproject.events;


import com.example.alex.motoproject.firebase.UsersProfileFirebase;

public class OnlineUserProfileReady {
    UsersProfileFirebase usersProfileFirebase;

    public OnlineUserProfileReady(UsersProfileFirebase usersProfileFirebase) {
        this.usersProfileFirebase = usersProfileFirebase;
    }

    public UsersProfileFirebase getUsersProfileFirebase() {
        return usersProfileFirebase;
    }

    public void setUsersProfileFirebase(UsersProfileFirebase usersProfileFirebase) {
        this.usersProfileFirebase = usersProfileFirebase;
    }
}
