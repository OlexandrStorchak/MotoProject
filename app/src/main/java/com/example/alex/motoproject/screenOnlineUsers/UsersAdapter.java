package com.example.alex.motoproject.screenOnlineUsers;

import android.support.v7.util.SortedList;
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
    private final SortedList.Callback<OnlineUser> mSortCallback = new SortedList.Callback<OnlineUser>() {

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public int compare(OnlineUser o1, OnlineUser o2) {
            return o1.getName().compareTo(o2.getName());
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(OnlineUser oldItem, OnlineUser newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(OnlineUser item1, OnlineUser item2) {
            return item1.getUid().equals(item2.getUid());
        }
    };
    private final SortedList<OnlineUser> mUsers =
            new SortedList<>(OnlineUser.class, mSortCallback);
    private UsersAdapterListener mUsersFragment;

    UsersAdapter(UsersAdapterListener listener) {
        mUsersFragment = listener;
    }

    void addUser(OnlineUser user) {
        mUsers.add(user);
    }

    void removeUser(OnlineUser user) {
        mUsers.remove(user);
    }

    void clearUsers() {
        mUsers.clear();
    }

    void replaceAll(List<OnlineUser> models) {
        mUsers.beginBatchedUpdates();
        for (int i = mUsers.size() - 1; i >= 0; i--) {
            final OnlineUser model = mUsers.get(i);
            if (!models.contains(model)) {
                mUsers.remove(model);
            }
        }
        mUsers.addAll(models);
        mUsers.endBatchedUpdates();
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
        pendingFriendVH.setButtonsClickListeners(mUsers.get(position).getUid());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    interface UsersAdapterListener {
        void onUserFriendshipAccepted(String uid);

        void onUserFriendshipDeclined(String uid);
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

        void setButtonsClickListeners(final String uid) {
            acceptFriendshipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUsersFragment.onUserFriendshipAccepted(uid);
                }
            });

            declineFriendshipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mUsersFragment.onUserFriendshipDeclined(uid);
                }
            });
        }
    }
}
