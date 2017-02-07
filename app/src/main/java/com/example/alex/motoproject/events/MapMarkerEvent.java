package com.example.alex.motoproject.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Isao on 07.02.2017.
 */

public class MapMarkerEvent {
    public final LatLng latLng;
    public final String uid;
    public final String userName;

    public MapMarkerEvent(LatLng latLng, String uid, String userName) {
        this.latLng = latLng;
        this.uid = uid;
        this.userName = userName;
    }
}
