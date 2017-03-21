package com.example.alex.motoproject.screenOnlineUsers;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import java.util.ArrayList;
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
        mFirebaseDatabaseHelper.getOnlineUsersAndRegisterListener(this);
    }

    @Override
    public void unregisterUsersListener() {
        mFirebaseDatabaseHelper.unregisterOnlineUsersDataListener();
    }

    @Override
    public void registerFriendsListener() {
        mFirebaseDatabaseHelper.getFriendsAndRegisterListener(this);
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
        for (User user : users) {
            List<User> list = mUsers.get(user.getRelation());
            if (list != null) {
                list.add(user);
            } else {
                List<User> newList = new ArrayList<>();
                newList.add(user);
                mUsers.put(user.getRelation(), newList);
                mPresenter.onAddNewSection(user.getRelation());
            }
            mPresenter.onUserAdded(user);
        }
    }

    @Override
    public boolean hasUser(String uidToCheck, String relation) {
        List<User> list = mUsers.get(relation);

        if (list == null) {
            return false;
        }

        for (User user : list) {
            if (user.getUid().equals(uidToCheck)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onUserAdded(User user) {
        List<User> list = mUsers.get(user.getRelation());
        if (list != null) {
            list.add(user);
        } else {
            List<User> newList = new ArrayList<>();
            newList.add(user);
            mUsers.put(user.getRelation(), newList);
            mPresenter.onAddNewSection(user.getRelation());
        }
        mPresenter.onUserAdded(user);
    }

    @Override
    public void onUserChanged(User user) {
        for (List<User> iteratedList : mUsers.values()) {
            for (User iteratedUser : iteratedList) {
                if (!iteratedUser.getUid().equals(user.getUid())) {
                    if (iteratedUser.getRelation().equals(user.getRelation())) {
                        iteratedUser = user;
                        onUserAdded(iteratedUser);
                        mPresenter.onUserChanged(iteratedUser);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onUserDeleted(User user) {
        List<User> list = mUsers.get(user.getRelation());
        for (User iteratedUser : list) {
            if (iteratedUser.getUid().equals(user.getUid())) {
                mPresenter.onUserRemoved(iteratedUser);
                list.remove(iteratedUser);
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
    public void setRelationToUser(String uid, String relation) {
        mFirebaseDatabaseHelper.setRelationToUser(uid, relation);
    }

    @Override
    public void setUserRelation(String uid, String relation) {
        mFirebaseDatabaseHelper.setUserRelation(uid, relation);
    }
}
