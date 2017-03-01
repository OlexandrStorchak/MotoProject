package com.example.alex.motoproject.screenOnlineUsers;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.DaggerPresenterComponent;
import com.example.alex.motoproject.PresenterModule;
import com.example.alex.motoproject.R;

import java.util.List;

import javax.inject.Inject;

public abstract class BaseUsersFragment extends Fragment
        implements UsersMvp.PresenterToView {
    // TODO: 02.03.2017 inject interface, not presenter itself
    @Inject
    UsersPresenter mPresenter;
    public BaseUsersAdapter mAdapter;

    public BaseUsersFragment(BaseUsersAdapter adapter) {
        mAdapter = adapter;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        mPresenter.onViewCreated();
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.navigation_friends_list_recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(mAdapter);
    }

    @Override
    public void setListToAdapter(List<OnlineUser> users) {
        mAdapter.setUsersList(users);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter = null;
    }
//    @Override
//    public void onUserAdded(OnlineUser onlineUser) {
//        mUsers.add(onlineUser);
//        mAdapter.notifyItemInserted(mUsers.indexOf(onlineUser));
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

//    @Override
//    public void onUserChanged(OnlineUser onlineUser) {
//        String thisUserId = onlineUser.getUid();
//        for (OnlineUser iteratedUser : mUsers) {
//            if (iteratedUser.getUid().equals(thisUserId)) {
//                mUsers.set(mUsers.indexOf(iteratedUser), onlineUser);
//                mAdapter.notifyItemChanged(mUsers.indexOf(onlineUser));
//                return;
//            }
//        }
//    }
//
//    @Override
//    public void onUserDeleted(OnlineUser onlineUser) {
//        mAdapter.notifyItemRemoved(mUsers.indexOf(onlineUser));
//        mUsers.remove(onlineUser);
//    }
}
