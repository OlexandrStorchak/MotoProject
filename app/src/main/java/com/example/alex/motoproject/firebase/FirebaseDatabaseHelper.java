package com.example.alex.motoproject.firebase;

import android.util.Log;

import com.example.alex.motoproject.adapters.FriendsListAdapter;
import com.example.alex.motoproject.models.usersOnline;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



public class FirebaseDatabaseHelper {
    private static final String TAG = "log";

    private FriendsListAdapter adapter;


    public FirebaseDatabaseHelper() {

    }
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private final List<usersOnline> listModels = new ArrayList<>();



    public void createDatabase(String userId,String email,String name){


        DatabaseReference myRef = database.getReference().child("users").child(userId).child("name");
        myRef.setValue(name);

        myRef = database.getReference().child("users").child(userId).child("email");
        myRef.setValue(email);

        myRef = database.getReference().child("users").child(userId).child("friendsList").child("userID");
        myRef.setValue("2363524");
        myRef = database.getReference().child("users").child(userId).child("friendsList").child("userID").child("name");
        myRef.setValue("User");
        myRef = database.getReference().child("users").child(userId).child("friendsList").child("userID").child("email");
        myRef.setValue("test@best.mail.com");

        myRef = database.getReference().child("users").child(userId).child("friendsRequest").child("userID");
        myRef.setValue("234256");
        myRef = database.getReference().child("users").child(userId).child("friendsRequest").child("userID").child("name");
        myRef.setValue("TestUser");
        myRef = database.getReference().child("users").child(userId).child("friendsRequest").child("userID").child("email");
        myRef.setValue("test@best.mail.com");

    }

    public void addToOnline(String userId, String email, String avatar){
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId).child("email");
        myRef.setValue(email);
        myRef = database.getReference().child("onlineUsers").child(userId).child("avatar");
        myRef.setValue(avatar);
        myRef = database.getReference().child("onlineUsers").child(userId).child("status");
        myRef.setValue("public");
    }

    public void removeFromOnline(String userId){
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId);
        myRef.removeValue();
    }

    public void updateOnlineUserLocation(double lat, double lon, String userId){
        Log.d(TAG, "updateOnlineUserLocation: "+userId);
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId).child("lat");
        myRef.setValue(lat);
        myRef = database.getReference().child("onlineUsers").child(userId).child("lon");
        myRef.setValue(lon);
    }

    public List<usersOnline> getAllOnlineUsers(){
        // Read from the database

        DatabaseReference myRef = database.getReference().child("onlineUsers");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listModels.clear();

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    usersOnline friend = postSnapshot.getValue(usersOnline.class);
                    Log.d(TAG, "onDataChange: " +friend.getEmail() + " - ");
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
    public void setAdapter(FriendsListAdapter adapter){
        this.adapter=adapter;
    }




}
