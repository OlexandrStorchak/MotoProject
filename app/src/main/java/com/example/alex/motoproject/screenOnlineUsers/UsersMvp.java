package com.example.alex.motoproject.screenOnlineUsers;

import java.util.List;

public interface UsersMvp {
    interface ViewToPresenter {
        void onStart();

        void onStop();

        void onViewCreated();

        void onQueryTextChange(String newText);

        void onRefreshSwipeLayout();

        void onUserFriendshipAccepted(String uid);

        void onUserFriendshipDeclined(String uid);

        List<OnlineUser> onGetUsersList();
    }

    interface PresenterToView {
        void setUserList(List<OnlineUser> users);

        void notifyItemInserted(int position);

        void notifyItemChanged(int position);

        void notifyItemRemoved(int position);

        void notifyDataSetChanged();

//        void setListToAdapter(List<OnlineUser> users);

        int getListType();

        //        void addUser(OnlineUser user);
//
//        void updateUser(OnlineUser user);
//
//        void removeUser(OnlineUser user);
//
        void replaceAllUsers(List<OnlineUser> filteredUsers);

        void clearUsers();

        void disableRefreshingSwipeLayout();

        void setSearchViewIconified(boolean iconified);

//        void removeAllSelections();
    }

    interface PresenterToModel {
        void registerUsersListener();

        void unregisterUsersListener();

        void registerFriendsListener();

        void unregisterFriendsListener();

        void clearUsers();

        List<OnlineUser> getUsers();

        List<OnlineUser> filterUsers(String query);

        void changeUserRelation(String uid, String relation);
    }

    interface ModelToPresenter {
//        void addUser(OnlineUser user);
//
//        void updateUser(OnlineUser user);
//
//        void removeUser(OnlineUser user);

        void notifyItemInserted(int position);

        void notifyItemChanged(int position);

        void notifyItemRemoved(int position);
    }
}
