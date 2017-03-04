package com.example.alex.motoproject.screenOnlineUsers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OpenMapEvent;
import com.example.alex.motoproject.event.ShowUserProfileEvent;
import com.example.alex.motoproject.util.CircleTransform;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.VH> {
    private static final int TYPE_PENDING_FRIEND = 10;
    private List<OnlineUser> mUsers;

    UsersAdapter() {

    }

    public void setUsersList(List<OnlineUser> users) {
        mUsers = users;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        VH viewHolder;
        switch (viewType) {
            case TYPE_PENDING_FRIEND:
                itemView = inflater.inflate(R.layout.item_friends_friend_pending, parent, false);
                viewHolder = new PendingFriendVH(itemView);
                break;
            default:
                itemView = inflater.inflate(R.layout.who_is_online_list_row_small, parent, false);
                viewHolder = new VH(itemView);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_PENDING_FRIEND:
                PendingFriendVH pendingFriendVH = (PendingFriendVH) holder;
                bindUser(pendingFriendVH, position);
                bindPendingFriend(pendingFriendVH, position);
                break;
            default:
                bindUser(holder, position);
                break;
        }

    }

    private void bindUser(final VH holder, int position) {
        holder.name.setText(mUsers.get(position).getName());

        Picasso.with(holder.avatar.getContext())
                .load(mUsers.get(position).getAvatar())
                .resize(holder.avatar.getMaxWidth(), holder.avatar.getMaxHeight())
                .centerCrop()
                .transform(new CircleTransform())
                .into(holder.avatar);

        if (mUsers.get(position).getStatus() != null) {
            if (mUsers.get(position).getStatus().equals("public")) {
                holder.mapCur.setVisibility(View.VISIBLE);
            } else {
                holder.mapCur.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mUsers.get(position).getRelation() == null) {
            return super.getItemViewType(position);
        }
        Log.e(String.valueOf(mUsers.get(position).getName()), String.valueOf(mUsers.get(position).getRelation()));
        switch (mUsers.get(position).getRelation()) {
            case "pending":
                return TYPE_PENDING_FRIEND;
            default:
                return super.getItemViewType(position);
        }
    }

    private void bindPendingFriend(final VH holder, int position) {
        PendingFriendVH pendingFriendVH = (PendingFriendVH) holder;
        pendingFriendVH.setButtonsClickListeners();
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        ImageView mapCur;
        TextView name;

        VH(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Show user profile
                    EventBus.getDefault().post(new ShowUserProfileEvent(
                            mUsers.get(getAdapterPosition()).getUid()));

                }
            });

            avatar = (ImageView) itemView.findViewById(R.id.friends_list_ava);
            name = (TextView) itemView.findViewById(R.id.userName);
            mapCur = (ImageView) itemView.findViewById(R.id.friends_list_map_icon);
            mapCur.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new OpenMapEvent(
                            mUsers.get(getAdapterPosition()).getUid()));
                }

            });
        }
    }

    private class PendingFriendVH extends VH {
        Button acceptFriendshipButton;
        Button declineFriendshipButton;

        PendingFriendVH(View itemView) {
            super(itemView);
            acceptFriendshipButton = (Button) itemView.findViewById(R.id.button_accept_friend);
            declineFriendshipButton = (Button) itemView.findViewById(R.id.button_decline_friend);
        }

        void setButtonsClickListeners() {
            acceptFriendshipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            declineFriendshipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
