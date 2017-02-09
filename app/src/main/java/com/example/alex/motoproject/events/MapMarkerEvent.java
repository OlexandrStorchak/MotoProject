package com.example.alex.motoproject.events;

import com.google.android.gms.maps.model.LatLng;

public class MapMarkerEvent {
    public final LatLng latLng;
    public final String uid;
    public final String userName;
    public final String avatarRef;

    public MapMarkerEvent(LatLng latLng, String uid, String userName, String avatarRef) {
        this.latLng = latLng;
        this.uid = uid;
        this.userName = userName;
        this.avatarRef = avatarRef;
    }
}
