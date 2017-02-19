package com.example.alex.motoproject.screenChat;

import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatModel implements ChatMVP.PresenterToModel,
        FirebaseDatabaseHelper.ChatUpdateReceiver {
    private ChatMVP.ModelToPresenter mPresenter;

    private List<ChatMessage> mMessages = new ArrayList<>();

    private FirebaseDatabaseHelper mFirebaseHelper = new FirebaseDatabaseHelper();

    ChatModel(ChatMVP.ModelToPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void registerChatMessagesListener() {
        mFirebaseHelper.registerChatMessagesListener(this);
    }

    @Override
    public void unregisterChatMessagesListener() {
        mFirebaseHelper.unregisterChatMessagesListener();
    }

    @Override
    public int getMessagesSize() {
        return mMessages.size();
    }

    @Override
    public List<ChatMessage> getMessages() {
        return mMessages;
    }

    @Override
    public void sendChatMessage(String msg) {
        mFirebaseHelper.sendChatMessage(msg);
    }

    @Override
    public void onNewChatMessage(ChatMessage message) {
        mMessages.add(message);
        mPresenter.showNewMessage();
    }

    @Override
    public void onChatMessageNewData(ChatMessage message) {
        mPresenter.updateMessage();
    }
}
