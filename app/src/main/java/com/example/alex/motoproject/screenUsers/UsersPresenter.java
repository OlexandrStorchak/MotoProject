package com.example.alex.motoproject.screenUsers;

import com.example.alex.motoproject.firebase.FirebaseConstants;

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
//        mModel.clearUsers();
    }

    @Override
    public void onQueryTextChange(String query) {
        getView().replaceAllUsers(mModel.filterUsers(query));
    }

    @Override
    public void onRefreshSwipeLayout() {
        onStop();
        getView().setSearchViewIconified(true);
        getView().clearUsers();
        getView().disableRefreshingSwipeLayout();
        onStart();
    }

    @Override
    public void onUserFriendshipAccepted(String uid) {
        mModel.setRelationToUser(uid, FirebaseConstants.RELATION_FRIEND);
        mModel.setUserRelation(uid, FirebaseConstants.RELATION_FRIEND);
    }

    @Override
    public void onUserFriendshipDeclined(String uid) {
        mModel.setRelationToUser(uid, null);
    }

    @Override
    public void onUserListUpdate() {
        getView().updateHeaders();
    }

    @Override
    public void onViewAttached(UsersMvp.PresenterToView view) {
        mView = new WeakReference<>(view);
    }

    @Override
    public void onItemCountChanged(int recyclerViewItems) {
        if (recyclerViewItems > 0 && !mModel.isUserListEmpty()) {
            getView().hideEmptyView();
        } else {
            getView().showEmptyView();
        }

    }

    @Override
    public void onUserAdded(User user) {
//        if (!hasUserRequiredData(user)) return;
        getView().addUser(user);
        onUserListUpdate();
    }

//    private boolean hasUserRequiredData(User user) {
//        return user != null && user.getName() != null
//                && user.getUid() != null && user.getAvatar() != null;
//    }

    @Override
    public void onUserChanged(User user) {
//        if (!hasUserRequiredData(user)) return;
        getView().changeUser(user);
    }

    @Override
    public void onUserRemoved(User user) {
        getView().removeUser(user);
    }

    @Override
    public void onNoUsers() {
        getView().showEmptyView();
    }

    @Override
    public void onAddNewSection(String relation) {
        getView().addNewSection(relation);
    }
}
