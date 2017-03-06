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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.DaggerPresenterComponent;
import com.example.alex.motoproject.PresenterModule;
import com.example.alex.motoproject.R;

import java.util.List;

import javax.inject.Inject;

public class UsersFragment extends Fragment
        implements UsersMvp.PresenterToView, UsersAdapter.UsersAdapterListener {
    public UsersAdapter mAdapter = new UsersAdapter(this);
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
                // TODO: 05.03.2017 without checking the app crashes when trying to open up details fragment
                if (mPresenter != null) {
                    mPresenter.onQueryTextChange(newText);
                }

                return true;
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
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.navigation_friends_list_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                layoutManager.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(mAdapter);
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
    public void addOrUpdateUser(OnlineUser user) {
        mAdapter.addUser(user);
    }

    @Override
    public void removeUser(OnlineUser user) {
        mAdapter.removeUser(user);
    }

    @Override
    public void replaceAllUsers(List<OnlineUser> filteredUsers) {
        mAdapter.replaceAll(filteredUsers);
    }

    @Override
    public void clearUsers() {
        mAdapter.clearUsers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.clearUsers();
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

    @Override
    public void onUserFriendshipAccepted(String uid) {
        mPresenter.onUserFriendshipAccepted(uid);
    }

    @Override
    public void onUserFriendshipDeclined(String uid) {
        mPresenter.onUserFriendshipDeclined(uid);
    }

    @Override
    public void setSearchViewIconified(boolean iconified) {
        mSearchView.setIconified(iconified);
    }
}
