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
    public void clearUsers() {
        mUsers.clear();
    }

    @Override
    public List<OnlineUser> getUsers() {
        return mUsers;
    }

    @Override
    public void onUserAdded(OnlineUser onlineUser) {
        mUsers.add(onlineUser);
        mPresenter.notifyItemInserted(mUsers.indexOf(onlineUser));
    }

    @Override
    public void onUserChanged(OnlineUser onlineUser) {
        String thisUserId = onlineUser.getUid();
        for (OnlineUser iteratedUser : mUsers) {
            if (iteratedUser.getUid().equals(thisUserId)) {
                mUsers.set(mUsers.indexOf(iteratedUser), onlineUser);
                mPresenter.notifyItemChanged(mUsers.indexOf(onlineUser));
                return;
            }
        }
    }

    @Override
    public void onUserDeleted(OnlineUser onlineUser) {
        mPresenter.notifyItemRemoved(mUsers.indexOf(onlineUser));
        mUsers.remove(onlineUser);
    }
}
