package com.example.alex.motoproject.screenOnlineUsers;

import java.lang.ref.WeakReference;
import java.util.List;

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
                getView().setupUsersList();
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
    public void onViewCreated() {
//        getView().setUserList(mModel.getUsers());
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
        mModel.changeUserRelation(uid, "friend");
    }

    @Override
    public void onUserFriendshipDeclined(String uid) {
        mModel.changeUserRelation(uid, null);
    }

//    @Override
//    public List<OnlineUser> onGetUsersList() {
//        return mModel.getUsers();
//    }

//    @Override
//    public void addUser(OnlineUser user) {
//        getView().addUser(user);
//    }
//
//    @Override
//    public void updateUser(OnlineUser user) {
//        getView().updateUser(user);
//    }
//
//    @Override
//    public void removeUser(OnlineUser user) {
//        getView().removeUser(user);
//    }

//    @Override
//    public void notifyItemInserted(int position) {
//        getView().notifyItemInserted(position);
//    }
//
//    @Override
//    public void notifyItemChanged(int position) {
//        getView().notifyItemChanged(position);
//    }
//
//    @Override
//    public void notifyItemRemoved(int position) {
//        getView().notifyItemRemoved(position);
//    }

    @Override
    public void notifyDataSetChanged() {
        getView().notifyDataSetChanged();
    }

    @Override
    public void addNewSection(String relation, List<User> list) {
        getView().addNewSection(relation, list);
    }
}
