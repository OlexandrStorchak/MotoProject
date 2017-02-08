package com.example.alex.motoproject.models;

public class OnlineUser {
    private String name, avatar, status;

    public OnlineUser(String name, String avatar, String status) {
        this.name = name;
        this.avatar = avatar;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getStatus() {
        return status;
    }
}
