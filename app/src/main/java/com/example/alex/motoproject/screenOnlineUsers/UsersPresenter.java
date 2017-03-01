package com.example.alex.motoproject.screenOnlineUsers;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class UsersPresenter implements UsersMvp.ViewToPresenter, UsersMvp.ModelToPresenter{

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
        mModel.registerUsersListener();
    }

    @Override
    public void onStop() {
        getView().notifyDataSetChanged();
        mModel.unregisterUsersListener();
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
