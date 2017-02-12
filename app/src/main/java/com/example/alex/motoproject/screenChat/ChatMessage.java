package com.example.alex.motoproject.screenChat;

public class ChatMessage {
    private final String uid, text;

    public ChatMessage(String uid, String text) {
        this.uid = uid;
        this.text = text;

    }

    public String getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }
}
