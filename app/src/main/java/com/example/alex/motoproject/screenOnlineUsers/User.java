package com.example.alex.motoproject.screenOnlineUsers;

import android.support.annotation.NonNull;

public class User implements Comparable<User> {
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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        OnlineUser that = (OnlineUser) o;
//
//        if (!uid.equals(that.uid)) return false;
//        if (!name.equals(that.name)) return false;
//        if (!avatar.equals(that.avatar)) return false;
//        if (status != null ? !status.equals(that.status) : that.status != null) return false;
//        return relation != null ? relation.equals(that.relation) : that.relation == null;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = uid.hashCode();
//        result = 31 * result + name.hashCode();
//        result = 31 * result + avatar.hashCode();
//        result = 31 * result + (status != null ? status.hashCode() : 0);
//        result = 31 * result + (relation != null ? relation.hashCode() : 0);
//        return result;
//    }

    @Override
    public int compareTo(@NonNull User otherUser) {
        return this.getName().compareTo(otherUser.getName());
    }
}
