package com.example.alex.motoproject.screenOnlineUsers;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        //Add or remove header
        for (Section section : mAdapter.getSectionsMap().values()) {
            if (section.hasHeader()) {
                if (section.getContentItemsTotal() < 1) {
                    section.setHasHeader(false); //remove header if it has no children
                }
            } else if (section.getContentItemsTotal() >= 1) {
                UsersSection usersSection = (UsersSection) section;
                if (usersSection.getTitle() != null) { //add header if it has one or more children
                    section.setHasHeader(true); //and if there is something to show in this header
                }
            }
        }
    }

    @Override
    public void onStart() {
        mPresenter.onStart();
        super.onStart();
    }

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
                mPresenter.onQueryTextChange(newText);
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
//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
//                layoutManager.getOrientation());
//        rv.addItemDecoration(dividerItemDecoration);
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

    @Override
    public void clearUsers() {
        mAdapter.removeAllSections();
    }

    @Override
    public void replaceAllUsers(Map<String, List<User>> users) {
        List<String> mapKeys = new ArrayList<>(users.keySet());
        int iteration = 0;
        for (List<User> list : users.values()) {
            UsersSection section = (UsersSection) mAdapter.getSection(mapKeys.get(iteration));
            section.setUsers(list);
            notifyDataSetChanged();
            iteration++;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.removeAllSections();
        mPresenter = null;
    }

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

    @Override
    public void setSearchViewIconified(boolean iconified) {
        mSearchView.setIconified(iconified);
    }


    @Override
    public void setupFriendsList() {
        //Makes pending friends section always show on top if it has any child
        Resources res = getContext().getResources();
        String title = res.getString(R.string.title_pending_friends);
        PendingFriendSection pfs = new PendingFriendSection(title, new ArrayList<User>());
        mAdapter.addSection("pending", pfs);
    }

    @Override
    public void setupUsersList() {

    }

    @Override
    public void addNewSection(String relation, List<User> list) {
        if (relation == null) {
            Section section = new UsersSection(null, list);
//            section.setHasHeader(false);
            mAdapter.addSection(null, section);
        } else {
            Resources res = getContext().getResources();
            String title;
            switch (relation) {
                case "pending":
//                    title = res.getString(R.string.title_pending_friends);
//                    mAdapter.addSection(relation, new PendingFriendSection(title, list));
                    PendingFriendSection pfs = (PendingFriendSection) mAdapter.getSection(relation);
                    pfs.setUsers(list);
                    break;
                default:
                    title = res.getString(R.string.title_friends);
                    mAdapter.addSection(relation, new UsersSection(title, list));
                    break;
            }

        }
    }

    private class PendingFriendSection extends UsersSection {

        private PendingFriendSection(String title, List<User> users) {
            super(title, users, R.layout.item_users_header, R.layout.item_friends_friend_pending);
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new PendingFriendViewHolder(view);
        }
    }

    class UsersSection extends StatelessSection {
        private String mTitle;
        private List<User> mUsers;

        private UsersSection(String title, List<User> users) {
            super(R.layout.item_users_header, R.layout.item_user);
            setHasHeader(false);
            mTitle = title;
            mUsers = users;
        }

        private UsersSection(String title, List<User> users,
                             int headerLayout, int itemLayout) {
            super(headerLayout, itemLayout);
            setHasHeader(false);
            mTitle = title;
            mUsers = users;
        }

        void setUsers(List<User> users) {
            mUsers = users;
            if (mUsers == null) {
                setVisible(false);
            } else {
                setVisible(true);
            }
        }

        private String getTitle() {
            return mTitle;
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
            if (holder instanceof SectionedRecyclerViewAdapter.EmptyViewHolder) {
                return;
            }
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.title.setText(mTitle);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            User user = mUsers.get(position);

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

            private String getTitle() {
                return mTitle;
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
