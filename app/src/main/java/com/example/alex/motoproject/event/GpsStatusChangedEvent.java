package com.example.alex.motoproject.event;

public class GpsStatusChangedEvent {

    private boolean mGpsOn;

    public GpsStatusChangedEvent(boolean isGpsOn) {
        this.mGpsOn = isGpsOn;
    }

    public boolean isGpsOn() {
        return mGpsOn;
    }
}
