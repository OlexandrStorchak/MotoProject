package com.example.alex.motoproject.mainActivity;


import android.support.v4.app.Fragment;

import com.google.firebase.auth.FirebaseUser;

public class MainActivityPresenter implements MainPresenterInterface {
    private MainViewInterface mainView;


    MainActivityPresenter(MainViewInterface mainView) {

        this.mainView = mainView;
    }

    @Override
    public void onLogout() {
        mainView.logout();

    }

    @Override
    public void onLogin(FirebaseUser user) {
        mainView.login(user);
    }

    public void replaceFragment(Fragment fragment){
        mainView.replaceFragment(fragment);
    }
}
