package com.example.alex.motoproject.models;

public class User {

    private String name, avatar;
    @SuppressWarnings("unused")
    public User() {
        //required empty public constructor
    }
    @SuppressWarnings("unused")
    public User(String name, String avatar) {
        this.name = name;
        this.avatar = avatar;
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
    @SuppressWarnings("unused")
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}