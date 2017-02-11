package com.example.alex.motoproject.screenOnlineUsers;

public class OnlineUserModel {
    private String uid, name, avatar, status;

    public OnlineUserModel(String uid, String name, String avatar, String status) {
        this.uid = uid;
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

    public String getUid() {
        return uid;
    }
}
