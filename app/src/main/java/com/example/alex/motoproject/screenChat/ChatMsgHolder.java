package com.example.alex.motoproject.screenChat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OpenMapEvent;
import com.example.alex.motoproject.util.CircleTransform;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

class ChatMsgHolder extends RecyclerView.ViewHolder {

    private ImageView mUserAvatarView;
    private ImageView mStaticMap;
    private TextView mNameView;
    private TextView mTextView;
    private TextView mSendTime;
    private ProgressBar mProgressBar;

    ChatMsgHolder(View itemView) {
        super(itemView);
        mUserAvatarView = (ImageView) itemView.findViewById(R.id.chat_message_avatar);
        mNameView = (TextView) itemView.findViewById(R.id.chat_message_name);
        mTextView = (TextView) itemView.findViewById(R.id.chat_message_text);
        mSendTime = (TextView) itemView.findViewById(R.id.chat_message_date);
        mStaticMap = (ImageView) itemView.findViewById(R.id.chat_message_map);
        mProgressBar = (ProgressBar) itemView.findViewById(R.id.chat_message_progressbar);

        mUserAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 21.02.2017 an action
                Log.d("onClick", String.valueOf(getAdapterPosition()));
            }
        });
    }

    public void setName(String name) {
        mNameView.setText(name);
    }

    void setMessageText(String text) {
        mTextView.setText(text);
    }

    void setStaticMap(String mapLink, Context ctx) {
        mTextView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        Picasso.with(ctx).load(mapLink).into(mStaticMap, new Callback() {
            @Override
            public void onSuccess() {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    void setSendTime(String dateTime) {
        mSendTime.setText(dateTime);
    }

    void setAvatar(String avatarRef, Context ctx) {
        Picasso.with(ctx).load(avatarRef).fit().centerCrop()
                .transform(new CircleTransform()).into(mUserAvatarView);
    }

    void setStaticMapOnClickListener(final LatLng latLng) {
        mStaticMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new OpenMapEvent(latLng));
            }
        });
    }
}
