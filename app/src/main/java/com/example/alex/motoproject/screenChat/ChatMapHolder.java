package com.example.alex.motoproject.screenChat;

import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OpenMapEvent;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

class ChatMapHolder extends BaseChatItemHolder {

    private ImageView mStaticMap;
    private ProgressBar mProgressBar;

    ChatMapHolder(View itemView) {
        super(itemView);
        mStaticMap = (ImageView) itemView.findViewById(R.id.chat_message_map);
        mProgressBar = (ProgressBar) itemView.findViewById(R.id.chat_message_progressbar);
    }

    void setStaticMap(final Context context, final LatLng location) {
        mStaticMap.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mStaticMap.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        mProgressBar.getLayoutParams().height = mStaticMap.getWidth() / 2;

                        Picasso.with(context)
                                .load(StaticMapHelper.createStaticMapLink(
                                        location, mStaticMap.getWidth(), mStaticMap.getWidth() / 2))
                                .into(mStaticMap, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        mProgressBar.setVisibility(View.GONE);
                                        setStaticMapOnClickListener(location);
                                    }

                                    @Override
                                    public void onError() {
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                });
    }

    private void setStaticMapOnClickListener(final LatLng latLng) {
        mStaticMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new OpenMapEvent(latLng));
            }
        });
    }
}
