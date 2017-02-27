package com.example.alex.motoproject.screenOnlineUsers;

public class OnlineUser {
    private String uid, name, avatar, status;

    public OnlineUser(String uid, String name, String avatar, String status) {
        this.uid = uid;
        this.name = name;
        this.avatar = avatar;
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
