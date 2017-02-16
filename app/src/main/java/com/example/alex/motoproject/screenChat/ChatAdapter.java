package com.example.alex.motoproject.screenChat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements FirebaseDatabaseHelper.ChatUpdateListener {

    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_MESSAGE_OWN = 1;
    private List<ChatMessage> mMessages;
    private Context mContext;

    public ChatAdapter() {
        mMessages = new ArrayList<>();
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

        ChatMsgHolder msgHolder = (ChatMsgHolder) holder;
        msgHolder.setMessageText(text);
        msgHolder.setName(name);
        msgHolder.setAvatar(avatarRef, mContext);
    }

    private void bindMessageOwn(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = mMessages.get(position);
        String text = message.getText();
        ChatMsgOwnHolder msgOwnHolder = (ChatMsgOwnHolder) holder;
        msgOwnHolder.setMessageText(text);
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

    @Subscribe
    public void onNewChatMessage(ChatMessage message) {
        mMessages.add(message);
        notifyItemInserted(mMessages.size() - 1);
    }

    // TODO: 15.02.2017 get rid of EventBus implementation by making this method run on UI thread
    @Override
    public void updateChat(ChatMessage message) {
        mMessages.add(message);
        notifyItemInserted(mMessages.size() - 1);
    }
}
