package com.example.alex.motoproject;

public class LocationModel {
    private double lat;
    private double lng;

    public LocationModel(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public LocationModel() {

    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
