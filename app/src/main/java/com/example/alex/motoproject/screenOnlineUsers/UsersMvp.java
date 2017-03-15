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

        void addNewSection(String relation);

        void replaceAllUsers(Map<String, List<User>> users);

        void setupFriendsList();

        void setupUsersList();

        void addUser(User user);

        void changeUser(User user);

        void removeUser(User user);
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
        void onUserAdded(User user);

        void onUserChanged(User user);

        void onUserRemoved(User user);

//        void notifyItemInserted(int position);
//
//        void notifyItemChanged(int position);
//
//        void notifyItemRemoved(int position);

        void notifyDataSetChanged();

        void addNewSection(String relation);
    }
}
