package com.example.alex.motoproject.screenOnlineUsers;

public abstract class BaseUser {
    private String uid, name, avatar;

    public BaseUser(String uid, String name, String avatar) {
        this.uid = uid;
        this.name = name;
        this.avatar = avatar;
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
}
