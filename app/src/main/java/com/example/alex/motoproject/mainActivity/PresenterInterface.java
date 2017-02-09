package com.example.alex.motoproject.mainActivity;


import com.google.firebase.auth.FirebaseUser;

public interface PresenterInterface {

    void isLogedIn(FirebaseUser user);
    void isLogedOut();


}
