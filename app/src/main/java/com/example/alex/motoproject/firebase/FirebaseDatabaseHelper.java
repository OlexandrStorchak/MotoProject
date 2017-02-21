package com.example.alex.motoproject.firebase;

import android.location.Location;

import com.example.alex.motoproject.events.FriendDataReadyEvent;
import com.example.alex.motoproject.events.MapMarkerEvent;
import com.example.alex.motoproject.screenChat.ChatMessage;
import com.example.alex.motoproject.screenChat.ChatMessageSendable;
import com.example.alex.motoproject.screenChat.ChatModel;
import com.example.alex.motoproject.screenOnlineUsers.OnlineUsersModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FirebaseDatabaseHelper {
    private static final int CHAT_MESSAGES_COUNT_LIMIT = 31;
    private final HashMap<String, OnlineUsersModel> mOnlineUserHashMap = new HashMap<>();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDbReference = mDatabase.getReference();
    private ChildEventListener mOnlineUsersLocationListener;
    private ChildEventListener mOnlineUsersDataListener;
    private ChildEventListener mChatMessagesListener;
    private DatabaseReference mOnlineUsersRef;

    private HashMap<DatabaseReference, ValueEventListener> mLocationListeners = new HashMap<>();
    private HashMap<DatabaseReference, ValueEventListener> mUsersDataListeners = new HashMap<>();

    private String mFirstChatMsgKeyAfterFetch;
    //    private int mChatMessagesToBeLoaded = 10;
    private boolean isFirstChatMessageAfterFetch;
    private boolean isFirstNewChatMessageAfterFetch = true;

    public FirebaseDatabaseHelper() {

    }

    public HashMap<String, OnlineUsersModel> getOnlineUserHashMap() {
        return mOnlineUserHashMap;
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
        final DatabaseReference currentUserRef = mDbReference.child("users").child(uid);
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { //Required data already exists
                    return;
                }
                //No data found by the reference, add new data
                currentUserRef.child("email").setValue(email);
                currentUserRef.child("name").setValue(name);
                currentUserRef.child("avatar").setValue(avatar);
                currentUserRef.child("friendsRequest").child("userId").setValue("1");
                currentUserRef.child("friendsRequest").child("time").setValue("1");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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
                if (dataSnapshot.getKey().equals(getCurrentUser().getUid())) {
                    return;
                }
                Number lat = (Number) dataSnapshot.child("lat").getValue();
                Number lng = (Number) dataSnapshot.child("lng").getValue();
                if (lat == null || lng == null) {
                    return;
                }
                final String uid = dataSnapshot.getKey();
                final LatLng latLng = new LatLng(lat.doubleValue(), lng.doubleValue());
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

    public void registerOnlineUsersListener() {
        // Read from the mDatabase
        DatabaseReference myRef = mDbReference.child("onlineUsers");
        mOnlineUsersDataListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                postUserDataReadyEvent(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                postUserDataReadyEvent(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                postUserDataDeletedEvent(dataSnapshot);
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

        if (!mUsersDataListeners.isEmpty()) {
            for (Map.Entry<DatabaseReference, ValueEventListener> entry :
                    mUsersDataListeners.entrySet()) {
                DatabaseReference ref = entry.getKey();
                ValueEventListener listener = entry.getValue();
                ref.removeEventListener(listener);
            }
        }
    }

    private void postUserDataReadyEvent(DataSnapshot dataSnapshot) {
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
                if (name != null) {
                    if (!mOnlineUserHashMap.containsKey(uid)) {
                        mOnlineUserHashMap.put(uid, new OnlineUsersModel(uid, name, avatar, userStatus));
                    } else {
                        mOnlineUserHashMap.remove(uid);
                        mOnlineUserHashMap.put(uid, new OnlineUsersModel(uid, name, avatar, userStatus));
                    }
                    EventBus.getDefault().post(new FriendDataReadyEvent());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref.addValueEventListener(userDataListener);
        mUsersDataListeners.put(ref, userDataListener);
    }

    private void postUserDataDeletedEvent(DataSnapshot dataSnapshot) {
        final String uid = dataSnapshot.getKey();
        DatabaseReference ref = mDbReference.child("users").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mOnlineUserHashMap.remove(uid);
                EventBus.getDefault().post(new FriendDataReadyEvent());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void registerChatMessagesListener(ChatModel receiver) {
        final ChatUpdateReceiver chatModel = receiver;
        mChatMessagesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (isFirstNewChatMessageAfterFetch) {
                    mFirstChatMsgKeyAfterFetch = dataSnapshot.getKey();
                    isFirstNewChatMessageAfterFetch = false;
                    return;
                }
                String uid = (String) dataSnapshot.child("uid").getValue();
                String text = (String) dataSnapshot.child("text").getValue();
                final ChatMessage message = new ChatMessage(uid, text);
                chatModel.onNewChatMessage(message);
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
                                chatModel.onChatMessageNewData(message);
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
        mDbReference.child("chat").limitToLast(CHAT_MESSAGES_COUNT_LIMIT)
                .addChildEventListener(mChatMessagesListener);

    }

    //Called every time users scrolls to the end of the list and swipes up if there are more messages
    public void fetchOlderChatMessages(ChatModel receiver) {
        isFirstChatMessageAfterFetch = true;
        final ChatUpdateReceiver chatModel = receiver;
        mDbReference.child("chat").orderByKey().endAt(mFirstChatMsgKeyAfterFetch)
                .limitToLast(CHAT_MESSAGES_COUNT_LIMIT)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot parentSnapshot) {
                        List<ChatMessage> olderMessages = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : parentSnapshot.getChildren()) {
                            String messageId = dataSnapshot.getKey();
                            if (isFirstChatMessageAfterFetch && parentSnapshot.getChildrenCount()
                                    >= CHAT_MESSAGES_COUNT_LIMIT) {
                                mFirstChatMsgKeyAfterFetch = messageId;
                                isFirstChatMessageAfterFetch = false;
                            } else {
                                String uid = (String) dataSnapshot.child("uid").getValue();
                                String text = (String) dataSnapshot.child("text").getValue();
                                final ChatMessage message = new ChatMessage(uid, text);
                                olderMessages.add(message);
                                if (message.getUid().equals(getCurrentUser().getUid())) {
                                    message.setCurrentUserMsg(true);
                                }
                                mDbReference.child("users").child(uid)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String name = (String) dataSnapshot.child("name").getValue();
                                                String avatarRef = (String) dataSnapshot.child("avatar").getValue();
                                                message.setName(name);
                                                message.setAvatarRef(avatarRef);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                            }

                        }
                        if (parentSnapshot.getChildrenCount()
                                < CHAT_MESSAGES_COUNT_LIMIT) {
                            chatModel.onLastMessages();
                        }
                        onOlderChatMessagesReady(olderMessages, chatModel);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void onOlderChatMessagesReady(List<ChatMessage> olderMessages,
                                          ChatUpdateReceiver chatModel) {
        Collections.reverse(olderMessages);
        chatModel.onOlderChatMessages(olderMessages, olderMessages.size());
        olderMessages.clear();
    }

    public void unregisterChatMessagesListener() {
        if (mChatMessagesListener != null && getCurrentUser() != null) {
            DatabaseReference myRef = mDbReference.child("chat");
            myRef.removeEventListener(mChatMessagesListener);
        }
    }

    public void sendChatMessage(String message) {
        mDbReference.child("chat").push()
                .setValue(new ChatMessageSendable(getCurrentUser().getUid(), message));
    }

    public interface ChatUpdateReceiver {
        void onNewChatMessage(ChatMessage message);

        void onOlderChatMessages(List<ChatMessage> olderMessages, int lastPos);

        void onChatMessageNewData(ChatMessage message);

        void onLastMessages();
    }
}
