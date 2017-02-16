package com.example.alex.motoproject.screenChat;

public class ChatMessage {
    private String uid, text, name, avatarRef;
    private boolean currentUserMsg;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
