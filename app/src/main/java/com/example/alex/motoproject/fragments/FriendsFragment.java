package com.example.alex.motoproject.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.R;

public class FriendsFragment extends Fragment {


private static FriendsFragment friendsFragmentInstance;


    public FriendsFragment() {
        // Required empty public constructor
    }
    public static FriendsFragment getInstance(){
        if (friendsFragmentInstance==null){
            friendsFragmentInstance = new FriendsFragment();
        }
        return friendsFragmentInstance;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.navigation_friends_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
}
