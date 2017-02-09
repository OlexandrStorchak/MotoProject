package com.example.alex.motoproject.firebase;

import android.util.Log;

import com.example.alex.motoproject.screenOnlineUsers.UsersOnline;
import com.example.alex.motoproject.screenOnlineUsers.UsersOnlineAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FirebaseDatabaseHelper {
    private static final String TAG = "log2";
    private final List<UsersOnline> listModels = new ArrayList<>();
    private UsersOnlineAdapter adapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();


    public FirebaseDatabaseHelper() {

    }

    public void createDatabase(final String userId, final String email, final String name) {

        DatabaseReference myRef = database.getReference().child("users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userId)){
                    Log.d(TAG, "createDatabase: NO CREATE");
                } else {

                    Log.d(TAG, "createDatabase: " + userId);

                    //Set User name in database table "USERS"
                    DatabaseReference myRef = database.getReference().child("users")
                            .child(userId).child("name");
                    myRef.setValue(name);

                    //Set User email in database table "USERS"
                    myRef = database.getReference().child("users")
                            .child(userId).child("email");
                    myRef.setValue(email);

                    //Set User email in database table "USERS"
                    myRef.getDatabase().getReference().child("users")
                            .child(userId)
                            .child("friendsRequest").child("userId").setValue("1");
                    myRef.getDatabase().getReference().child("users")
                            .child(userId)
                            .child("friendsRequest").child("time").setValue("1");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addToOnline(String userId, String email, String avatar) {
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId).child("email");
        myRef.setValue(email);
        myRef = database.getReference().child("onlineUsers").child(userId).child("avatar");
        myRef.setValue(avatar);
        myRef = database.getReference().child("onlineUsers").child(userId).child("status");
        myRef.setValue("public");
    }

    public void removeFromOnline(String userId) {
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId);
        myRef.removeValue();
    }

    public void updateOnlineUserLocation(double lat, double lon) {
        String uid = getCurrentUser().getUid();
        Log.d(TAG, "updateOnlineUserLocation: " + uid);
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(uid).child("lat");
        myRef.setValue(lat);
        myRef = database.getReference().child("onlineUsers").child(uid).child("lon");
        myRef.setValue(lon);
    }

    public List<UsersOnline> getAllOnlineUsers() {
        // Read from the database

        DatabaseReference myRef = database.getReference().child("onlineUsers");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listModels.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UsersOnline friend = postSnapshot.getValue(UsersOnline.class);
                    Log.d(TAG, "onDataChange: " + friend.getEmail() + " - ");
                    listModels.add(friend);
                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return listModels;
    }

    public void setAdapter(UsersOnlineAdapter adapter) {
        this.adapter = adapter;
    }

    private FirebaseUser getCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            return auth.getCurrentUser();
        }
        throw new RuntimeException("Current user is null");
    }
}
