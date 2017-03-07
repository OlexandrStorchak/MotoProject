package com.example.alex.motoproject.screenOnlineUsers;

import android.util.Log;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

public class UsersModel implements UsersMvp.PresenterToModel,
        FirebaseDatabaseHelper.OnlineUsersUpdateReceiver {
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private UsersMvp.ModelToPresenter mPresenter;
    private List<OnlineUser> mUsers = new ArrayList<>();

    public UsersModel(UsersMvp.ModelToPresenter presenter) {
        App.getCoreComponent().inject(this);
        mPresenter = presenter;
    }

    @Override
    public void registerUsersListener() {
        mFirebaseDatabaseHelper.registerOnlineUsersListener(this);
    }

    @Override
    public void unregisterUsersListener() {
        mFirebaseDatabaseHelper.unregisterOnlineUsersDataListener();
    }

    @Override
    public void registerFriendsListener() {
        mFirebaseDatabaseHelper.registerFriendsListener(this);
    }

    @Override
    public void unregisterFriendsListener() {
        mFirebaseDatabaseHelper.unregisterFriendsListener();
    }

    @Override
    public void clearUsers() {
        mUsers.clear();
    }

    @Override
    public List<OnlineUser> getUsers() {
        return mUsers;
    }

    @Override
    public void onUserAdded(OnlineUser user) {
        mUsers.add(user);
        Log.e("User from Model", user.getName());
        Collections.sort(mUsers);
        mPresenter.notifyItemInserted(mUsers.indexOf(user));
    }

    @Override
    public void onUserChanged(OnlineUser user) {
        for (OnlineUser iteratedUser : mUsers) {
            if (iteratedUser.getUid().equals(user.getUid())) {
                mUsers.set(mUsers.indexOf(iteratedUser), user);
                Collections.sort(mUsers);
                mPresenter.notifyItemChanged(mUsers.indexOf(iteratedUser));
                return;
            }
        }
    }

    @Override
    public void onUserDeleted(OnlineUser user) {
        for (OnlineUser iteratedUser : mUsers) {
            if (iteratedUser.getUid().equals(user.getUid())) {
                mUsers.remove(mUsers.indexOf(iteratedUser));
                mPresenter.notifyItemRemoved(mUsers.indexOf(iteratedUser));
                break;
            }
        }
        mUsers.remove(user);
    }

    public List<OnlineUser> filterUsers(String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<OnlineUser> filteredUserList = new ArrayList<>();
        for (OnlineUser user : mUsers) {
            final String text = user.getName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredUserList.add(user);
            }
        }
        return filteredUserList;
    }

    @Override
    public void changeUserRelation(String uid, String relation) {
        mFirebaseDatabaseHelper.changeUserRelation(uid, relation);
    }
}
