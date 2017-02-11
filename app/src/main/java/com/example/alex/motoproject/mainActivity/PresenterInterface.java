package com.example.alex.motoproject.mainActivity;


import com.google.firebase.auth.FirebaseUser;

interface PresenterInterface {

    void onLogin(FirebaseUser user);

    void onLogout();


}
