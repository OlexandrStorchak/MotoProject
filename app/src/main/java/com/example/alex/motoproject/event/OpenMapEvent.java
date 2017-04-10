package com.example.alex.motoproject.event;

import com.google.android.gms.maps.model.LatLng;

public class OpenMapEvent {
    private LatLng mLatLng;
    private String mUserId;

    public OpenMapEvent(LatLng latLng) {
        this.mLatLng = latLng;
    }

    public OpenMapEvent(String uid) {
        this.mUserId = uid;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public String getUserId() {
        return mUserId;
    }
}
