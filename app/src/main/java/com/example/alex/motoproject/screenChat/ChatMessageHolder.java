package com.example.alex.motoproject.screenChat;

import android.view.View;
import android.widget.TextView;

import com.example.alex.motoproject.R;

class ChatMessageHolder extends BaseChatItemHolder {

    private TextView mTextView;

    ChatMessageHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.chat_message_text);
    }

    void setMessageText(String text) {
        mTextView.setText(text);
    }
}
