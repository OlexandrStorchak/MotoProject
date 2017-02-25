package com.example.alex.motoproject.events;

import com.google.android.gms.maps.model.LatLng;

public class OpenMapWithLatLngEvent {
    private LatLng latLng;

    public OpenMapWithLatLngEvent(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
