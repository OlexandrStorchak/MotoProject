package com.example.alex.motoproject.mainActivity;


import com.google.firebase.auth.FirebaseUser;

public class PresenterImp implements PresenterInterface {
    private MainView mainView;


    public PresenterImp(MainView mainView) {

        this.mainView = mainView;
    }


    @Override
    public void isLogedOut() {
        mainView.logout();
    }

    @Override
    public void isLogedIn(FirebaseUser user) {
        mainView.login(user);
    }


}
