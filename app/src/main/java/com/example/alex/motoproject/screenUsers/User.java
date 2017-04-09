package com.example.alex.motoproject.screenUsers;

public class User {
    private String uid, name, avatar, status, relation;

    public User(String uid, String name, String avatar, String status, String relation) {
        this.uid = uid;
        this.name = name;
        this.avatar = avatar;
        this.status = status;
        this.relation = relation;
    }

    public User(String uid, String relation) {
        this.uid = uid;
        this.relation = relation;
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

    String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
