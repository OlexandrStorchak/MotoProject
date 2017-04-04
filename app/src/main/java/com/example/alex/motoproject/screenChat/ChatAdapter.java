package com.example.alex.motoproject.screenChat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_MAP = 1;
    private static final int TYPE_MESSAGE_OWN = 20;
    private static final int TYPE_MAP_OWN = 21;

    private List<ChatMessage> mMessages;
    private Context mContext;

    private int mStaticMapWidth;
    private int mStaticMapHeight;


    ChatAdapter(List<ChatMessage> messages, int maxImageWidth, int maxImageHeight) {
        mMessages = messages;
        mStaticMapWidth = maxImageWidth;
        mStaticMapHeight = maxImageHeight;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case TYPE_MESSAGE:
                itemView = inflater.inflate(R.layout.item_chat_message, parent, false);
                viewHolder = new ChatMessageHolder(itemView);
                break;
            case TYPE_MAP:
                itemView = inflater.inflate(R.layout.item_chat_mapmessage, parent, false);
                viewHolder = new ChatMapHolder(itemView);
                break;
            case TYPE_MESSAGE_OWN:
                itemView = inflater.inflate(R.layout.item_chat_ownmessage, parent, false);
                viewHolder = new ChatMessageHolder(itemView);
                break;
            case TYPE_MAP_OWN:
                itemView = inflater.inflate(R.layout.item_chat_mapownmessage, parent, false);
                viewHolder = new ChatMapHolder(itemView);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_MESSAGE:
                ChatMessageHolder chatMessageHolder = (ChatMessageHolder) holder;
                bindMessage(chatMessageHolder, position);
                break;
            case TYPE_MAP:
                ChatMapHolder chatMapHolder = (ChatMapHolder) holder;
                bindMessage(chatMapHolder, position);
                break;
            case TYPE_MESSAGE_OWN:
                ChatMessageHolder chatMsgOwnHolder = (ChatMessageHolder) holder;
                bindMessageOwn(chatMsgOwnHolder, position);
                break;
            case TYPE_MAP_OWN:
                ChatMapHolder chatMapOwnHolder = (ChatMapHolder) holder;
                bindMessageOwn(chatMapOwnHolder, position);
        }
    }

    private void bindMessage(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = mMessages.get(position);
        String uid = message.getUid();
        String text = message.getText();
        String name = message.getName();
        String avatarRef = message.getAvatarRef();
        String sendTime = message.getSendTime();
        LatLng location = message.getLocation();

        BaseChatItemHolder baseHolder = (BaseChatItemHolder) holder;
        baseHolder.setUserAvatarViewOnClickListener(uid);

//        if (name == null) {
//            return;
//        }

        baseHolder.setName(name);
        baseHolder.setAvatar(avatarRef, mContext);
        baseHolder.setSendTime(sendTime);

        switch (holder.getItemViewType()) {
            case TYPE_MESSAGE:
                ChatMessageHolder msgHolder = (ChatMessageHolder) holder;
                msgHolder.setMessageText(text);
                break;
            case TYPE_MAP:
                ChatMapHolder mapHolder = (ChatMapHolder) holder;
                mapHolder.setStaticMap(StaticMapHelper.createStaticMapLink(location,
                        mStaticMapWidth, mStaticMapHeight), mContext);
                mapHolder.setStaticMapOnClickListener(location);
                break;
        }
    }


    private void bindMessageOwn(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = mMessages.get(position);
        String text = message.getText();
        String sendTime = message.getSendTime();
        LatLng location = message.getLocation();

        BaseChatItemHolder baseHolder = (BaseChatItemHolder) holder;
        baseHolder.setSendTime(sendTime);

        switch (holder.getItemViewType()) {
            case TYPE_MESSAGE_OWN:
                ChatMessageHolder msgHolder = (ChatMessageHolder) holder;
                msgHolder.setMessageText(text);
                break;
            case TYPE_MAP_OWN:
                ChatMapHolder mapHolder = (ChatMapHolder) holder;
                mapHolder.setStaticMap(StaticMapHelper.createStaticMapLink(location,
                        mStaticMapWidth, mStaticMapHeight), mContext);
                mapHolder.setStaticMapOnClickListener(location);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = mMessages.get(position);
        if (message.isCurrentUserMsg()) {
            if (message.getText() != null) {
                return TYPE_MESSAGE_OWN;
            } else if (message.getLocation() != null) {
                return TYPE_MAP_OWN;
            }
        } else {
            if (message.getText() != null) {
                return TYPE_MESSAGE;
            } else if (message.getLocation() != null) {
                return TYPE_MAP;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    void clearMessages() {
        mMessages.clear();
    }
}
