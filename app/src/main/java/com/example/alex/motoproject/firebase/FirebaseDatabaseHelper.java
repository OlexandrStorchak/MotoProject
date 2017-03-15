package com.example.alex.motoproject.firebase;

import android.location.Location;
import android.util.Log;

import com.example.alex.motoproject.event.CurrentUserProfileReadyEvent;
import com.example.alex.motoproject.event.MapMarkerEvent;
import com.example.alex.motoproject.event.OnlineUserProfileReadyEvent;
import com.example.alex.motoproject.screenChat.ChatMessage;
import com.example.alex.motoproject.screenChat.ChatMessageSendable;
import com.example.alex.motoproject.screenOnlineUsers.User;
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

@Module
public class FirebaseDatabaseHelper {

    private static final String STANDART_AVATAR =
            "https://firebasestorage.googleapis.com/v0/b/profiletests-d3a61.appspot.com/" +
                    "o/ava4.png?alt=media&token=96951c00-fd27-445c-85a6-b636bd0cb9f5";

    private static final int FETCHED_CHAT_MESSAGES_MIN_COUNT_LIMIT = 31; //31
    private static final int SHOWN_MESSAGES_MIN_COUNT_LIMIT =
            FETCHED_CHAT_MESSAGES_MIN_COUNT_LIMIT - 1;
    private int mMessagesCountLimit = 0;

    private ChatUpdateReceiver mChatModel;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDbReference = mDatabase.getReference();
    private ChildEventListener mOnlineUsersLocationListener;
    private ChildEventListener mOnlineUsersDataListener;
    private ChildEventListener mFriendsListener;
    private ChildEventListener mChatMessagesListener;
    private DatabaseReference mOnlineUsersRef;

    private HashMap<String, String> mOnlineUserStatusHashMap = new HashMap<>();
    private HashMap<String, LatLng> mUsersLocation = new HashMap<>();
    private HashMap<DatabaseReference, ValueEventListener> mLocationListeners = new HashMap<>();

    private LinkedList<ChatMessage> mOlderMessages = new LinkedList<>();

    private String mFirstChatMsgKeyAfterFetch;
    private boolean isFirstChatMessageAfterFetch;
    private boolean isFirstNewChatMessageAfterFetch = true;

    private int mReceivedUsersCount;

    private LatLng mCurrentUserLocation;
    private int mCloseDistance = 0;

    private boolean isOlderMessagesFirstIteration = true;

    public FirebaseDatabaseHelper() {

    }

    public FirebaseUser getCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser();
    }

    public void setUserOnline(String status) {
        String uid = getCurrentUser().getUid();
        DatabaseReference onlineUsers = mDbReference.child("onlineUsers").child(uid);
        onlineUsers.setValue(status);
    }

    public void setUserOffline() {
        String uid = getCurrentUser().getUid();
        DatabaseReference onlineUsers = mDbReference.child("onlineUsers").child(uid);
        onlineUsers.removeValue();
    }

    public void updateUserLocation(Location location) {
        String uid = getCurrentUser().getUid();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        DatabaseReference myRef = mDbReference.child("location").child(uid);
        myRef.child("lat").setValue(lat);
        myRef.child("lng").setValue(lng);
    }

    //Called when user auth state changes. Adds required user data to Firebase
    public void addUserToFirebase(
            final String uid, final String email, final String name, final String avatar) {

        final String ava;
        final String nameDef;
        if (avatar.equals("null")) {
            ava = STANDART_AVATAR;
        } else {
            ava = avatar;
        }
        if (name == null) {
            nameDef = getCurrentUser().getEmail();
        } else {
            nameDef = name;
        }
        final DatabaseReference currentUserRef = mDbReference.child("users").child(uid);


        ValueEventListener userProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //Required data already exists
                    return;
                }
                //No data found by the reference, add new data
                currentUserRef.child("email").setValue(email);
                currentUserRef.child("name").setValue(nameDef);
                currentUserRef.child("avatar").setValue(ava);
                currentUserRef.child("id").setValue(uid);
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
        mOnlineUsersRef = mDbReference.child("onlineUsers");
        mOnlineUsersLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postChangeMarkerEvent(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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
            mOnlineUsersRef = mDbReference.child("onlineUsers");
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
        DatabaseReference ref = mDbReference.child("location");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot entry : dataSnapshot.getChildren()) {
                    String uid = entry.getKey();
                    Number lat = (Number) entry.child("lat").getValue();
                    Number lng = (Number) entry.child("lng").getValue();
                    if (lat == null || lng == null) {
                        return;
                    }
                    LatLng location = new LatLng(lat.doubleValue(), lng.doubleValue());
                    mUsersLocation.put(uid, location);
                }
//                receiver.onUsersLocationsReady();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void postChangeMarkerEvent(DataSnapshot dataSnapshot) {
        if (!(dataSnapshot.getValue() instanceof String)) {
            return;
        }
        String status = (String) dataSnapshot.getValue();
        if (!status.equals("public")) {
            return;
        }
        DatabaseReference location =
                mDbReference.child("location").child(dataSnapshot.getKey());
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Number lat = (Number) dataSnapshot.child("lat").getValue();
                Number lng = (Number) dataSnapshot.child("lng").getValue();
                if (lat == null || lng == null) {
                    return;
                }

                final String uid = dataSnapshot.getKey();
                final LatLng latLng = new LatLng(lat.doubleValue(), lng.doubleValue());

                mUsersLocation.put(uid, latLng);

                if (dataSnapshot.getKey().equals(getCurrentUser().getUid())) {
//                    mCurrentUserLocation = new LatLng(lat.doubleValue(), lng.doubleValue());
                    return;
                }

                DatabaseReference nameRef = mDbReference.child("users").child(uid).child("name");
                nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = (String) dataSnapshot.getValue();
                        DatabaseReference ref = mDbReference.child("users").child(uid).child("avatar");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String avatarRef = (String) dataSnapshot.getValue();
                                EventBus.getDefault().post(new MapMarkerEvent(latLng, uid, name, avatarRef));
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
        };
        location.addValueEventListener(listener);
        mLocationListeners.put(location, listener);
    }

    private void postDeleteMarkerEvent(DataSnapshot dataSnapshot) {
        DatabaseReference location =
                mDbReference.child("location").child(dataSnapshot.getKey());
        location.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getCurrentUser() == null) {
                    return;
                }
                String uid = dataSnapshot.getKey();
                if (!uid.equals(getCurrentUser().getUid())) {
                    EventBus.getDefault().post(new MapMarkerEvent(null, uid, null, null));
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
    public void changeUserRelation(String uid, String relation) {
        DatabaseReference ref = mDbReference.child("users")
                .child(getCurrentUser().getUid()).child("friendList").child(uid);
        ref.setValue(relation);
    }

    // TODO: 10.03.2017 use automatic saving but listener
    public void registerOnlineUsersGlobalListener() {
        DatabaseReference ref = mDbReference.child("onlineUsers");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mOnlineUserStatusHashMap.put(dataSnapshot.getKey(), (String) dataSnapshot.getValue());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mOnlineUserStatusHashMap.put(dataSnapshot.getKey(), (String) dataSnapshot.getValue());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mOnlineUserStatusHashMap.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getFriends(final UsersUpdateReceiver receiver) {
        DatabaseReference ref = mDbReference.child("users")
                .child(getCurrentUser().getUid()).child("friendList");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<User> friends = new ArrayList<>();
                final int childrenCount = (int) dataSnapshot.getChildrenCount();
                mReceivedUsersCount = 0;
                for (DataSnapshot entry : dataSnapshot.getChildren()) {
                    final String uid = entry.getKey();
                    final String relation = (String) entry.getValue();
                    final String userStatus = mOnlineUserStatusHashMap.get(uid);
                    DatabaseReference ref = mDbReference.child("users").child(uid);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = (String) dataSnapshot.child("name").getValue();
                            String avatar = (String) dataSnapshot.child("avatar").getValue();
                            User user = new User(uid, name, avatar, userStatus, relation);
                            friends.add(user);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void registerFriendsListener(final UsersUpdateReceiver receiver) {
        DatabaseReference ref = mDbReference.child("users")
                .child(getCurrentUser().getUid()).child("friendList");
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
        DatabaseReference ref = mDbReference.child("users")
                .child(getCurrentUser().getUid()).child("friendList");
        ref.removeEventListener(mFriendsListener);
    }

    private void onFriendAdded(DataSnapshot dataSnapshot, final UsersUpdateReceiver receiver) {
        final String uid = dataSnapshot.getKey();
        final String relation = (String) dataSnapshot.getValue();
        final String userStatus = mOnlineUserStatusHashMap.get(uid);
        DatabaseReference ref = mDbReference.child("users").child(uid);
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String avatar = (String) dataSnapshot.child("avatar").getValue();
                User user = new User(uid, name, avatar, userStatus, relation);
                receiver.onUserAdded(user);
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
        final String userStatus = mOnlineUserStatusHashMap.get(uid);
        DatabaseReference ref = mDbReference.child("users").child(uid);
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String avatar = (String) dataSnapshot.child("avatar").getValue();
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
        receiver.onUserDeleted(new User(uid));
    }

    public void registerOnlineUsersListener(final UsersUpdateReceiver receiver) {
        // Read from the mDatabase
        DatabaseReference myRef = mDbReference.child("onlineUsers");
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
            DatabaseReference myRef = mDbReference.child("onlineUsers");
            myRef.removeEventListener(mOnlineUsersDataListener);
        }
    }

    public void getOnlineUsers(final UsersUpdateReceiver receiver) {
        DatabaseReference ref = mDbReference.child("onlineUsers");

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
                    DatabaseReference ref = mDbReference.child("users").child(uid);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String name = (String) dataSnapshot.child("name").getValue();
                            String avatar = (String) dataSnapshot.child("avatar").getValue();
                            User user = new User(uid, name, avatar, userStatus, null);
                            onlineUsers.add(user);
                            mReceivedUsersCount++;
                            // TODO: 10.03.2017 why does it work only with + 1?
                            if (mReceivedUsersCount + 1 == childrenCount) {
                                receiver.onUsersAdded(onlineUsers);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
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
        DatabaseReference ref = mDbReference.child("users").child(uid);
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String avatar = (String) dataSnapshot.child("avatar").getValue();
                User user = new User(uid, name, avatar, userStatus, null);
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
        DatabaseReference ref = mDbReference.child("users").child(uid);
        ValueEventListener userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String avatar = (String) dataSnapshot.child("avatar").getValue();
                User user = new User(uid, name, avatar, userStatus, null);
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
        receiver.onUserDeleted(new User(uid));
    }

    /**
     * Chat
     */

    public void setCloseDistance(int closeDistance) {
        mCloseDistance = closeDistance;
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
                String uid = (String) dataSnapshot.child("uid").getValue();
                String text = (String) dataSnapshot.child("text").getValue();
                Long sendTime = (Long) dataSnapshot.child("sendTime").getValue();
                Number lat = (Number) dataSnapshot.child("location").child("latitude").getValue();
                Number lng = (Number) dataSnapshot.child("location").child("longitude").getValue();

                mMessagesCountLimit++;

                if (mCloseDistance > 0) {
//                    if (mCurrentUserLocation == null) {
//                        receiver.onNoCurrentUserLocation();
//                        mCloseDistance = 0;
//                        return;
//                    }

                    if (!DistanceUtil.isClose(mCurrentUserLocation,
                            mUsersLocation.get(uid),
                            mCloseDistance)) {
                        return;
                    }
                }

                if (isFirstNewChatMessageAfterFetch) {
                    mFirstChatMsgKeyAfterFetch = dataSnapshot.getKey();
                    isFirstNewChatMessageAfterFetch = false;
                    return;
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

                mDbReference.child("users").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String name = (String) dataSnapshot.child("name").getValue();
                                String avatarRef = (String) dataSnapshot.child("avatar").getValue();
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
        mDbReference.child("chat").limitToLast(FETCHED_CHAT_MESSAGES_MIN_COUNT_LIMIT)
                .addChildEventListener(mChatMessagesListener);

    }

    //Called every time users scrolls to the end of the list and swipes up, if there are more messages
    public void fetchOlderChatMessages(final ChatUpdateReceiver receiver) {
        isFirstChatMessageAfterFetch = true;
        mDbReference.child("chat").orderByKey().endAt(mFirstChatMsgKeyAfterFetch)
                .limitToLast(mMessagesCountLimit)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot parentSnapshot) {
                        LinkedList<ChatMessage> olderMessages = new LinkedList<>();
                        for (DataSnapshot dataSnapshot : parentSnapshot.getChildren()) {
                            String uid = (String) dataSnapshot.child("uid").getValue();

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
//                                    mCurrentUserLocation = mUsersLocation.get(getCurrentUser().getUid());
                                    return;
                                }

                                if (!DistanceUtil.isClose(mCurrentUserLocation,
                                        mUsersLocation.get(uid),
                                        mCloseDistance)) {
                                    continue;
                                }
                            }

                            String text = (String) dataSnapshot.child("text").getValue();
                            long sendTime = (long) dataSnapshot.child("sendTime").getValue();
                            Number lat = (Number) dataSnapshot.child("location").child("latitude").getValue();
                            Number lng = (Number) dataSnapshot.child("location").child("longitude").getValue();

                            final ChatMessage message = new ChatMessage(uid, convertUnixTimeToDate(sendTime));
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

                            mDbReference.child("users").child(uid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String name = (String) dataSnapshot.child("name").getValue();
                                            String avatarRef = (String) dataSnapshot.child("avatar").getValue();
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
            DatabaseReference myRef = mDbReference.child("chat");
            myRef.removeEventListener(mChatMessagesListener);
            isFirstNewChatMessageAfterFetch = true;
            mMessagesCountLimit = 0;
        }
    }

    public void getCurrentUserLocation(final UsersLocationReceiver receiver) {
        if (mCurrentUserLocation != null) {
            return;
        }
        mDbReference.child("location").child(getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Number lat = (Number) dataSnapshot.child("lat").getValue();
                        Number lng = (Number) dataSnapshot.child("lng").getValue();
                        mCurrentUserLocation = new LatLng(lat.doubleValue(), lng.doubleValue());
                        receiver.onCurrentUserLocationReady(mCurrentUserLocation);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void sendChatMessage(String message) {
        mDbReference.child("chat").push()
                .setValue(new ChatMessageSendable(getCurrentUser().getUid(),
                        message,
                        ServerValue.TIMESTAMP));
    }

    public void sendChatMessage(LatLng latLng) {
        mDbReference.child("chat").push()
                .setValue(new ChatMessageSendable(getCurrentUser().getUid(),
                        latLng,
                        ServerValue.TIMESTAMP));
    }

    //Send friend request
    public void sendFriendRequest(String userId) {
        final DatabaseReference ref = mDbReference.child("users").child(userId)
                .child("friendsRequest").child(getCurrentUser().getUid());
        Log.d("log", "sendFriendRequest: " + userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Request already exists
                    Log.d("log", "onDataChange: NO ADD");
                    return;
                }
                ref.setValue("timeStamp");
                Log.d("log", "onDataChange: ADD USER REQUEST");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("log", "onCancelled: ");
            }
        });
    }

    //Friend request table listener
    public void getFriendRequest() {
        DatabaseReference ref = mDbReference.child("users").child(getCurrentUser().getUid())
                .child("friendsRequest");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("log", "onChildAdded: " + dataSnapshot.getKey());

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
        });
    }

    //get current user from database
    public void getCurrentUserModel() {
        //get user name

        DatabaseReference ref = mDbReference.child("users").child(getCurrentUser().getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

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
    public void getUserModel(String userId) {
        //get user name

        DatabaseReference ref = mDbReference.child("users").child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UsersProfileFirebase usersProfileFirebase = dataSnapshot.getValue(UsersProfileFirebase.class);
                EventBus.getDefault().post(new OnlineUserProfileReadyEvent(usersProfileFirebase));


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    public interface UsersUpdateReceiver {
        void onUserAdded(User user);

        void onUserChanged(User user);

        void onUserDeleted(User user);

        void onUsersAdded(List<User> users);
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
