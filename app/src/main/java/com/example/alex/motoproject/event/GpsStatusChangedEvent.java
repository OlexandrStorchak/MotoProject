package com.example.alex.motoproject.event;

public class GpsStatusChangedEvent {

    private boolean isGpsOn;

    public GpsStatusChangedEvent(boolean isGpsOn) {
        this.isGpsOn = isGpsOn;
    }

    public boolean isGpsOn() {
        return isGpsOn;
    }
}
