package com.example.alex.motoproject.screenChat;

import java.util.Map;

public class ChatMessageSendable {
    private final String uid, text;
    private final Map<String, String> sendTime;

    public ChatMessageSendable(String uid, String text, Map<String, String> sendTime) {
        this.uid = uid;
        this.text = text;
        this.sendTime = sendTime;
    }

    public String getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }

    public Map<String, String> getSendTime() {
        return sendTime;
    }
}
