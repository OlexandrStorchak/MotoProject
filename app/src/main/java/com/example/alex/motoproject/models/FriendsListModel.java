package com.example.alex.motoproject.models;

public class FriendsListModel {
    String name,status, email;
    String avatar;

    public FriendsListModel() {

    }

    public FriendsListModel(String name, String status, String email, String avatar) {
        this.name = name;
        this.status = status;
        this.email = email;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}