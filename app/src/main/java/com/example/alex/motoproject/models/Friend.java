package com.example.alex.motoproject.models;

/**
 * Created by Isao on 08.02.2017.
 */

public class Friend {
    private String name, email, avatar;

    public Friend(String name, String email, String avatar) {
        this.name = name;
        this.email = email;
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }
}
