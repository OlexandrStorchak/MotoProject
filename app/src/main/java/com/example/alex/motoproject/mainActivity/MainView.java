package com.example.alex.motoproject.mainActivity;


import com.google.firebase.auth.FirebaseUser;

public interface MainView {

    void login(FirebaseUser user);

    void logout();

}
