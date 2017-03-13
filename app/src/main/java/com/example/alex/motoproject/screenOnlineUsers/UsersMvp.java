package com.example.alex.motoproject.screenOnlineUsers;

import java.util.List;
import java.util.Map;

public interface UsersMvp {
    interface ViewToPresenter {
        void onStart();

        void onStop();

        void onViewCreated();

        void onQueryTextChange(String newText);

        void onRefreshSwipeLayout();

        void onUserFriendshipAccepted(String uid);

        void onUserFriendshipDeclined(String uid);
    }

    interface PresenterToView {
        void notifyItemInserted(int position);

        void notifyItemChanged(int position);

        void notifyItemRemoved(int position);

        void notifyDataSetChanged();

        int getListType();

        void clearUsers();

        void disableRefreshingSwipeLayout();

        void setSearchViewIconified(boolean iconified);

        void addNewSection(String relation, List<User> list);

        void replaceAllUsers(Map<String, List<User>> users);

        void setupFriendsList();

        void setupUsersList();
    }

    interface PresenterToModel {
        void registerUsersListener();

        void unregisterUsersListener();

        void registerFriendsListener();

        void unregisterFriendsListener();

        void clearUsers();

        Map<String, List<User>> filterUsers(String query);

        void changeUserRelation(String uid, String relation);

//        void setListType(int listType);
    }

    interface ModelToPresenter {
//        void addUser(OnlineUser user);
//
//        void updateUser(OnlineUser user);
//
//        void removeUser(OnlineUser user);

//        void notifyItemInserted(int position);
//
//        void notifyItemChanged(int position);
//
//        void notifyItemRemoved(int position);

        void notifyDataSetChanged();

        void addNewSection(String relation, List<User> list);
    }
}
