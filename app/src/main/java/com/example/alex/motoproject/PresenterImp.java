package com.example.alex.motoproject;


import com.example.alex.motoproject.utils.PresenterInterface;

public class PresenterImp implements PresenterInterface {
    private MainView mainView;

    public PresenterImp(MainView mainView) {
        this.mainView = mainView;
    }


    @Override
    public void isLogedIn(Boolean logedIn) {
        if (logedIn){
            mainView.login();
        } else
        {
            mainView.logout();
        }
    }
}
