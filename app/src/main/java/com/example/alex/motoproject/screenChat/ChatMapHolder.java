package com.example.alex.motoproject.screenChat;

import android.content.Context;
import android.view.View;
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
//
//    public void setName(String name) {
//        super.setName(name);
//    }
//
//    void setSendTime(String dateTime) {
//        super.setSendTime(dateTime);
//    }

    void setStaticMap(String mapLink, Context ctx) {
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
//        Glide.with(ctx)
//                .load(mapLink)
//                .listener(new RequestListener<String, GlideDrawable>() {
//                    @Override
//                    public boolean onException(Exception e, String model,
//                                               Target<GlideDrawable> target,
//                                               boolean isFirstResource) {
//                        mProgressBar.setVisibility(View.GONE);
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(GlideDrawable resource, String model,
//                                                   Target<GlideDrawable> target,
//                                                   boolean isFromMemoryCache,
//                                                   boolean isFirstResource) {
//                        mProgressBar.setVisibility(View.GONE);
//                        return false;
//                    }
//                })
//                .into(mStaticMap);
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
