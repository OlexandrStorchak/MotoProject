package com.example.alex.motoproject.event;


import com.example.alex.motoproject.firebase.MyProfileFirebase;

public class CurrentUserProfileReadyEvent {
    private MyProfileFirebase myProfileFirebase;

    public CurrentUserProfileReadyEvent(MyProfileFirebase myProfileFirebase) {
        this.myProfileFirebase = myProfileFirebase;
    }

    public MyProfileFirebase getMyProfileFirebase() {
        return myProfileFirebase;
    }

    public void setMyProfileFirebase(MyProfileFirebase myProfileFirebase) {
        this.myProfileFirebase = myProfileFirebase;
    }
}
