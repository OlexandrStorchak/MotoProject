package com.example.alex.motoproject.event;

import com.google.android.gms.maps.model.LatLng;

public class MapMarkerModel {
    public final LatLng latLng;
    public final String uid;
    public final String userName;
    public final String avatarRef;
    public final String relation;

    public MapMarkerModel(LatLng latLng, String uid, String userName,
                          String avatarRef, String relation) {
        this.latLng = latLng;
        this.uid = uid;
        this.userName = userName;
        this.avatarRef = avatarRef;
        this.relation = relation;
    }
}
