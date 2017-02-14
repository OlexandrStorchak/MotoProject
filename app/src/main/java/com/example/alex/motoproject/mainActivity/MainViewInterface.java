package com.example.alex.motoproject.mainActivity;


import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseUser;

interface MainViewInterface {

    void login(FirebaseUser user);

    void logout();

    void replaceFragment(Fragment fragment);



}
