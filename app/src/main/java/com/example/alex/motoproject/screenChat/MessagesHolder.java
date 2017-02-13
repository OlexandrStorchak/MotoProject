package com.example.alex.motoproject.screenChat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.utils.CircleTransform;
import com.squareup.picasso.Picasso;

class MessagesHolder extends RecyclerView.ViewHolder {

    private ImageView mUserAvatarView;
    private TextView mNameView;
    private TextView mTextView;

    MessagesHolder(View itemView) {
        super(itemView);
        mUserAvatarView = (ImageView) itemView.findViewById(R.id.chat_message_avatar);
        mNameView = (TextView) itemView.findViewById(R.id.chat_message_name);
        mTextView = (TextView) itemView.findViewById(R.id.chat_message_text);
    }

    public void setName(String name) {
        mNameView.setText(name);
    }

    void setMessageText(String text) {
        mTextView.setText(text);
    }

    // TODO: 13.02.2017 search for a better way of item updates
    void modifyMessageText(String text) {
        mTextView.setText(text);
        mUserAvatarView.setVisibility(View.INVISIBLE);
        mNameView.setVisibility(View.GONE);
    }

    void setAvatar(String avatarRef, Context ctx) {
        Picasso.with(ctx).load(avatarRef).resize(40, 40).centerCrop()
                .transform(new CircleTransform()).into(mUserAvatarView);
    }
}
