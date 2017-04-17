package com.example.alex.motoproject.screenChat;

import com.example.alex.motoproject.app.App;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

public class ChatModel implements ChatMvp.PresenterToModel,
        FirebaseDatabaseHelper.ChatUpdateReceiver {
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private ChatMvp.ModelToPresenter mPresenter;
    private LinkedList<ChatMessage> mMessages = new LinkedList<>();

    ChatModel(ChatMvp.ModelToPresenter presenter) {
        App.getCoreComponent().inject(this);
        mPresenter = presenter;
    }

    @Override
    public void registerChatMessagesListener() {
        mFirebaseDatabaseHelper.registerChatMessagesListener(this);
    }

    @Override
    public void unregisterChatMessagesListener() {
        mFirebaseDatabaseHelper.unregisterChatMessagesListener();
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
    public void fetchOlderChatMessages() {
        mFirebaseDatabaseHelper.fetchOlderChatMessages(this);
        mMessages.size();
    }

    @Override
    public void sendChatMessage(String msg) {
        mFirebaseDatabaseHelper.sendChatMessage(msg);
    }

    @Override
    public void onNewChatMessage(ChatMessage message) {
        mMessages.add(message);
        mPresenter.showNewMessage();
    }

    @Override
    public void onOlderChatMessages(List<ChatMessage> olderMessages, int lastPos) {
        for (ChatMessage olderMessage : olderMessages) {
            mMessages.addFirst(olderMessage);
        }
        mPresenter.disableRefreshingSwipeLayout();
        mPresenter.showOlderMessages(0, lastPos);
    }

    @Override
    public void onChatMessageNewData(ChatMessage message) {
        mPresenter.updateMessage(mMessages.indexOf(message));
    }

    @Override
    public void onLastMessage() {
        mPresenter.enableSwipeLayout(false);
    }

    @Override
    public void onNoCurrentUserLocation() {
        mPresenter.onNoCurrentUserLocation();
    }

    @Override
    public boolean hasThisMessage(String messageId) {
        for (ChatMessage message : mMessages) {
            if (message.getMessageId().equals(messageId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void filterChatToDistance(int meters) {
        mFirebaseDatabaseHelper.fetchUsersLocations();
        mFirebaseDatabaseHelper.setCloseDistance(meters);
    }
}
