package com.example.alex.motoproject.screenChat;

public class ChatMessage {
    private String uid, text, name, avatarRef, sendTime;
    private boolean currentUserMsg;

    public ChatMessage(String uid, String text, String sendTime) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSendTime() {
        return sendTime;
    }

    public String getAvatarRef() {
        return avatarRef;
    }

    public void setAvatarRef(String avatarRef) {
        this.avatarRef = avatarRef;
    }

    public boolean isCurrentUserMsg() {
        return currentUserMsg;
    }

    public void setCurrentUserMsg(boolean currentUserMsg) {
        this.currentUserMsg = currentUserMsg;
    }
}
