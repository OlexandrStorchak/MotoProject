package com.example.alex.motoproject.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class DistanceUtil {
    public static boolean isClose(LatLng thisUserLocation,
                                  LatLng otherUserLocation,
                                  int closeDistance) {
        if (thisUserLocation == null || otherUserLocation == null) {
            return false;
        }

        float distance = calculateDistanceMeters(
                thisUserLocation.latitude,
                thisUserLocation.longitude,
                otherUserLocation.latitude,
                otherUserLocation.longitude);

        return distance <= closeDistance;
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
