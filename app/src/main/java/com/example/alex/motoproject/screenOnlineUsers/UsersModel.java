package com.example.alex.motoproject.screenOnlineUsers;

import android.util.Log;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class UsersModel implements UsersMvp.PresenterToModel,
        FirebaseDatabaseHelper.UsersUpdateReceiver {
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private UsersMvp.ModelToPresenter mPresenter;

    private Map<String, List<User>> mUsers = new HashMap<>();

    UsersModel(UsersMvp.ModelToPresenter presenter) {
        App.getCoreComponent().inject(this);
        mPresenter = presenter;
    }

    @Override
    public void registerUsersListener() {
        mFirebaseDatabaseHelper.getOnlineUsers(this);
        mFirebaseDatabaseHelper.registerOnlineUsersListener(this);
    }

    @Override
    public void unregisterUsersListener() {
        mFirebaseDatabaseHelper.unregisterOnlineUsersDataListener();
    }

    @Override
    public void registerFriendsListener() {
        mFirebaseDatabaseHelper.getFriends(this);
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
    public void onUsersAdded(List<User> users) {
        Collections.sort(users);
        for (User user : users) {
            List<User> list = mUsers.get(user.getRelation());
            if (list != null) {
                list.add(user);
            } else {
                List<User> newList = new ArrayList<>();
                newList.add(user);
                mUsers.put(user.getRelation(), newList);
                mPresenter.addNewSection(user.getRelation(), newList);
            }
        }
        mPresenter.notifyDataSetChanged();
    }

    @Override
    public void onUserAdded(User user) {
        List<User> list = mUsers.get(user.getRelation());
        if (list != null) {
            list.add(user);
            Collections.sort(list);
            mPresenter.notifyDataSetChanged();
            Log.e("listPos", String.valueOf(list.indexOf(user)));
        } else {
            List<User> newList = new ArrayList<>();
            newList.add(user);
            mUsers.put(user.getRelation(), newList);
            mPresenter.addNewSection(user.getRelation(), newList);
            mPresenter.notifyDataSetChanged();
            Log.e("newListPos", String.valueOf(newList.indexOf(user)));
        }
    }

    @Override
    public void onUserChanged(User user) {
        for (List<User> iteratedList : mUsers.values()) {
            for (User iteratedUser : iteratedList) {
                if (iteratedUser.getUid().equals(user.getUid())) {
                    if (!iteratedUser.getRelation().equals(user.getRelation())) {
                        iteratedList.remove(iteratedUser);
                        onUserAdded(user);
                        return;
                    }
                    iteratedList.set(iteratedList.indexOf(iteratedUser), user);
                    mPresenter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    @Override
    public void onUserDeleted(User user) {
        List<User> list = mUsers.get(user.getRelation());
        if (list == null) {
            for (List<User> iteratedList : mUsers.values()) {
                deleteUser(iteratedList, user);
            }
            return;
        }
        deleteUser(list, user);
    }

    private void deleteUser(List<User> users, User user) {
        for (User iteratedUser : users) {
            if (iteratedUser.getUid().equals(user.getUid())) {
                users.remove(users.indexOf(iteratedUser));
                mPresenter.notifyDataSetChanged();
                return;
            }
        }
    }

    public Map<String, List<User>> filterUsers(String query) {
        final String lowerCaseQuery = query.toLowerCase();

        Map<String, List<User>> filteredUsers = new HashMap<>();
        List<String> mapKeys = new ArrayList<>(mUsers.keySet());
        int iteration = 0;
        for (List<User> list : mUsers.values()) {
            List<User> users = new ArrayList<>();
            for (User user : list) {
                String string = user.getName().toLowerCase();
                if (string.contains(lowerCaseQuery)) {
                    users.add(user);
                }
            }
            filteredUsers.put(mapKeys.get(iteration), users);
            iteration++;
        }
        return filteredUsers;
    }

    @Override
    public void changeUserRelation(String uid, String relation) {
        mFirebaseDatabaseHelper.changeUserRelation(uid, relation);
    }
}
