package com.example.alex.motoproject.screenChat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.ShowUserProfileEvent;
import com.example.alex.motoproject.util.CircleTransform;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

abstract class BaseChatItemHolder extends RecyclerView.ViewHolder {

    private ImageView mUserAvatarView;
    private TextView mNameView;
    private TextView mSendTime;

    BaseChatItemHolder(View itemView) {
        super(itemView);
        mUserAvatarView = (ImageView) itemView.findViewById(R.id.chat_message_avatar);
        mNameView = (TextView) itemView.findViewById(R.id.chat_message_name);
        mSendTime = (TextView) itemView.findViewById(R.id.chat_message_time);
    }

    void setName(String name) {
        mNameView.setText(name);
    }

    void setSendTime(String dateTime) {
        mSendTime.setText(dateTime);
    }

    void setAvatar(String avatarRef, Context ctx) {
        Picasso.with(ctx).load(avatarRef).fit().centerCrop()
                .transform(new CircleTransform()).into(mUserAvatarView);
    }

    void setUserAvatarViewOnClickListener(final String uid) {
        mUserAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new ShowUserProfileEvent(uid));
            }
        });
    }
}