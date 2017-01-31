package com.example.alex.motoproject.firebase;

import android.util.Log;

import com.example.alex.motoproject.models.FriendsListModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



public class FirebaseDatabaseHelper {
    private static final String TAG = "log";




    public FirebaseDatabaseHelper() {

    }
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private final List<FriendsListModel> listModels = new ArrayList<>();

    private ValueEventListener onlineListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            listModels.clear();

            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                FriendsListModel friend = postSnapshot.getValue(FriendsListModel.class);
                Log.d(TAG, "onDataChange: " +friend.getEmail() + " - ");
                listModels.add(friend);

            }


        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };




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

    public void addToOnline(String userId,String email){
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId).child("email");
        myRef.setValue(email);
    }

    public void removeFromOnline(String userId){
        DatabaseReference myRef = database.getReference().child("onlineUsers").child(userId);
        myRef.removeValue();
    }

    public List<FriendsListModel> getAllOnlineUsers(){
        // Read from the database

        DatabaseReference myRef = database.getReference().child("onlineUsers");
        myRef.addValueEventListener(onlineListener);

        return listModels;
    }






}
