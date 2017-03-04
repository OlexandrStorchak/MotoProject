package com.example.alex.motoproject.screenOnlineUsers;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class UsersPresenter implements UsersMvp.ViewToPresenter, UsersMvp.ModelToPresenter {
    private static final int LIST_TYPE_ONLINE_USERS = 0;
    private static final int LIST_TYPE_FRIENDS = 10;
    private int mUserListType = 0;

    private WeakReference<UsersMvp.PresenterToView> mView;
    private UsersMvp.PresenterToModel mModel = new UsersModel(this);

    @Inject
    public UsersPresenter(UsersMvp.PresenterToView view) {
        mView = new WeakReference<>(view);
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
    public void onViewCreated() {
        getView().setListToAdapter(mModel.getUsers());
    }

    @Override
    public void notifyItemInserted(int position) {
        getView().notifyItemInserted(position);
    }

    @Override
    public void notifyItemChanged(int position) {
        getView().notifyItemChanged(position);
    }

    @Override
    public void notifyItemRemoved(int position) {
        getView().notifyItemRemoved(position);
    }
}
