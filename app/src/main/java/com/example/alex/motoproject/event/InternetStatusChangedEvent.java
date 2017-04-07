package com.example.alex.motoproject.event;

public class InternetStatusChangedEvent {
    private boolean mInternetOn;

    public InternetStatusChangedEvent(boolean internetOn) {
        mInternetOn = internetOn;
    }

    public boolean isInternetOn() {
        return mInternetOn;
    }
}