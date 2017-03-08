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
        FirebaseDatabaseHelper.OnlineUsersUpdateReceiver {
    private static final int LIST_TYPE_FRIENDS = 10;
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private UsersMvp.ModelToPresenter mPresenter;
    private int mListType;

    private Map<String, List<OnlineUser>> mUsers = new HashMap<>();

    public UsersModel(UsersMvp.ModelToPresenter presenter, int listType) {
        App.getCoreComponent().inject(this);
        mPresenter = presenter;
        mListType = listType;
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
    public Map<String, List<OnlineUser>> getUsers() {
//        Collections.sort(mUsers);
//        return mUsers;
        return mUsers;
    }

    @Override
    public void onUserAdded(OnlineUser user) {
////        Collections.sort(mUsers);
//
//        mUsers.add(user);
//        Log.e("User from Model", user.getName());
//        mPresenter.notifyItemInserted(mUsers.indexOf(user));
        List<OnlineUser> list = mUsers.get(user.getRelation());
        if (list != null) {
            list.add(user);
            Collections.sort(list);
//            mPresenter.notifyItemInserted(list.indexOf(user));
            mPresenter.notifyDataSetChanged();
            Log.e("listPos", String.valueOf(list.indexOf(user)));
        } else {
            List<OnlineUser> newList = new ArrayList<>();
            newList.add(user);
            mUsers.put(user.getRelation(), newList);
            mPresenter.addNewSection(user.getRelation(), newList);
            mPresenter.notifyDataSetChanged();
//            mPresenter.notifyItemInserted(newList.indexOf(user));
            Log.e("newListPos", String.valueOf(newList.indexOf(user)));
        }
    }

    @Override
    public void onUserChanged(OnlineUser user) {
//        for (OnlineUser iteratedUser : mUsers) {
//            if (iteratedUser.getUid().equals(user.getUid())) {
//                mUsers.set(mUsers.indexOf(iteratedUser), user);
////                Collections.sort(mUsers);
//                mPresenter.notifyItemChanged(mUsers.indexOf(iteratedUser));
//                return;
//            }
//        }

//        List<OnlineUser> list = mUsers.get(user.getRelation());
        for (List<OnlineUser> iteratedList : mUsers.values()) {
            for (OnlineUser iteratedUser : iteratedList) {
                if (iteratedUser.getUid().equals(user.getUid())) {
                    if (!iteratedUser.getRelation().equals(user.getRelation())) {
                        iteratedList.remove(iteratedUser);
                        onUserAdded(user);
                        return;
                    }
                    iteratedList.set(iteratedList.indexOf(iteratedUser), user);
                    mPresenter.notifyDataSetChanged();
//                mPresenter.notifyItemChanged(list.indexOf(user));
                    return;
                }
            }
        }
    }

    @Override
    public void onUserDeleted(OnlineUser user) {
        List<OnlineUser> list = mUsers.get(user.getRelation());
        if (list == null) {
            for (List<OnlineUser> iteratedList : mUsers.values()) {
                deleteUser(iteratedList, user);
            }
            return;
        }
        deleteUser(list, user);
    }

    private void deleteUser(List<OnlineUser> users, OnlineUser user) {
        for (OnlineUser iteratedUser : users) {
            if (iteratedUser.getUid().equals(user.getUid())) {
                users.remove(users.indexOf(iteratedUser));
//                mPresenter.notifyItemRemoved(list.indexOf(iteratedUser));
                mPresenter.notifyDataSetChanged();
                return;
            }
        }
    }

    public Map<String, List<OnlineUser>> filterUsers(String query) {
        final String lowerCaseQuery = query.toLowerCase();

        Map<String, List<OnlineUser>> filteredUsers = new HashMap<>();
        List<String> mapKeys = new ArrayList<>(mUsers.keySet());
        int iteration = 0;
        for (List<OnlineUser> list : mUsers.values()) {
            List<OnlineUser> users = new ArrayList<>();
            for (OnlineUser user : list) {
                String string = user.getName().toLowerCase();
                if (string.contains(lowerCaseQuery)) {
                    users.add(user);
                }
            }
            filteredUsers.put(mapKeys.get(iteration), users);
            iteration++;
//            if (!users.isEmpty()) {
//                filteredUsers.put(users.get(0).getRelation(), users);
//            }
        }
        return filteredUsers;
    }

    @Override
    public void changeUserRelation(String uid, String relation) {
        mFirebaseDatabaseHelper.changeUserRelation(uid, relation);
    }

    @Override
    public void setListType(int listType) {
        mListType = listType;
    }
}
