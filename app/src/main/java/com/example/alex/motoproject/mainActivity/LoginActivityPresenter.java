package com.example.alex.motoproject.mainActivity;


import com.google.firebase.auth.FirebaseUser;

public class LoginActivityPresenter implements MainPresenterInterface {
    private static LoginActivityPresenter mLoginActivityPresenter;
    private MainViewInterface mainView;

    private LoginActivityPresenter(MainViewInterface mainView) {
        this.mainView = mainView;
    }

    public static LoginActivityPresenter getInstance(MainViewInterface mainView) {
        if (mLoginActivityPresenter == null) {
            mLoginActivityPresenter = new LoginActivityPresenter(mainView);
        }
        return mLoginActivityPresenter;
    }

    @Override
    public void onLogout() {
        mainView.logout();
    }

    @Override
    public void onLogin(FirebaseUser user) {
        mainView.login(user);
    }
}
