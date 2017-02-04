package com.example.alex.motoproject.models;

public class usersOnline {
    String name,status, email,avatar;
    double lat,lon;
    public usersOnline() {

    }

    public usersOnline(String name, String status, String email, double lat, double lon, String avatar) {
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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


}