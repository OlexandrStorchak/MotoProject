package com.example.alex.motoproject.events;


import com.example.alex.motoproject.screenOnlineUsers.OnlineUsersModel;

public class ShowUserProfile {
    OnlineUsersModel model;

    public OnlineUsersModel getModel() {
        return model;
    }

    public void setModel(OnlineUsersModel model) {
        this.model = model;
    }

    public ShowUserProfile(OnlineUsersModel model) {

        this.model = model;
    }
}
