package com.example.alex.motoproject.screenChat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OpenMapWithLatLngEvent;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

class ChatMsgOwnHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;
    private ImageView mStaticMap;
    private TextView mSendTime;
    private ProgressBar mProgressBar;

    ChatMsgOwnHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.chat_messageown_text);
        mSendTime = (TextView) itemView.findViewById(R.id.chat_messageown_date);
        mStaticMap = (ImageView) itemView.findViewById(R.id.chat_messageown_map);
        mProgressBar = (ProgressBar) itemView.findViewById(R.id.chat_messageown_progressbar);
    }

    void setMessageText(String text) {
        mTextView.setText(text);
    }

    void setSendTime(String dateTime) {
        mSendTime.setText(dateTime);
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

    void setStaticMapOnClickListener(final LatLng latLng) {
        mStaticMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new OpenMapWithLatLngEvent(latLng));
            }
        });
    }
}
