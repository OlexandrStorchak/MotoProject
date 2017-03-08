package com.example.alex.motoproject.screenOnlineUsers;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.DaggerPresenterComponent;
import com.example.alex.motoproject.PresenterModule;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OpenMapEvent;
import com.example.alex.motoproject.event.ShowUserProfileEvent;
import com.example.alex.motoproject.util.CircleTransform;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class UsersFragment extends Fragment implements UsersMvp.PresenterToView {
    public SectionedRecyclerViewAdapter mAdapter = new SectionedRecyclerViewAdapter();
    // TODO: 02.03.2017 inject interface, not presenter itself
    @Inject
    UsersPresenter mPresenter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView mSearchView;

    public UsersFragment() {

    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.onRefreshSwipeLayout();
            }
        });
    }

    @Override
    public void onStop() {
        mPresenter.onStop();
        super.onStop();
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();

        //Remove a header, if there are no items in a section except the header itself
        for (Section section : mAdapter.getSectionsMap().values()) {
            if (section.getSectionItemsTotal() < 2 && section.hasHeader()) {
                section.setHasHeader(false);
            }
        }

    }

    @Override
    public void onStart() {
        mPresenter.onStart();
        super.onStart();
    }

//    @Override
//    public void removeAllSelections() {
//        mAdapter.removeAllSections();
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.appbar_userlist, menu);

        final MenuItem searchItem = menu.findItem(R.id.search_users);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO: 05.03.2017 without checking the app crashes when trying to open up details fragment when searching
                if (mPresenter != null) {
                    mPresenter.onQueryTextChange(newText);
                }

                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        DaggerPresenterComponent.builder()
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users_online, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.container_users_swipe);
        setupSwipeRefreshLayout();

        mPresenter.onViewCreated();

        if (!mAdapter.hasStableIds()) {
            mAdapter.setHasStableIds(true);
        }

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.navigation_friends_list_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                layoutManager.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(mAdapter);
//        mAdapter.addPendingFriendSection();
//        rv.setItemAnimator(null);
    }

    @Override
    public int getListType() {
        if (getArguments() != null) {
            return getArguments().getInt("listType", 0);
        } else {
            return 0;
        }
    }

//    @Override
//    public void replaceAllUsers(List<OnlineUser> filteredUsers) {
//        mAdapter.replaceAll(filteredUsers);
//    }

//    @Override
//    public void clearUsers() {
//        mAdapter.clearUsers();
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        mAdapter.clearUsers();
        mAdapter.removeAllSections();
        mPresenter = null;
    }

//    @Override
//    public void setUserList(Map<String, List<OnlineUser>> users) {
//        mAdapter.setUsers(users);
//    }

    @Override
    public void notifyItemInserted(int position) {
        mAdapter.notifyItemInserted(position);
    }

    @Override
    public void notifyItemChanged(int position) {
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void notifyItemRemoved(int position) {
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void disableRefreshingSwipeLayout() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void onUserFriendshipAccepted(String uid) {
        mPresenter.onUserFriendshipAccepted(uid);
    }

    public void onUserFriendshipDeclined(String uid) {
        mPresenter.onUserFriendshipDeclined(uid);
    }

//    @Override
//    public void setUserList(List<OnlineUser> users) {
//        mAdapter.setUsers(users);
//    }

    @Override
    public void setSearchViewIconified(boolean iconified) {
        mSearchView.setIconified(iconified);
    }

    @Override
    public void addNewSection(String relation, List<OnlineUser> list) {
        if (relation == null) {
            mAdapter.addSection(new UsersSection(null, list));
        } else {
            switch (relation) {
                case "pending":
                    mAdapter.addSection(new PendingFriendSection(relation, list));
                    break;
                default:
                    mAdapter.addSection(new UsersSection(relation, list));
                    break;
            }
        }
    }

    private class PendingFriendSection extends UsersSection {

        private PendingFriendSection(String title, List<OnlineUser> users) {
            super(title, users, R.layout.header_users, R.layout.item_friends_friend_pending);
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new PendingFriendViewHolder(view);
        }

//        @Override
//        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
//            PendingFriendViewHolder pendingFriendViewHolder = (PendingFriendViewHolder) holder;
//        }
    }

    class UsersSection extends StatelessSection {
        private String mTitle;
        private List<OnlineUser> mUsers;

        private UsersSection(String title, List<OnlineUser> users) {
            super(R.layout.header_users, R.layout.item_user);
            mTitle = title;
            mUsers = users;
        }

        private UsersSection(String title, List<OnlineUser> users,
                             int headerLayout, int itemLayout) {
            super(headerLayout, itemLayout);
            mTitle = title;
            mUsers = users;
        }

        @Override
        public int getContentItemsTotal() {
            return mUsers.size();
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new UserViewHolder(view);
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            if (mUsers.isEmpty()) {
                return new SectionedRecyclerViewAdapter.EmptyViewHolder(view);
            } else {
                return new HeaderViewHolder(view);
            }
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.title.setText(mTitle);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            OnlineUser user = mUsers.get(position);

            Log.e("OnBindPos", String.valueOf(position) + " "
                    + String.valueOf(mAdapter.getSectionPosition(position)));

            userViewHolder.name.setText(user.getName());
            Picasso.with(userViewHolder.avatar.getContext())
                    .load(user.getAvatar())
                    .resize(userViewHolder.avatar.getMaxWidth(),
                            userViewHolder.avatar.getMaxHeight())
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(userViewHolder.avatar);
            if (user.getStatus() != null && user.getStatus().equals("public")) {
                userViewHolder.mapCur.setVisibility(View.VISIBLE);
            } else {
                userViewHolder.mapCur.setVisibility(View.GONE);
            }
        }

        private class UserViewHolder extends RecyclerView.ViewHolder {
            View rootView;
            ImageView avatar;
            TextView name;
            ImageView mapCur;

            private UserViewHolder(View view) {
                super(view);
                rootView = view;

                avatar = (ImageView) view.findViewById(R.id.friends_list_ava);
                name = (TextView) view.findViewById(R.id.userName);
                mapCur = (ImageView) view.findViewById(R.id.friends_list_map_icon);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("OnClickPos", String.valueOf(getAdapterPosition()));
                        EventBus.getDefault().post(new ShowUserProfileEvent(
                                mUsers.get(mAdapter.getSectionPosition(getAdapterPosition())).getUid()
                        ));
                    }
                });

                mapCur.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EventBus.getDefault().post(new OpenMapEvent(
                                mUsers.get(mAdapter.getSectionPosition(getAdapterPosition())).getUid()));
                    }

                });
            }
        }

        class PendingFriendViewHolder extends UserViewHolder {
            Button acceptFriendshipButton;
            Button declineFriendshipButton;

            PendingFriendViewHolder(View view) {
                super(view);
                acceptFriendshipButton = (Button) view.findViewById(R.id.button_accept_friend);
                declineFriendshipButton = (Button) view.findViewById(R.id.button_decline_friend);

                acceptFriendshipButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onUserFriendshipAccepted(mUsers.get(mAdapter
                                .getSectionPosition(getAdapterPosition())).getUid());
                    }
                });

                declineFriendshipButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onUserFriendshipDeclined(mUsers.get(mAdapter
                                .getSectionPosition(getAdapterPosition())).getUid());
                    }
                });
            }
        }

        private class HeaderViewHolder extends RecyclerView.ViewHolder {
            View rootView;
            TextView title;

            HeaderViewHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                title = (TextView) itemView.findViewById(R.id.title_header_users);
            }
        }
    }
}
