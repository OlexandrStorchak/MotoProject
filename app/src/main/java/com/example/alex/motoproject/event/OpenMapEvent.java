package com.example.alex.motoproject.event;

import com.google.android.gms.maps.model.LatLng;

public class OpenMapEvent {
    private LatLng latLng;
    private String uid;

    public OpenMapEvent(LatLng latLng) {
        this.latLng = latLng;
    }

    public OpenMapEvent(String uid) {
        this.uid = uid;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getUid() {
        return uid;
    }
}
