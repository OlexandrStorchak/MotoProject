package com.example.alex.motoproject.screenChat;

import android.view.View;

import java.util.List;

interface ChatMVP {

    interface ViewToPresenter {
        void onTextChanged(CharSequence charSequence);

        void onClickSendButton(String msg);

        void onTouchRecyclerView(View view);

        void registerChatMessagesListener();

        void unregisterChatMessagesListener();

        void registerAdapter();
    }

    interface PresenterToView {
        void hideSendButton();

        void showSendButton();

        void scrollToPosition(int position);

        void cleanupEditText();

        void hideKeyboard(View view);

        void updateMessage(int position);

        void notifyItemInserted(int position);

        void setListToAdapter(List<ChatMessage> messages);

        int getLastCompletelyVisibleItemPosition();
    }

    interface PresenterToModel {
        void sendChatMessage(String msg);

        void registerChatMessagesListener();

        void unregisterChatMessagesListener();

        int getMessagesSize();

        List<ChatMessage> getMessages();
    }

    interface ModelToPresenter {
        void showNewMessage();

        void updateMessage();
    }
}
