package com.example.alex.motoproject.screenChat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.alex.motoproject.R;

class ChatMsgOwnHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;
    private TextView mSendTime;

    ChatMsgOwnHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.chat_messageown_text);
        mSendTime = (TextView) itemView.findViewById(R.id.chat_messageown_date);
    }

    void setMessageText(String text) {
        mTextView.setText(text);
    }

    void setSendTime(String dateTime) {
        mSendTime.setText(dateTime);
    }
}
