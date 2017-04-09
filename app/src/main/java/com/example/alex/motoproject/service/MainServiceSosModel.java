package com.example.alex.motoproject.service;


import java.util.Map;

public class MainServiceSosModel {
    private String userId;
    private String userName;
    private String description;
    private String lat;
    private String lng;
    private Map<String, String> time;


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getDescription() {
        return description;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }

    public Map<String, String> getTime() {
        return time;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public void setLat(String lat) {
        this.lat = lat;
    }


    public void setLng(String lng) {
        this.lng = lng;
    }


    public void setTime(Map<String, String> time) {
        this.time = time;
    }
}
