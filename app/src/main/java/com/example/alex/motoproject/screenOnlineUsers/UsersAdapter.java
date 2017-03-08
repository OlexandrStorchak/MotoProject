package com.example.alex.motoproject.screenOnlineUsers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

class UsersAdapter extends SectionedRecyclerViewAdapter {
    private static final int TYPE_PENDING_FRIEND = 10;
    private List<OnlineUser> mUsers;
    private Map<String, List<OnlineUser>> mAllUsers;
    private UsersAdapterListener mUsersFragment;

    UsersAdapter(UsersAdapterListener listener) {
        mUsersFragment = listener;
    }

    //    void addUser(OnlineUser user) {
//        mUsers.add(user);
//        notifyItemInserted(mUsers.indexOf(user));
//    }
//
//    void removeUser(OnlineUser user) {
//        mUsers.remove(user);
//    }
//    void setUsers(List<OnlineUser> users) {
//        mUsers = users;
////        mAllUsers.put()
//    }

    void setUsers(Map<String, List<OnlineUser>> users) {
        mAllUsers = users;
    }

    void clearUsers() {
//        mUsers.clear();
        mAllUsers.clear();
    }

    void replaceAll(List<OnlineUser> models) {
//        mUsers.beginBatchedUpdates();
        for (int i = mUsers.size() - 1; i >= 0; i--) {
            final OnlineUser model = mUsers.get(i);
            if (!models.contains(model)) {
                mUsers.remove(model);
            }
        }
        mUsers.addAll(models);
//        mUsers.endBatchedUpdates();
    }

    void addNewSection(String relation) {
        if (relation == null) {
            addSection(null, new UsersSection("Users", mAllUsers.get(null), null));
            return;
        }
        switch (relation) {
            case "pending":
                addSection(relation, new PendingFriendsSection("Pending", mAllUsers.get(relation)));
                break;
            default:
                addSection(relation, new UsersSection("Users", mAllUsers.get(relation), relation));
                break;
        }


//        List<OnlineUser> list = new ArrayList<>();
//        for (OnlineUser user : mUsers) {
//            if (user.getRelation().equals("pending")) {
//                list.add(user);
//            }
//        }
//        addSection(new PendingFriendsSection("Pending", list));
    }

//    @Override
//    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
//        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//        View itemView;
//        VH viewHolder;
//        switch (viewType) {
//            case TYPE_PENDING_FRIEND:
//                itemView = inflater.inflate(R.layout.item_friends_friend_pending, parent, false);
//                viewHolder = new PendingFriendVH(itemView);
//                List<OnlineUser> pendingFriendsList = new ArrayList<>();
//                for (OnlineUser user : mUsers) {
//                    if (user.getRelation().equals("pending")) {
//                        pendingFriendsList.add(user);
//                    }
//                }
//                addSection(new PendingFriendsSection("Запрошення", pendingFriendsList));
//                break;
//            default:
//                itemView = inflater.inflate(R.layout.item_user, parent, false);
//                viewHolder = new VH(itemView);
//                break;
//        }
//        return viewHolder;
//    }

//    @Override
//    public void onBindViewHolder(final VH holder, int position) {
//        switch (holder.getItemViewType()) {
//            case TYPE_PENDING_FRIEND:
//                PendingFriendVH pendingFriendVH = (PendingFriendVH) holder;
//                bindUser(pendingFriendVH, position);
//                bindPendingFriend(pendingFriendVH);
//                break;
//            default:
//                bindUser(holder, position);
//                break;
//        }
//
//    }
//
//    private void bindUser(final VH holder, int position) {
//        holder.name.setText(mUsers.get(position).getName());
//
//        Picasso.with(holder.avatar.getContext())
//                .load(mUsers.get(position).getAvatar())
//                .resize(holder.avatar.getMaxWidth(), holder.avatar.getMaxHeight())
//                .centerCrop()
//                .transform(new CircleTransform())
//                .into(holder.avatar);
//
//        if (mUsers.get(position).getStatus() != null) {
//            if (mUsers.get(position).getStatus().equals("public")) {
//                holder.mapCur.setVisibility(View.VISIBLE);
//            } else {
//                holder.mapCur.setVisibility(View.GONE);
//            }
//        }
//    }

//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return super.onCreateViewHolder(parent, viewType);
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        if (mUsers.get(position).getRelation() == null) {
//            return super.getItemViewType(position);
//        }
//
//        switch (mUsers.get(position).getRelation()) {
//            case "pending":
//                return TYPE_PENDING_FRIEND;
//            default:
//                return super.getItemViewType(position);
//        }
//    }

    private void bindPendingFriend(final VH holder) {
        PendingFriendVH pendingFriendVH = (PendingFriendVH) holder;
        pendingFriendVH.setButtonsClickListeners();
    }

//    @Override
//    public int getItemCount() {
//        return mUsers.size();
//    }

    interface UsersAdapterListener {
        void onUserFriendshipAccepted(String uid);

        void onUserFriendshipDeclined(String uid);
    }

    public class PendingFriendsSection extends StatelessSection {
        private List<OnlineUser> mPendingFriends;
        private String title;

        public PendingFriendsSection(String title, List<OnlineUser> pendingFriends) {
            super(R.layout.item_users_header, R.layout.item_friends_friend_pending);
            mPendingFriends = pendingFriends;
            this.title = title;
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            super.onBindHeaderViewHolder(holder);
            if (holder instanceof HeaderViewHolder) {
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.title.setText(title);
            }
        }

        @Override
        public int getContentItemsTotal() {
            if (mPendingFriends == null) {
                return 0;
            }
            return mPendingFriends.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new PendingFriendVH(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            VH itemHolder = (VH) holder;

            Log.e("PositionFriend", String.valueOf(position) + String.valueOf(getSectionPosition(position)) + String.valueOf(itemHolder.getAdapterPosition() + String.valueOf(position)));

            itemHolder.name.setText(mPendingFriends.get(position).getName());

            Picasso.with(itemHolder.avatar.getContext())
                    .load(mPendingFriends.get(position).getAvatar())
                    .resize(itemHolder.avatar.getMaxWidth(), itemHolder.avatar.getMaxHeight())
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(itemHolder.avatar);

            if (mPendingFriends.get(position).getStatus() != null) {
                if (mPendingFriends.get(position).getStatus().equals("public")) {
                    itemHolder.mapCur.setVisibility(View.VISIBLE);
                } else {
                    itemHolder.mapCur.setVisibility(View.GONE);
                }
            }

//            itemHolder.setButtonsClickListeners();
        }
    }

    public class UsersSection extends StatelessSection {
        private List<OnlineUser> mUsers;
        private String mTitle;
        private String mRelation;

        public UsersSection(String title, List<OnlineUser> users, String relation) {
            super(R.layout.item_users_header, R.layout.item_user);
            mUsers = users;
            mTitle = title;
            mRelation = relation;
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            super.onBindHeaderViewHolder(holder);
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.title.setText(mTitle);
        }

        @Override
        public int getContentItemsTotal() {
            if (mUsers == null) {
                return 0;
            }
            return mUsers.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new VH(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            VH itemHolder = (VH) holder;
            position = getSectionPosition(itemHolder.getAdapterPosition());
            Log.e("Position", String.valueOf(position) + String.valueOf(getSectionPosition(position)) + String.valueOf(itemHolder.getAdapterPosition() + String.valueOf(position)));
            OnlineUser user = mAllUsers.get(mRelation).get(position);
            Log.e("User", user.getName());
            itemHolder.name.setText(user.getName());

            Picasso.with(itemHolder.avatar.getContext())
                    .load(user.getAvatar())
                    .resize(itemHolder.avatar.getMaxWidth(), itemHolder.avatar.getMaxHeight())
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(itemHolder.avatar);

            if (user.getStatus() != null) {
                if (user.getStatus().equals("public")) {
                    itemHolder.mapCur.setVisibility(View.VISIBLE);
                } else {
                    itemHolder.mapCur.setVisibility(View.GONE);
                }
            }
        }
    }

    private class VH extends RecyclerView.ViewHolder {
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
                            mUsers.get(getSectionPosition(getAdapterPosition())).getUid()));

                }
            });

            avatar = (ImageView) itemView.findViewById(R.id.friends_list_ava);
            name = (TextView) itemView.findViewById(R.id.userName);
            mapCur = (ImageView) itemView.findViewById(R.id.friends_list_map_icon);
            mapCur.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new OpenMapEvent(
                            mUsers.get(getSectionPosition(getAdapterPosition())).getUid()));
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
//                mUsersFragment.onUserFriendshipAccepted(mUsers.get(getAdapterPosition()).getUid());
                }
            });

            declineFriendshipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                mUsersFragment.onUserFriendshipDeclined(mUsers.get(getAdapterPosition()).getUid());
                }
            });
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;

        public HeaderViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title_header_users);
        }
    }
}
