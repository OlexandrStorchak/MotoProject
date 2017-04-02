package com.example.alex.motoproject.mainActivity;


import com.google.firebase.auth.FirebaseUser;

public class MainActivityPresenter implements MainPresenterInterface {
    private static MainActivityPresenter mMainActivityPresenter;
    private MainViewInterface mainView;

    private boolean mLoggedIn;
    private boolean mFirstLaunch = true;

    MainActivityPresenter(MainViewInterface mainView) {
        this.mainView = mainView;
    }

    public static MainActivityPresenter getInstance(MainViewInterface mainView) {
        if (mMainActivityPresenter == null) {
            mMainActivityPresenter = new MainActivityPresenter(mainView);
        }
        return mMainActivityPresenter;
    }

    @Override
    public void onLogout() {
        if ((mLoggedIn) || (mFirstLaunch)) {
            mFirstLaunch = false;
            mLoggedIn = false;
            mainView.logout();
        }
    }

    @Override
    public void onLogin(FirebaseUser user) {
        if (mLoggedIn) return;
        mLoggedIn = true;
        mainView.login(user);
    }

}
