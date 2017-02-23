package com.example.alex.motoproject.screenChat;

import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

public class ChatMessageSendable {
    private final String uid, text;
    private final Map<String, String> sendTime;
    private final LatLng location;

    public ChatMessageSendable(String uid, String text, Map<String, String> sendTime) {
        this.uid = uid;
        this.text = text;
        this.location = null;
        this.sendTime = sendTime;
    }

    public ChatMessageSendable(String uid, LatLng location, Map<String, String> sendTime) {
        this.uid = uid;
        this.text = null;
        this.location = location;
        this.sendTime = sendTime;
    }

    public String getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }

    public LatLng getLocation() {
        return location;
    }

    public Map<String, String> getSendTime() {
        return sendTime;
    }
}
