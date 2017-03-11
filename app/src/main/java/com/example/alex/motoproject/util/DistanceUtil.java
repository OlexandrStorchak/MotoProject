package com.example.alex.motoproject.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class DistanceUtil {
    private static final int CLOSE_DISTANCE = 10000; //10 km

    public static boolean isClose(LatLng thisUserLocation, LatLng otherUserLocation) {
        float distance = calculateDistanceMeters(
                thisUserLocation.latitude,
                thisUserLocation.longitude,
                otherUserLocation.latitude,
                otherUserLocation.longitude);

        return distance <= CLOSE_DISTANCE;
    }

    private static float calculateDistanceMeters(double firstLat, double firstLng,
                                                 double secondLat, double secondLng) {
        float[] results = new float[1];
        Location.distanceBetween(
                firstLat,
                firstLng,
                secondLat,
                secondLng,
                results
        );
        return results[0];
    }
}
