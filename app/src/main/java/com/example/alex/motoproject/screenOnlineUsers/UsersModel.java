package com.example.alex.motoproject.screenOnlineUsers;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import java.util.ArrayList;
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
            mPresenter.notifyItemInserted(list.indexOf(user));
        } else {
            List<OnlineUser> newList = new ArrayList<>();
            newList.add(user);
            mUsers.put(user.getRelation(), newList);
            mPresenter.notifyItemInserted(newList.indexOf(user));
            mPresenter.addNewSection(user.getRelation());
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

        List<OnlineUser> list = mUsers.get(user.getRelation());
        for (OnlineUser iteratedUser : list) {
            if (iteratedUser.getUid().equals(user.getUid())) {
                list.set(list.indexOf(iteratedUser), user);
                mPresenter.notifyItemChanged(list.indexOf(user));
                return;
            }
        }
    }

    @Override
    public void onUserDeleted(OnlineUser user) {
        List<OnlineUser> list = mUsers.get(user.getRelation());
        for (OnlineUser iteratedUser : list) {
            if (iteratedUser.getUid().equals(user.getUid())) {
                list.remove(list.indexOf(iteratedUser));
                mPresenter.notifyItemRemoved(list.indexOf(iteratedUser));
                return;
            }
        }
    }

    public List<OnlineUser> filterUsers(String query) {
//        final String lowerCaseQuery = query.toLowerCase();
//
//        final List<OnlineUser> filteredUserList = new ArrayList<>();
//        for (OnlineUser user : mUsers) {
//            final String text = user.getName().toLowerCase();
//            if (text.contains(lowerCaseQuery)) {
//                filteredUserList.add(user);
//            }
//        }
//        return filteredUserList;
        return null;
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
