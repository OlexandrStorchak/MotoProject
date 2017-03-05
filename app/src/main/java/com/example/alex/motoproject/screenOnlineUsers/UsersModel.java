package com.example.alex.motoproject.screenOnlineUsers;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import java.util.ArrayList;
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
        mPresenter.addOrUpdateUser(user);
    }

    @Override
    public void onUserChanged(OnlineUser user) {
        mPresenter.addOrUpdateUser(user);

        for (OnlineUser iteratedUser : mUsers) {
            if (iteratedUser.getUid().equals(user.getUid())) {
                mUsers.set(mUsers.indexOf(iteratedUser), user);
                return;
            }
        }
    }

    @Override
    public void onUserDeleted(OnlineUser user) {
        for (OnlineUser iteratedUser : mUsers) {
            if (iteratedUser.getUid().equals(user.getUid())) {
                mPresenter.removeUser(iteratedUser);
                mUsers.remove(mUsers.indexOf(iteratedUser));
                return;
            }
        }
        mUsers.remove(user);
        mPresenter.removeUser(user);
    }

    public List<OnlineUser> filterUsers(String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<OnlineUser> filteredModelList = new ArrayList<>();
        for (OnlineUser user : mUsers) {
            final String text = user.getName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(user);
            }
        }
        return filteredModelList;
    }
}
