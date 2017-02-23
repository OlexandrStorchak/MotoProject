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
    private static final int TYPE_MESSAGE_OWN = 1;
    private List<ChatMessage> mMessages;
    private Context mContext;

    ChatAdapter(List<ChatMessage> messages) {
        mMessages = messages;
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
                viewHolder = new ChatMsgHolder(itemView);
                break;
            case TYPE_MESSAGE_OWN:
                itemView = inflater.inflate(R.layout.item_chat_ownmessage, parent, false);
                viewHolder = new ChatMsgOwnHolder(itemView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_MESSAGE:
                bindMessage(holder, position);
                break;
            case TYPE_MESSAGE_OWN:
                ChatMsgOwnHolder vh = (ChatMsgOwnHolder) holder;
                bindMessageOwn(vh, position);
                break;
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

        ChatMsgHolder msgHolder = (ChatMsgHolder) holder;
        if (text != null) {
            msgHolder.setMessageText(text);
        } else {
            msgHolder.setStaticMap(StaticMapHelper.createStaticMapLink(location), mContext);
            msgHolder.setStaticMapOnClickListener(location);
        }
        msgHolder.setName(name);
        msgHolder.setAvatar(avatarRef, mContext);
        msgHolder.setSendTime(sendTime);
    }

    private void bindMessageOwn(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = mMessages.get(position);
        String text = message.getText();
        String sendTime = message.getSendTime();
        LatLng location = message.getLocation();

        ChatMsgOwnHolder msgOwnHolder = (ChatMsgOwnHolder) holder;
        if (text != null) {
            msgOwnHolder.setMessageText(text);
        } else {
            msgOwnHolder.setStaticMap(StaticMapHelper.createStaticMapLink(location), mContext);
            msgOwnHolder.setStaticMapOnClickListener(location);
        }
        msgOwnHolder.setMessageText(text);
        msgOwnHolder.setSendTime(sendTime);
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessages.get(position).isCurrentUserMsg()) {
            return TYPE_MESSAGE_OWN;
        } else {
            return TYPE_MESSAGE;
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}
