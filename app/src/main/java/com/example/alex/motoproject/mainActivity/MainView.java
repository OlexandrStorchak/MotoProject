package com.example.alex.motoproject.mainActivity;


import com.google.firebase.auth.FirebaseUser;

interface MainView {

    void login(FirebaseUser user);

    void logout();

}
