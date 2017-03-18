package com.example.alex.motoproject.screenOnlineUsers;

import com.example.alex.motoproject.firebase.Constants;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class UsersPresenter implements UsersMvp.ViewToPresenter, UsersMvp.ModelToPresenter {
    private static final int LIST_TYPE_FRIENDS = 10;

    private WeakReference<UsersMvp.PresenterToView> mView;
    private UsersMvp.PresenterToModel mModel;

    @Inject
    public UsersPresenter(UsersMvp.PresenterToView view) {
        mView = new WeakReference<>(view);
        mModel = new UsersModel(this);
    }

    private UsersMvp.PresenterToView getView() throws NullPointerException {
        if (mView != null) {
            return mView.get();
        } else {
            throw new NullPointerException("View is unavailable");
        }
    }

    @Override
    public void onStart() {
        switch (getView().getListType()) {
            case LIST_TYPE_FRIENDS:
                getView().setupFriendsList();
                mModel.registerFriendsListener();
                break;
            default:
                mModel.registerUsersListener();
                break;
        }
    }

    @Override
    public void onStop() {
        switch (getView().getListType()) {
            case LIST_TYPE_FRIENDS:
                mModel.unregisterFriendsListener();
                break;
            default:
                mModel.unregisterUsersListener();
                break;
        }
        getView().notifyDataSetChanged();
        mModel.clearUsers();
    }

    @Override
    public void onQueryTextChange(String query) {
        getView().replaceAllUsers(mModel.filterUsers(query));
    }

    @Override
    public void onRefreshSwipeLayout() {
        onStop();
        getView().clearUsers();
        getView().disableRefreshingSwipeLayout();
        getView().setSearchViewIconified(true);
        onStart();
    }

    @Override
    public void onUserFriendshipAccepted(String uid) {
        mModel.changeUserRelation(uid, Constants.RELATION_FRIEND);
    }

    @Override
    public void onUserFriendshipDeclined(String uid) {
        mModel.changeUserRelation(uid, null);
    }

    @Override
    public void onUserListUpdate() {
        getView().updateHeaders();
    }

    @Override
    public void onUserAdded(User user) {
        getView().addUser(user);
    }

    @Override
    public void onUserChanged(User user) {
        getView().changeUser(user);
    }

    @Override
    public void onUserRemoved(User user) {
        getView().removeUser(user);
    }

    @Override
    public void onAddNewSection(String relation) {
        getView().addNewSection(relation);
    }
}
