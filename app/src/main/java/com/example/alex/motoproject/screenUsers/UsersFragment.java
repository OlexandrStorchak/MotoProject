package com.example.alex.motoproject.screenUsers;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.dagger.DaggerPresenterComponent;
import com.example.alex.motoproject.dagger.PresenterModule;
import com.example.alex.motoproject.event.OpenMapEvent;
import com.example.alex.motoproject.event.ShowUserProfileEvent;
import com.example.alex.motoproject.firebase.FirebaseConstants;
import com.example.alex.motoproject.retainFragment.FragmentWithRetainInstance;
import com.example.alex.motoproject.transformation.GlideCircleTransform;
import com.example.alex.motoproject.util.DimensHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

import static com.example.alex.motoproject.util.ArgKeys.SEARCH;

public class UsersFragment extends FragmentWithRetainInstance
        implements UsersMvp.PresenterToView {
    private static final String LIST_TYPE_KEY = "listType";
    public SectionedRecyclerViewAdapter mAdapter = new SectionedRecyclerViewAdapter() {
        @Override
        public long getItemId(int position) {
            return position;
        }
    };
    @Inject
    UsersMvp.ViewToPresenter mPresenter;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView mSearchView;

    private String mSearchViewQuery;

    private View mEmptyView;
    private RecyclerView.AdapterDataObserver mDataObserver =
            new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() { //Show custom View if no children in RecyclerView
                    mPresenter.onItemCountChanged(mAdapter.getItemCount());
                }
            };

    private boolean mDestroyed;

    public UsersFragment() {

    }

    @Override
    public boolean isDestroyed() {
        return mDestroyed;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;
        mSearchViewQuery = savedInstanceState.getString(SEARCH);

        mPresenter = (UsersMvp.ViewToPresenter) getRetainData();
        if (mPresenter == null) {
            injectThis();
        }
        mPresenter.onViewAttached(UsersFragment.this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        setRetainData(mPresenter);
        if (mSearchView != null) outState.putString(SEARCH, mSearchView.getQuery().toString());
    }

    @Override
    public String getDataTag() {
        return String.valueOf(getListType());
    }

    public void showEmptyView() {
        mRecyclerView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    public void hideEmptyView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
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
        mAdapter.unregisterAdapterDataObserver(mDataObserver);
        mPresenter.onStop();
        super.onStop();
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public void updateHeaders() {
        //Add or remove header
        for (Section section : mAdapter.getSectionsMap().values()) {
            UsersSection usersSection = (UsersSection) section;

            //remove header if it has no children
            if (section.hasHeader() && section.getContentItemsTotal() < 1) {
                section.setHasHeader(false);

                //add header if it has one or more children and title`s not null
            } else if (section.getContentItemsTotal() >= 1 && usersSection.getTitle() != null) {
                section.setHasHeader(true);
            }
        }
    }

    @Override
    public void onStart() {
        mAdapter.registerAdapterDataObserver(mDataObserver);
        mPresenter.onStart();
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.appbar_userlist, menu);

        final MenuItem searchItem = menu.findItem(R.id.search_users);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);

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
        if (mSearchViewQuery != null && !mSearchViewQuery.equals("")) {
            mSearchView.setQuery(mSearchViewQuery, true);
            mSearchView.setIconified(false);
        } else {
            MenuItemCompat.collapseActionView(searchItem);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            injectThis();
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users_online, container, false);
    }

    private void injectThis() {
        DaggerPresenterComponent.builder()
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.container_users_swipe);
        setupSwipeRefreshLayout();

        if (!mAdapter.hasStableIds()) {
            mAdapter.setHasStableIds(true);
        }

        mEmptyView = view.findViewById(R.id.view_empty);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.navigation_friends_list_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mDestroyed = false;
    }

    @Override
    public int getListType() {
        if (getArguments() != null) {
            return getArguments().getInt(LIST_TYPE_KEY);
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
            section.removeAllUsers();
            section.addUsers(list);
            iteration++;
        }
    }

    @Override
    public void onDestroyView() {
        mAdapter.removeAllSections();
        mPresenter = null;
        mDestroyed = true;
        mSearchViewQuery = null;
        super.onDestroyView();
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
        mSearchView.setQuery("", false);
        mSearchView.setIconified(iconified);
    }

    @Override
    public void setupFriendsList() {
        //Make pending friends section always show on top
        String title = getContext().getString(R.string.title_pending_friends);
        PendingFriendSection pfs = new PendingFriendSection(title);
        mAdapter.addSection(FirebaseConstants.RELATION_PENDING, pfs);
    }

    @Override
    public void addUser(User user) {
        UsersSection section = (UsersSection) mAdapter.getSection(user.getRelation());
        section.addUser(user);
    }

    @Override
    public void changeUser(User user) {
        UsersSection section = (UsersSection) mAdapter.getSection(user.getRelation());
        section.updateUser(user);
    }

    @Override
    public void removeUser(User user) {
        UsersSection section = (UsersSection) mAdapter.getSection(user.getRelation());
        section.removeUser(user);
    }

    @Override
    public void addNewSection(String relation) {
        String title;
        switch (relation) {
            case FirebaseConstants.RELATION_PENDING:
                //Pending friends section was added earlier to appear on top
                break;
            case FirebaseConstants.RELATION_FRIEND:
                title = getContext().getString(R.string.title_friends);
                mAdapter.addSection(relation, new UsersSection(title));
                break;
            default:
                mAdapter.addSection(relation, new UsersSection(null));
                break;
        }
    }

    private class PendingFriendSection extends UsersSection {

        private PendingFriendSection(String title) {
            super(title, R.layout.item_users_header, R.layout.item_friends_friend_pending);
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new PendingFriendViewHolder(view);
        }
    }

    class UsersSection extends StatelessSection {
        private String mTitle;
        private SortedList<User> mUsers = new SortedList<>(User.class,
                new SortedList.Callback<User>() {
                    @Override
                    public int compare(User o1, User o2) {
                        return o1.getName().compareTo(o2.getName());
                    }

                    @Override
                    public void onChanged(int position, int count) {
                        mAdapter.notifyItemRangeChanged(mAdapter.getSectionPosition(position), count);
                    }

                    @Override
                    public boolean areContentsTheSame(User oldItem, User newItem) {
                        return oldItem.equals(newItem);
                    }

                    @Override
                    public boolean areItemsTheSame(User item1, User item2) {
                        return item1.getUid().equals(item2.getUid());
                    }

                    @Override
                    public void onInserted(int position, int count) {
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onRemoved(int position, int count) {
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onMoved(int fromPosition, int toPosition) {
                        mAdapter.notifyItemMoved(mAdapter.getSectionPosition(fromPosition),
                                mAdapter.getSectionPosition(toPosition));
                    }
                });

        private UsersSection(String title) {
            super(R.layout.item_users_header, R.layout.item_user);
            setHasHeader(false);
            mTitle = title;
        }

        private UsersSection(String title, int headerLayout, int itemLayout) {
            super(headerLayout, itemLayout);
            setHasHeader(false);
            mTitle = title;
        }

        private void addUser(User user) {
            mUsers.add(user);
        }

        private void addUsers(List<User> users) {
            mUsers.beginBatchedUpdates();
            mUsers.addAll(users);
            mUsers.endBatchedUpdates();
            mAdapter.notifyDataSetChanged();
            mPresenter.onUserListUpdate();
        }

        private void updateUser(User user) {
            mUsers.add(user);
        }

        private void removeUser(User user) {
            mUsers.remove(user);
            mPresenter.onUserListUpdate();
        }

        private void removeAllUsers() {
            mUsers.clear();
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
            if (mUsers.size() <= 0) {
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
            final UserViewHolder userViewHolder = (UserViewHolder) holder;
            final User user = mUsers.get(position);

            userViewHolder.name.setText(user.getName());

            DimensHelper.getScaledAvatar(user.getAvatar(),
                    userViewHolder.avatar.getWidth(), new DimensHelper.AvatarRefReceiver() {
                        @Override
                        public void onRefReady(String ref) {
                            Glide.with(userViewHolder.avatar.getContext())
                                    .load(ref)
                                    .override(userViewHolder.avatar.getMaxWidth(),
                                            userViewHolder.avatar.getMaxHeight())
                                    .transform(new GlideCircleTransform(getContext()))
                                    .into(userViewHolder.avatar);

                        }

                        @Override
                        public void onError() {

                        }
                    });

            if (user.getStatus() != null && user.getStatus().equals(FirebaseConstants.STATUS_PUBLIC)) {
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

                avatar = (ImageView) view.findViewById(R.id.users_avatar);
                name = (TextView) view.findViewById(R.id.users_name);
                mapCur = (ImageView) view.findViewById(R.id.users_map_button);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
