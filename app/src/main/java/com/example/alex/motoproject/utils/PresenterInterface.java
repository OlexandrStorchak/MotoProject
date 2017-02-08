package com.example.alex.motoproject.utils;


import com.google.firebase.auth.FirebaseUser;

public interface PresenterInterface {

    void isLogedIn(FirebaseUser user);
    void isLogedOut();


}
