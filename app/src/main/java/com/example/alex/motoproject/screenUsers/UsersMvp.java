package com.example.alex.motoproject.screenUsers;

import java.util.List;
import java.util.Map;

public interface UsersMvp {
    interface ViewToPresenter {
        void onStart();

        void onStop();

        void onQueryTextChange(String newText);

        void onRefreshSwipeLayout();

        void onUserFriendshipAccepted(String uid);

        void onUserFriendshipDeclined(String uid);

        void onUserListUpdate();

        void onViewAttached(UsersMvp.PresenterToView view);
    }

    interface PresenterToView {
        void addNewSection(String relation);

        void setupFriendsList();

        void addUser(User user);

        void changeUser(User user);

        void removeUser(User user);

        void showEmptyView();

        void clearUsers();

        void updateHeaders();

        void replaceAllUsers(Map<String, List<User>> users);

        void notifyDataSetChanged();

        void disableRefreshingSwipeLayout();

        void setSearchViewIconified(boolean iconified);

        int getListType();
    }

    interface PresenterToModel {
        void registerUsersListener();

        void unregisterUsersListener();

        void registerFriendsListener();

        void unregisterFriendsListener();

        void setRelationToUser(String uid, String relation);

        void setUserRelation(String uid, String relation);

        Map<String, List<User>> filterUsers(String query);
    }

    interface ModelToPresenter {
        void onAddNewSection(String relation);

        void onUserAdded(User user);

        void onUserChanged(User user);

        void onUserRemoved(User user);

        void onNoUsers();
    }
}
