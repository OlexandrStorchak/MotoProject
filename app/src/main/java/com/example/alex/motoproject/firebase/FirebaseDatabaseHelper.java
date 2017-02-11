package com.example.alex.motoproject.firebase;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.example.alex.motoproject.events.FriendDataReadyEvent;
import com.example.alex.motoproject.events.MapMarkerEvent;
import com.example.alex.motoproject.models.OnlineUser;
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
import java.util.List;


public class FirebaseDatabaseHelper {
    private static final String LOG_TAG = FirebaseDatabaseHelper.class.getSimpleName();
    private final List<OnlineUser> onlineUserList = new ArrayList<>();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private ChildEventListener mOnlineUsersLocationListener;
    private ChildEventListener onlineUsersListener;
    private DatabaseReference mOnlineUsersRef;

    public FirebaseDatabaseHelper() {

    }

    public List<OnlineUser> getOnlineUserList() {
        return onlineUserList;
    }

    public void setUserOnline(String status) {
        String uid = getCurrentUser().getUid();
        DatabaseReference onlineUsers = mDatabase.getReference().child("onlineUsers").child(uid);
        onlineUsers.setValue(status);
    }

    public void setUserOffline() {
        String uid = getCurrentUser().getUid();
        DatabaseReference onlineUsers = mDatabase.getReference().child("onlineUsers").child(uid);
        onlineUsers.removeValue();
    }

    public void updateUserLocation(Location location) {
        String uid = getCurrentUser().getUid();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Log.d(LOG_TAG, "updateUserLocation: " + uid);
        DatabaseReference myRef = mDatabase.getReference().child("location").child(uid);
        myRef.child("lat").setValue(lat);
        myRef.child("lng").setValue(lng);
    }

    //Called when user auth state changes. Adds required user data to Firebase
    public void addUserToFirebase(
            final String uid, final String email, final String name, final Uri avatar) {
        final DatabaseReference currentUserRef = mDatabase.getReference().child("users").child(uid);
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
        mOnlineUsersRef = mDatabase.getReference().child("onlineUsers");
        mOnlineUsersLocationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() instanceof String) {
                    String status = (String) dataSnapshot.getValue();
                    if (!status.equals("public")) {
                        return;
                    }
                }
                DatabaseReference location =
                        mDatabase.getReference().child("location").child(dataSnapshot.getKey());
                location.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.getKey().equals(getCurrentUser().getUid())) {
                            Number lat = (Number) dataSnapshot.child("lat").getValue();
                            Number lng = (Number) dataSnapshot.child("lng").getValue();
                            if (lat != null && lng != null) {
                                String uid = dataSnapshot.getKey();
                                LatLng latLng = new LatLng(lat.doubleValue(), lng.doubleValue());
                                getNameByUid(uid, latLng);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() instanceof String) {
                    String status = (String) dataSnapshot.getValue();
                    if (status.equals("public")) {
                        DatabaseReference location =
                                mDatabase.getReference().child("location").child(dataSnapshot.getKey());
                        location.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.getKey().equals(getCurrentUser().getUid())) {
                                    Double lat = (Double) dataSnapshot.child("lat").getValue();
                                    Double lng = (Double) dataSnapshot.child("lng").getValue();
                                    if (lat != null && lng != null) { // TODO: 07.02.2017 delete if statement
                                        String uid = dataSnapshot.getKey();
                                        LatLng latLng = new LatLng(lat, lng);
                                        getNameByUid(uid, latLng);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        return;
                    }
                }
                DatabaseReference location =
                        mDatabase.getReference().child("location").child(dataSnapshot.getKey());
                location.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
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

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                DatabaseReference location =
                        mDatabase.getReference().child("location").child(dataSnapshot.getKey());
                location.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String uid = dataSnapshot.getKey();
                        if (getCurrentUser() == null) {
                            return;
                        }
                        if (!uid.equals(getCurrentUser().getUid())) {
                            EventBus.getDefault().post(new MapMarkerEvent(null, uid, null, null));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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
            mOnlineUsersRef = mDatabase.getReference().child("onlineUsers");
            mOnlineUsersRef.removeEventListener(mOnlineUsersLocationListener);
        }
    }

    private void getNameByUid(final String uid, final LatLng latLng) {
        DatabaseReference nameRef = mDatabase.getReference().child("users").child(uid).child("name");
        nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String name = (String) dataSnapshot.getValue();
                DatabaseReference ref = mDatabase.getReference().child("users").child(uid).child("avatar");
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

    public void registerOnlineUsersListener() {
        // Read from the mDatabase
        DatabaseReference myRef = mDatabase.getReference().child("onlineUsers");
        onlineUsersListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onlineUserList.clear();
                String uid = dataSnapshot.getKey();
                Object userStatus = dataSnapshot.getValue();
                if (userStatus instanceof String) // TODO: 08.02.2017 remove this line
                    getOnlineUserDataByUid(uid, (String) userStatus);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String uid = dataSnapshot.getKey();
                Object userStatus = dataSnapshot.getValue();
                if (userStatus instanceof String) // TODO: 08.02.2017 remove this line
                    getOnlineUserDataByUid(uid, (String) userStatus);
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
        myRef.addChildEventListener(onlineUsersListener);
    }

    public void unregisterOnlineUsersListener() {
        DatabaseReference myRef = mDatabase.getReference().child("onlineUsers");
        myRef.removeEventListener(onlineUsersListener);
    }

    public FirebaseUser getCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser();
    }

    private void getOnlineUserDataByUid(final String uid, final String userStatus) {
        DatabaseReference ref = mDatabase.getReference().child("users").child(uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("name").getValue();
                String avatar = (String) dataSnapshot.child("avatar").getValue();
                if (name != null) {
                    onlineUserList.add(new OnlineUser(uid, name, avatar, userStatus));
                    EventBus.getDefault().post(new FriendDataReadyEvent());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
