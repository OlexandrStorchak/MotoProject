package com.example.alex.motoproject.screenOnlineUsers;

public class OnlineUser extends BaseUser {
    private String status;

    private String relation;

    public OnlineUser(String uid, String name, String avatar, String status, String relation) {
        super(uid, name, avatar);
        this.status = status;
        this.relation = relation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
