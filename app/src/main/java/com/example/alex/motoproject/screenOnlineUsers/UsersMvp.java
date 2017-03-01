package com.example.alex.motoproject.screenOnlineUsers;

import java.util.List;

public interface UsersMvp {
    interface ViewToPresenter {
        void onStart();
        void onStop();
        void onViewCreated();
    }

    interface PresenterToView {
        void notifyItemInserted(int position);

        void notifyItemChanged(int position);

        void notifyItemRemoved(int position);

        void notifyDataSetChanged();

        void setListToAdapter(List<OnlineUser> users);
    }

    interface PresenterToModel {
        void registerUsersListener();

        void unregisterUsersListener();

        void clearUsers();

        List<OnlineUser> getUsers();
    }

    interface ModelToPresenter {
        void notifyItemInserted(int position);

        void notifyItemChanged(int position);

        void notifyItemRemoved(int position);
    }
}
