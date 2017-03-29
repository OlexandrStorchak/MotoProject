package com.example.alex.motoproject.firebase;

import android.location.Location;
import android.support.annotation.NonNull;

import com.example.alex.motoproject.LocationModel;
import com.example.alex.motoproject.event.CurrentUserProfileReadyEvent;
import com.example.alex.motoproject.event.MapMarkerEvent;
import com.example.alex.motoproject.event.OnlineUserProfileReadyEvent;
import com.example.alex.motoproject.screenChat.ChatMessage;
import com.example.alex.motoproject.screenChat.ChatMessageSendable;
import com.example.alex.motoproject.screenUsers.User;
import com.example.alex.motoproject.service.MainServiceSosModel;
import com.example.alex.motoproject.util.DistanceUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dagger.Module;

import static com.example.alex.motoproject.firebase.Constants.CHAT_ID;
import static com.example.alex.motoproject.firebase.Constants.CHAT_LATITUDE;
import static com.example.alex.motoproject.firebase.Constants.CHAT_LONGITUDE;
import static com.example.alex.motoproject.firebase.Constants.CHAT_SEND_TIME;
import static com.example.alex.motoproject.firebase.Constants.CHAT_TEXT;
import static com.example.alex.motoproject.firebase.Constants.ONE_KILOMETER;
import static com.example.alex.motoproject.firebase.Constants.PATH_CHAT;
import static com.example.alex.motoproject.firebase.Constants.PATH_LOCATION;
import static com.example.alex.motoproject.firebase.Constants.PATH_LOCATION_LAT;
import static com.example.alex.motoproject.firebase.Constants.PATH_LOCATION_LNG;
import static com.example.alex.motoproject.firebase.Constants.PATH_ONLINE_USERS;
import static com.example.alex.motoproject.firebase.Constants.PATH_SOS;
import static com.example.alex.motoproject.firebase.Constants.PATH_USERS;
import static com.example.alex.motoproject.firebase.Constants.RELATION_FRIEND;
import static com.example.alex.motoproject.firebase.Constants.RELATION_PENDING;
import static com.example.alex.motoproject.firebase.Constants.RELATION_UNKNOWN;
import static com.example.alex.motoproject.firebase.Constants.STATUS_NO_GPS;
import static com.example.alex.motoproject.firebase.Constants.STATUS_PUBLIC;
import static com.example.alex.motoproject.firebase.Constants.STATUS_SOS;
import static com.example.alex.motoproject.firebase.Constants.USER_PROFILE_ABOUTME;
import static com.example.alex.motoproject.firebase.Constants.USER_PROFILE_AVATAR;
import static com.example.alex.motoproject.firebase.Constants.USER_PROFILE_EMAIL;
import static com.example.alex.motoproject.firebase.Constants.USER_PROFILE_FRIEND_LIST;
import static com.example.alex.motoproject.firebase.Constants.USER_PROFILE_ID;
import static com.example.alex.motoproject.firebase.Constants.USER_PROFILE_MOTORCYCLE;
import static com.example.alex.motoproject.firebase.Constants.USER_PROFILE_NAME;
import static com.example.alex.motoproject.firebase.Constants.USER_PROFILE_NICK;

@Module
public class FirebaseDatabaseHelper {

    private static final String STANDART_AVATAR =
            "https://firebasestorage.googleapis.com/v0/b/profiletests-d3a61.appspot.com/" +
                    "o/avatar_defaultar_default.png?alt=media&token=96951c00-fd27-445c-85a6-b636bd0cb9f5";
    private static final int FETCHED_CHAT_MESSAGES_MIN_COUNT_LIMIT = 31;
    private static final int SHOWN_MESSAGES_MIN_COUNT_LIMIT =
            FETCHED_CHAT_MESSAGES_MIN_COUNT_LIMIT - 1;


    private ChatUpdateReceiver mChatModel;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDbReference = mDatabase.getReference();
    private ChildEventListener mOnlineUsersLocationListener;
    private ChildEventListener mOnlineUsersDataListener;
    private ChildEventListener mFriendsListener;
    private ChildEventListener mChatMessagesListener;
    private DatabaseReference mOnlineUsersRef;

    private HashMap<String, LatLng> mUsersLocation = new HashMap<>();
    private HashMap<DatabaseReference, ValueEventListener> mLocationListeners = new HashMap<>();
    private Map<String, String> mFriends = new HashMap<>();

    private LinkedList<ChatMessage> mOlderMessages = new LinkedList<>();

    private String mFirstChatMsgKeyAfterFetch;
    private boolean isFirstChatMessageAfterFetch;
    private boolean isFirstNewChatMessageAfterFetch = true;

    private int mReceivedUsersCount;
    private int mMessagesCountLimit;
    private int mCloseDistance;

    private LatLng mCurrentUserLocation;

    private boolean isOlderMessagesFirstIteration = true;

    public FirebaseDatabaseHelper() {

    }

    public void registerAuthLoadingListener(final AuthLoadingListener listener) {
        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    firebaseAuth.removeAuthStateListener(this);
                    listener.onLoadFinished();
                }
            }
        });
    }

    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public void setUserOnline(String status) {
        DatabaseReference onlineUsers = mDbReference.child(PATH_ONLINE_USERS)
                .child(getCurrentUser().getUid());
        onlineUsers.setValue(status);
    }

    public void setUserOfflineOnDisconnect() {
        DatabaseReference onlineUsers = mDbReference.child(PATH_ONLINE_USERS)
                .child(getCurrentUser().getUid());
        onlineUsers.onDisconnect().removeValue();
    }

    public void updateUserLocation(Location location) {
        String uid = getCurrentUser().getUid();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        DatabaseReference myRef = mDbReference.child(PATH_LOCATION).child(uid);
        //Set values to two children at the same time
        myRef.setValue(new LocationModel(lat, lng));
    }

    //When the user firstly started app - pushes user data to Firebase
    public void addUserToFirebase(
            final String uid, final String email, final String name, final String avatar) {

        final String avatarUrl;
        final String nameUrl;
        if (avatar.equals("null")) {
            avatarUrl = STANDART_AVATAR;
        } else {
            avatarUrl = avatar;
        }
        if (name == null) {
            nameUrl = getCurrentUser().getEmail();
        } else {
            nameUrl = name;
        }
        final DatabaseReference currentUserRef = mDbReference.child(PATH_USERS).child(uid);

        ValueEventListener userProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Required data already exists
                    return;
                }
                //No data found by the reference, add new data
                currentUserRef.child(USER_PROFILE_EMAIL).setValue(email);
                currentUserRef.child(USER_PROFILE_NAME).setValue(nameUrl);
                currentUserRef.child(USER_PROFILE_AVATAR).setValue(avatarUrl);
                currentUserRef.child(USER_PROFILE_ID).setValue(uid);
                //Remove event listener for current databaseReference only after new data fetched
                currentUserRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        currentUserRef.addValueEventListener(userProfileListener);

    }

    /**
     * Location listeners
     */

    public void registerOnlineUsersLocationListener() {
        mOnlineUsersRef = mDbReference.child(PATH_ONLINE_USERS);
        mOnlineUsersLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postChangeMarkerEvent(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                postDeleteMarkerEvent(dataSnapshot);
                postChangeMarkerEvent(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                postDeleteMarkerEvent(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mOnlineUsersRef.addChildEventListener(mOnlineUsersLocationListener);
    }

    public void unregisterOnlineUsersLocationListener() {
        if (mOnlineUsersLocationListener != null && getCurrentUser() != null) {
            mOnlineUsersRef = mDbReference.child(PATH_ONLINE_USERS);
            mOnlineUsersRef.removeEventListener(mOnlineUsersLocationListener);
        }

        if (!mLocationListeners.isEmpty()) {
            for (Map.Entry<DatabaseReference, ValueEventListener> entry :
                    mLocationListeners.entrySet()) {
                DatabaseReference ref = entry.getKey();
                ValueEventListener listener = entry.getValue();
                ref.removeEventListener(listener);
            }
        }
    }

    public void fetchUsersLocations() {
        DatabaseReference ref = mDbReference.child(PATH_LOCATION);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot entry : dataSnapshot.getChildren()) {
                    String uid = entry.getKey();
                    Number lat = (Number) entry.child(PATH_LOCATION_LAT).getValue();
                    Number lng = (Number) entry.child(PATH_LOCATION_LNG).getValue();
                    if (lat == null || lng == null) {
                        return;
                    }
                    LatLng location = new LatLng(lat.doubleValue(), lng.doubleValue());
                    mUsersLocation.put(uid, location);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isStatusPublic(String status) {
        return status != null && status.equals(STATUS_PUBLIC);
    }

    private boolean isStatusNoGps(String status) {
        return status.equals(STATUS_NO_GPS);
    }

    private boolean isStatusSos(String status) {
        return status.equals(STATUS_SOS);
    }

    private boolean isUserFriend(String relation) {
        return relation.equals(RELATION_FRIEND);
    }

    private void postChangeMarkerEvent(DataSnapshot dataSnapshot) {
        final String status = (String) dataSnapshot.getValue();
        final String uid = dataSnapshot.getKey();

        final DatabaseReference user = mDbReference.child(PATH_USERS).child(uid);

        //Fetch relations between this user and current user
        DatabaseReference relationRef = user
                .child(USER_PROFILE_FRIEND_LIST).child(getCurrentUser().getUid());

        relationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String relation = (String) dataSnapshot.getValue();
                if (status==null) {
                    return;
                }
                if (isStatusNoGps(status) || isStatusSos(status)) {
                    return;
                }

                if (!isStatusPublic(status)) {
                    if (relation==null || !isUserFriend(relation)) {
                        return;
                    }
                }

                //fetch name
                DatabaseReference nameRef = user.child(USER_PROFILE_NAME);
                nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = (String) dataSnapshot.getValue();

                        //fetch user avatar
                        DatabaseReference avatarRef = user.child(USER_PROFILE_AVATAR);
                        avatarRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String avatarRef = (String) dataSnapshot.getValue();

                                //fetch location
                                DatabaseReference location =
                                        mDbReference.child(PATH_LOCATION).child(uid);
                                ValueEventListener listener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Number lat = (Number) dataSnapshot.child(PATH_LOCATION_LAT)
                                                .getValue();
                                        Number lng = (Number) dataSnapshot.child(PATH_LOCATION_LNG)
                                                .getValue();
                                        if (lat == null || lng == null) {
                                            return;
                                        }

                                        final LatLng latLng =
                                                new LatLng(lat.doubleValue(), lng.doubleValue());
                                        mUsersLocation.put(uid, latLng);

                                        if (uid.equals(getCurrentUser().getUid())) {
                                            return;
                                        }

                                        EventBus.getDefault().post(new MapMarkerEvent(
                                                latLng, uid, name, avatarRef, relation));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                };
                                location.addValueEventListener(listener);
                                mLocationListeners.put(location, listener);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void postDeleteMarkerEvent(DataSnapshot dataSnapshot) {
        DatabaseReference location =
                mDbReference.child(PATH_LOCATION).child(dataSnapshot.getKey());
        location.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getCurrentUser() == null) {
                    return;
                }
                String uid = dataSnapshot.getKey();
                if (!uid.equals(getCurrentUser().getUid())) {
                    EventBus.getDefault().post(new MapMarkerEvent(uid));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Users
     */

    public boolean isInFriendList(String friendId, String relation) {
        String friendRelation = mFriends.get(friendId);
        return friendRelation != null && friendRelation.equals(relation);
    }

    //set relation to a given user in current user friend list
    public void setRelationToUser(String uid, String relation) {
        DatabaseReference ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_FRIEND_LIST).child(uid);
        ref.removeValue();
        ref.setValue(relation);
    }

    //set relation to current user in a given user friend list
    public void setUserRelation(String uid, String relation) {
        DatabaseReference ref = mDbReference.child(PATH_USERS).child(uid)
                .child(USER_PROFILE_FRIEND_LIST).child(getCurrentUser().getUid());
        ref.removeValue();
        ref.setValue(relation);
    }

    public void getFriends() {
        DatabaseReference ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_FRIEND_LIST);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot entry : dataSnapshot.getChildren()) {
                    final String uid = entry.getKey();
                    final String relation = (String) entry.getValue();
                    mFriends.put(uid, relation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void getFriendsAndRegisterListener(final UsersUpdateReceiver receiver) {
        DatabaseReference ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_FRIEND_LIST);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<User> friends = new ArrayList<>();
                final int childrenCount = (int) dataSnapshot.getChildrenCount();
                mReceivedUsersCount = 0;
                for (DataSnapshot entry : dataSnapshot.getChildren()) {
                    final String uid = entry.getKey();
                    final String relation = (String) entry.getValue();
                    final String userStatus = null;
                    DatabaseReference ref = mDbReference.child(PATH_USERS).child(uid);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = (String) dataSnapshot.child(USER_PROFILE_NAME).getValue();
                            String avatar = (String) dataSnapshot.child(USER_PROFILE_AVATAR).getValue();
                            User user = new User(uid, name, avatar, userStatus, relation);
                            friends.add(user);
                            mFriends.put(uid, relation);
                            mReceivedUsersCount++;
                            if (mReceivedUsersCount == childrenCount) {
                                receiver.onUsersAdded(friends);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                registerFriendsListener(receiver);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void registerFriendsListener(final UsersUpdateReceiver receiver) {
        DatabaseReference ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_FRIEND_LIST);
        mFriendsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onFriendAdded(dataSnapshot, receiver);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                onFriendChanged(dataSnapshot, receiver);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                onFriendRemoved(dataSnapshot, receiver);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(mFriendsListener);
    }

    public void unregisterFriendsListener() {
        DatabaseReference ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_FRIEND_LIST);
        ref.removeEventListener(mFriendsListener);
    }

    private void onFriendAdded(DataSnapshot dataSnapshot, final UsersUpdateReceiver receiver) {
        final String uid = dataSnapshot.getKey();
        final String relation = (String) dataSnapshot.getValue();

        if (receiver.hasUser(uid, relation)) {
            return;
        }

        final String userStatus = null;
        DatabaseReference ref = mDbReference.child(PATH_USERS).child(uid);
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child(USER_PROFILE_NAME).getValue();
                String avatar = (String) dataSnapshot.child(USER_PROFILE_AVATAR).getValue();
                User user = new User(uid, name, avatar, userStatus, relation);
                receiver.onUserAdded(user);
                mFriends.put(uid, relation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addListenerForSingleValueEvent(userDataListener);
    }

    private void onFriendChanged(DataSnapshot dataSnapshot, final UsersUpdateReceiver receiver) {
        final String uid = dataSnapshot.getKey();
        final String relation = (String) dataSnapshot.getValue();
        final String userStatus = null;
        DatabaseReference ref = mDbReference.child(PATH_USERS).child(uid);
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child(USER_PROFILE_NAME).getValue();
                String avatar = (String) dataSnapshot.child(USER_PROFILE_AVATAR).getValue();
                User user = new User(uid, name, avatar, userStatus, relation);
                receiver.onUserChanged(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addListenerForSingleValueEvent(userDataListener);
    }

    private void onFriendRemoved(DataSnapshot dataSnapshot,
                                 final UsersUpdateReceiver receiver) {
        String uid = dataSnapshot.getKey();
        String relation = (String) dataSnapshot.getValue();
        receiver.onUserDeleted(new User(uid, relation));
        mFriends.remove(uid);
    }

    private void registerOnlineUsersListener(final UsersUpdateReceiver receiver) {
        // Read from the mDatabase
        DatabaseReference myRef = mDbReference.child(USER_PROFILE_FRIEND_LIST);
        mOnlineUsersDataListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onOnlineUserAdded(dataSnapshot, receiver);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                onOnlineUserChanged(dataSnapshot, receiver);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                onOnlineUserRemoved(dataSnapshot, receiver);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        myRef.addChildEventListener(mOnlineUsersDataListener);
    }

    public void unregisterOnlineUsersDataListener() {
        if (mOnlineUsersDataListener != null && getCurrentUser() != null) {
            DatabaseReference myRef = mDbReference.child(PATH_ONLINE_USERS);
            myRef.removeEventListener(mOnlineUsersDataListener);
        }
    }

    public void getOnlineUsersAndRegisterListener(final UsersUpdateReceiver receiver) {
        DatabaseReference ref = mDbReference.child(PATH_ONLINE_USERS);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<User> onlineUsers = new ArrayList<>();
                final int childrenCount = (int) dataSnapshot.getChildrenCount();
                mReceivedUsersCount = 0;

                for (DataSnapshot entry : dataSnapshot.getChildren()) {

                    if (entry.getKey().equals(getCurrentUser().getUid())) {
                        continue;
                    }

                    final String uid = entry.getKey();
                    final String userStatus = (String) entry.getValue();
                    DatabaseReference ref = mDbReference.child(PATH_USERS).child(uid);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = (String) dataSnapshot.child(USER_PROFILE_NAME).getValue();
                            String avatar = (String) dataSnapshot.child(USER_PROFILE_AVATAR).getValue();
                            User user = new User(
                                    uid, name, avatar, userStatus, Constants.RELATION_UNKNOWN);
                            onlineUsers.add(user);
                            mReceivedUsersCount++;
                            //+1 is for not added to list current user
                            if (mReceivedUsersCount + 1 == childrenCount) {
                                receiver.onUsersAdded(onlineUsers);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                registerOnlineUsersListener(receiver);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void onOnlineUserAdded(DataSnapshot dataSnapshot,
                                   final UsersUpdateReceiver receiver) {
        if (dataSnapshot.getKey().equals(getCurrentUser().getUid())) {
            return;
        }

        final String uid = dataSnapshot.getKey();
        final String userStatus = (String) dataSnapshot.getValue();
        final String relation = Constants.RELATION_UNKNOWN;

        if (receiver.hasUser(uid, relation)) {
            return;
        }

        DatabaseReference ref = mDbReference.child(PATH_USERS).child(uid);
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child(USER_PROFILE_NAME).getValue();
                String avatar = (String) dataSnapshot.child(USER_PROFILE_AVATAR).getValue();
                User user = new User(uid, name, avatar, userStatus, relation);
                receiver.onUserAdded(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addListenerForSingleValueEvent(userDataListener);
    }

    private void onOnlineUserChanged(DataSnapshot dataSnapshot,
                                     final UsersUpdateReceiver receiver) {
        if (dataSnapshot.getKey().equals(getCurrentUser().getUid())) {
            return;
        }
        final String uid = dataSnapshot.getKey();
        final String userStatus = (String) dataSnapshot.getValue();
        DatabaseReference ref = mDbReference.child(PATH_USERS).child(uid);
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child(USER_PROFILE_NAME).getValue();
                String avatar = (String) dataSnapshot.child(USER_PROFILE_AVATAR).getValue();
                User user = new User(uid, name, avatar, userStatus, RELATION_UNKNOWN);
                receiver.onUserChanged(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addListenerForSingleValueEvent(userDataListener);
    }

    private void onOnlineUserRemoved(DataSnapshot dataSnapshot,
                                     final UsersUpdateReceiver receiver) {
        String uid = dataSnapshot.getKey();
        receiver.onUserDeleted(new User(uid, Constants.RELATION_UNKNOWN));
    }

    /**
     * Chat
     */

    public void setCloseDistance(int closeDistance) {
        mCloseDistance = closeDistance * ONE_KILOMETER; //kilometers to meters
    }

    public void registerChatMessagesListener(final ChatUpdateReceiver receiver) {
        mCurrentUserLocation = mUsersLocation.get(getCurrentUser().getUid());

        if (mCloseDistance > 0 && mCurrentUserLocation == null) {
            mCloseDistance = 0;
            receiver.onNoCurrentUserLocation();
        }
        mChatModel = receiver;
        mChatMessagesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String uid = (String) dataSnapshot.child(CHAT_ID).getValue();
                String text = (String) dataSnapshot.child(CHAT_TEXT).getValue();
                Long sendTime = (Long) dataSnapshot.child(CHAT_SEND_TIME).getValue();
                Number lat = (Number) dataSnapshot.child(PATH_LOCATION).child(CHAT_LATITUDE).getValue();
                Number lng = (Number) dataSnapshot.child(PATH_LOCATION).child(CHAT_LONGITUDE).getValue();

                mMessagesCountLimit++;

                if (isFirstNewChatMessageAfterFetch) {
                    mFirstChatMsgKeyAfterFetch = dataSnapshot.getKey();
                    isFirstNewChatMessageAfterFetch = false;
                    return;
                }

                if (mCloseDistance > 0) {

                    if (!DistanceUtil.isClose(mCurrentUserLocation,
                            mUsersLocation.get(uid),
                            mCloseDistance)) {
                        return;
                    }
                }

                final ChatMessage message = new ChatMessage(uid, convertUnixTimeToDate(sendTime));
                if (text != null) {
                    message.setText(text);
                } else if (lat != null && lng != null) {
                    LatLng latLng = new LatLng(lat.doubleValue(), lng.doubleValue());
                    message.setLocation(latLng);
                } else {
                    return;
                }

                mChatModel.onNewChatMessage(message);
                if (message.getUid().equals(getCurrentUser().getUid())) {
                    message.setCurrentUserMsg(true);
                    return;
                }

                mDbReference.child(PATH_USERS).child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String name = (String) dataSnapshot.child(USER_PROFILE_NAME).getValue();

                                String avatarRef = (String) dataSnapshot
                                        .child(USER_PROFILE_AVATAR).getValue();
                                message.setName(name);
                                message.setAvatarRef(avatarRef);
                                mChatModel.onChatMessageNewData(message);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDbReference.child(PATH_CHAT).limitToLast(FETCHED_CHAT_MESSAGES_MIN_COUNT_LIMIT)
                .addChildEventListener(mChatMessagesListener);

    }

    //Called every time users scrolls to the end of the list and swipes up, if there are more messages
    public void fetchOlderChatMessages(final ChatUpdateReceiver receiver) {
        isFirstChatMessageAfterFetch = true;
        mDbReference.child(PATH_CHAT).orderByKey().endAt(mFirstChatMsgKeyAfterFetch)
                .limitToLast(mMessagesCountLimit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot parentSnapshot) {
                        LinkedList<ChatMessage> olderMessages = new LinkedList<>();
                        for (DataSnapshot dataSnapshot : parentSnapshot.getChildren()) {
                            String uid = (String) dataSnapshot.child(CHAT_ID).getValue();

                            String messageId = dataSnapshot.getKey();
                            if (isFirstChatMessageAfterFetch && parentSnapshot.getChildrenCount()
                                    >= mMessagesCountLimit) {
                                mFirstChatMsgKeyAfterFetch = messageId;
                                isFirstChatMessageAfterFetch = false;
                                continue;
                            }

                            //Distance filter is on
                            if (mCloseDistance > 0) {
                                if (mCurrentUserLocation == null) {
                                    return;
                                }

                                if (!DistanceUtil.isClose(mCurrentUserLocation,
                                        mUsersLocation.get(uid),
                                        mCloseDistance)) {
                                    continue;
                                }
                            }

                            String text = (String) dataSnapshot.child(CHAT_TEXT).getValue();
                            long sendTime = (long) dataSnapshot.child(CHAT_SEND_TIME).getValue();
                            Number lat = (Number) dataSnapshot
                                    .child(PATH_LOCATION).child(CHAT_LATITUDE).getValue();
                            Number lng = (Number) dataSnapshot
                                    .child(PATH_LOCATION).child(CHAT_LONGITUDE).getValue();

                            final ChatMessage message =
                                    new ChatMessage(uid, convertUnixTimeToDate(sendTime));
                            if (text != null) {
                                message.setText(text);
                            } else if (lat != null && lng != null) {
                                LatLng latLng = new LatLng(lat.doubleValue(), lng.doubleValue());
                                message.setLocation(latLng);
                            } else {
                                continue;
                            }

                            if (message.getUid().equals(getCurrentUser().getUid())) {
                                message.setCurrentUserMsg(true);
                            }

                            if (isOlderMessagesFirstIteration) {
                                mOlderMessages.add(message);
                            } else {
                                olderMessages.addFirst(message);
                            }

                            mDbReference.child(PATH_USERS).child(uid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String name = (String) dataSnapshot
                                                    .child(USER_PROFILE_NAME).getValue();
                                            String avatarRef = (String) dataSnapshot
                                                    .child(USER_PROFILE_AVATAR).getValue();
                                            message.setName(name);
                                            message.setAvatarRef(avatarRef);
                                            mChatModel.onChatMessageNewData(message);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        }

                        for (ChatMessage message : olderMessages) {
                            mOlderMessages.addFirst(message);
                        }

                        if (parentSnapshot.getChildrenCount() < mMessagesCountLimit) {
                            receiver.onLastMessage();
                            onOlderChatMessagesReady(receiver);
                            return;
                        }

                        if (mOlderMessages.size() < SHOWN_MESSAGES_MIN_COUNT_LIMIT) {
                            fetchOlderChatMessages(receiver);
                            isOlderMessagesFirstIteration = false;
                            return;
                        }

                        onOlderChatMessagesReady(receiver);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void onOlderChatMessagesReady(ChatUpdateReceiver chatModel) {
        Collections.reverse(mOlderMessages);
        chatModel.onOlderChatMessages(mOlderMessages, mOlderMessages.size());
        mOlderMessages.clear();
        isOlderMessagesFirstIteration = true;
    }

    private String convertUnixTimeToDate(long unixTime) {
        Date date = new Date(unixTime);
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.UK)
                .format(date);
    }

    public void unregisterChatMessagesListener() {
        if (mChatMessagesListener != null && getCurrentUser() != null) {
            DatabaseReference myRef = mDbReference.child(PATH_CHAT);
            myRef.removeEventListener(mChatMessagesListener);
            isFirstNewChatMessageAfterFetch = true;
            mMessagesCountLimit = 0;
        }
    }

    public void getCurrentUserLocation(final UsersLocationReceiver receiver) {
        if (mCurrentUserLocation != null) {
            receiver.onCurrentUserLocationReady(mCurrentUserLocation);
            return;
        }
        mDbReference.child("location").child(getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Number lat = (Number) dataSnapshot.child(PATH_LOCATION_LAT).getValue();
                        Number lng = (Number) dataSnapshot.child(PATH_LOCATION_LNG).getValue();
                        mCurrentUserLocation = new LatLng(lat.doubleValue(), lng.doubleValue());
                        receiver.onCurrentUserLocationReady(mCurrentUserLocation);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void sendChatMessage(String message) {
        mDbReference.child(PATH_CHAT).push()
                .setValue(new ChatMessageSendable(getCurrentUser().getUid(),
                        message,
                        ServerValue.TIMESTAMP));
    }

    public void sendChatMessage(LatLng latLng) {
        mDbReference.child(PATH_CHAT).push()
                .setValue(new ChatMessageSendable(getCurrentUser().getUid(),
                        latLng,
                        ServerValue.TIMESTAMP));
    }

    //Send friend request
    public void sendFriendRequest(String userId) {
        DatabaseReference ref = mDbReference.child(PATH_USERS)
                .child(userId).child(USER_PROFILE_FRIEND_LIST);
        ref.child(getCurrentUser().getUid()).setValue(RELATION_PENDING);
    }


    //get current user from database
    public void getCurrentUserModel() {
        //get user name

        DatabaseReference ref = mDbReference.child(PATH_USERS).child(getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                MyProfileFirebase profileFirebase = dataSnapshot.getValue(MyProfileFirebase.class);
                EventBus.getDefault().post(new CurrentUserProfileReadyEvent(profileFirebase));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    //get user from database by userId
    public void getUserModel(final String userId) {
        //get user name
        DatabaseReference ref = mDbReference.child(PATH_USERS).child(userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UsersProfileFirebase usersProfileFirebase =
                        dataSnapshot.getValue(UsersProfileFirebase.class);
                usersProfileFirebase.setId(userId);
                EventBus.getDefault().post(new OnlineUserProfileReadyEvent(usersProfileFirebase));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void saveMyProfile(MyProfileFirebase profile) {
        DatabaseReference ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_ABOUTME);
        ref.setValue(profile.aboutMe);

        ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_AVATAR);
        ref.setValue(profile.avatar);

        ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_EMAIL);
        ref.setValue(profile.email);

        ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_MOTORCYCLE);
        ref.setValue(profile.motorcycle);

        ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_NAME);
        ref.setValue(profile.name);

        ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_NICK);
        ref.setValue(profile.nickName);

    }

    public void setCurrentUserAvatar(@NonNull String avatarUrl) {
        DatabaseReference ref = mDbReference.child(PATH_USERS)
                .child(getCurrentUser().getUid()).child(USER_PROFILE_AVATAR);
        ref.setValue(avatarUrl);
    }

    // TODO: 28.03.2017 must do before release
    public void sendSosMessage() {
        DatabaseReference ref = mDbReference.child(PATH_SOS).child(getCurrentUser().getUid());
        MainServiceSosModel sosModel = new MainServiceSosModel();
        sosModel.setUserId(getCurrentUser().getUid());
        sosModel.setUserName(getCurrentUser().getDisplayName());
        sosModel.setDescription("Test description");
        sosModel.setTime(String.valueOf(ServerValue.TIMESTAMP));
        sosModel.setLat("24.3242");
        sosModel.setLng("43.234");
        ref.setValue(sosModel);

    }

    public interface AuthLoadingListener {
        void onLoadFinished();
    }

    public interface UsersUpdateReceiver {
        void onUserAdded(User user);

        void onUserChanged(User user);

        void onUserDeleted(User user);

        void onUsersAdded(List<User> users);

        boolean hasUser(String uid, String relation);
    }

    public interface ChatUpdateReceiver {
        void onNewChatMessage(ChatMessage message);

        void onOlderChatMessages(List<ChatMessage> olderMessages, int lastPos);

        void onChatMessageNewData(ChatMessage message);

        void onLastMessage();

        void onNoCurrentUserLocation();
    }

    public interface UsersLocationReceiver {
        void onCurrentUserLocationReady(LatLng latLng);
    }
}
