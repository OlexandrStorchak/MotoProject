package com.example.alex.motoproject.screenChat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.R;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

class ChatAdapter extends RecyclerView.Adapter<MessagesHolder> {

    private List<ChatMessage> mMessages;
    private Context mContext;
    private String mLastMessageUid;

    ChatAdapter(List<ChatMessage> messages, Context ctx) {
        mMessages = messages;
        mContext = ctx;
    }

    @Override
    public MessagesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chatmessage, parent, false);

        return new MessagesHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessagesHolder holder, int position) {
        ChatMessage message = mMessages.get(position);
        String uid = message.getUid();
        String text = message.getText();
        String name = message.getName();
        String avatarRef = message.getAvatarRef();

        if (uid.equals(mLastMessageUid)) {
            holder.modifyMessageText(text);
        } else {
            holder.setMessageText(text);
            holder.setName(name);
            holder.setAvatar(avatarRef, mContext);
            mLastMessageUid = uid;
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
}
