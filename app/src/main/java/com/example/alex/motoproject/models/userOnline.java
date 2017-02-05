package com.example.alex.motoproject.models;

public class userOnline {

    private String name, status, email, avatar;
    private double lat, lon;
    @SuppressWarnings("unused")
    public userOnline() {
        //required empty public constructor
    }
    @SuppressWarnings("unused")
    public userOnline(String name, String status, String email, double lat, double lon, String avatar) {
        this.name = name;
        this.status = status;
        this.email = email;
        this.lat = lat;
        this.lon = lon;
        this.avatar = avatar;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }
    @SuppressWarnings("unused")
    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }
    @SuppressWarnings("unused")
    public void setEmail(String email) {
        this.email = email;
    }
    @SuppressWarnings("unused")
    public double getLat() {
        return lat;
    }
    @SuppressWarnings("unused")
    public void setLat(double lat) {
        this.lat = lat;
    }
    @SuppressWarnings("unused")
    public double getLon() {
        return lon;
    }
    @SuppressWarnings("unused")
    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getAvatar() {
        return avatar;
    }
    @SuppressWarnings("unused")
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}