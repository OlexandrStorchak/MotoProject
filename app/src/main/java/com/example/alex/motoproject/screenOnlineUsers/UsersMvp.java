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

//        List<OnlineUser> onGetUsersList();
    }

    interface PresenterToView {
//        void setUserList(Map<String, List<OnlineUser>> users);

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
//        void clearUsers();

        void disableRefreshingSwipeLayout();

        void setSearchViewIconified(boolean iconified);

//        void removeAllSelections();

        void addNewSection(String relation, List<OnlineUser> list);
    }

    interface PresenterToModel {
        void registerUsersListener();

        void unregisterUsersListener();

        void registerFriendsListener();

        void unregisterFriendsListener();

        void clearUsers();

        Map<String, List<OnlineUser>> getUsers();

        List<OnlineUser> filterUsers(String query);

        void changeUserRelation(String uid, String relation);

        void setListType(int listType);
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

        void notifyDataSetChanged();

        void addNewSection(String relation, List<OnlineUser> list);
    }
}
