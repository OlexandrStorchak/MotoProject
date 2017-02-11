package com.example.alex.motoproject.mainActivity;


import com.google.firebase.auth.FirebaseUser;

interface MainViewInterface {

    void login(FirebaseUser user);

    void logout();

}
