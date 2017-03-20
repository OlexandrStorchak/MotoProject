package com.example.alex.motoproject.event;

import com.google.android.gms.maps.model.LatLng;

public class MapMarkerEvent {
    public final LatLng latLng;
    public final String uid;
    public final String userName;
    public final String avatarRef;
    public final String relation;

    public MapMarkerEvent(LatLng latLng, String uid, String userName,
                          String avatarRef, String relation) {
        this.latLng = latLng;
        this.uid = uid;
        this.userName = userName;
        this.avatarRef = avatarRef;
        this.relation = relation;
    }

    public MapMarkerEvent(String uid) {
        this.latLng = null;
        this.uid = uid;
        this.userName = null;
        this.avatarRef = null;
        this.relation = null;
    }
}
