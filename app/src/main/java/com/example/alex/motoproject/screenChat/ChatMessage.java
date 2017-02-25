package com.example.alex.motoproject.screenChat;

import com.google.android.gms.maps.model.LatLng;

public class ChatMessage {
    private String uid, text, name, avatarRef, sendTime;
    private boolean currentUserMsg;
    private LatLng location;

    public ChatMessage(String uid, String sendTime) {
        this.uid = uid;
        this.sendTime = sendTime;

    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String getSendTime() {
        return sendTime;
    }

    String getAvatarRef() {
        return avatarRef;
    }

    public void setAvatarRef(String avatarRef) {
        this.avatarRef = avatarRef;
    }

    boolean isCurrentUserMsg() {
        return currentUserMsg;
    }

    public void setCurrentUserMsg(boolean currentUserMsg) {
        this.currentUserMsg = currentUserMsg;
    }
}
