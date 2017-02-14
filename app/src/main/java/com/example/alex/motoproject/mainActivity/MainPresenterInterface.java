package com.example.alex.motoproject.mainActivity;


import com.google.firebase.auth.FirebaseUser;

public interface MainPresenterInterface {

    void onLogin(FirebaseUser user);

    void onLogout();

}
