package com.example.alex.motoproject.firebase;


public class UsersProfileFirebase {
    String id;
    String name;
    String avatar;
    String email;
    String motorcycle;

    public String getMotorcycle() {
        return motorcycle;
    }

    public void setMotorcycle(String motorcycle) {
        this.motorcycle = motorcycle;
    }

    String nickName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
