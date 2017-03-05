package com.example.alex.motoproject.screenOnlineUsers;

import java.util.List;

public interface UsersMvp {
    interface ViewToPresenter {
        void onStart();

        void onStop();

        void onViewCreated();

        void onQueryTextChange(String newText);

        void onRefreshSwipeLayout();
    }

    interface PresenterToView {
        void notifyItemInserted(int position);

        void notifyItemChanged(int position);

        void notifyItemRemoved(int position);

        void notifyDataSetChanged();

//        void setListToAdapter(List<OnlineUser> users);

        int getListType();

        void addOrUpdateUser(OnlineUser user);

        void removeUser(OnlineUser user);

        void replaceAllUsers(List<OnlineUser> filteredUsers);

        void clearUsers();

        void disableRefreshingSwipeLayout();
    }

    interface PresenterToModel {
        void registerUsersListener();

        void unregisterUsersListener();

        void registerFriendsListener();

        void unregisterFriendsListener();

        void clearUsers();

        List<OnlineUser> getUsers();

        List<OnlineUser> filterUsers(String query);
    }

    interface ModelToPresenter {
        void addOrUpdateUser(OnlineUser user);

        void removeUser(OnlineUser user);

        void notifyItemInserted(int position);

        void notifyItemChanged(int position);

        void notifyItemRemoved(int position);
    }
}
