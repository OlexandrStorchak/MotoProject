package com.example.alex.motoproject.screenOnlineUsers;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.screenLogin.LoginController;

public class UsersOnlineFragment extends Fragment {

    public static UsersOnlineFragment usersOnlineFragmentInstance;

    public UsersOnlineFragment() {
        // Required empty public constructor
    }

    private FirebaseDatabaseHelper databaseHelper = new FirebaseDatabaseHelper();
    UsersOnlineAdapter adapter = new UsersOnlineAdapter(null);


    public static UsersOnlineFragment getInstance() {
        if (usersOnlineFragmentInstance == null) {
            usersOnlineFragmentInstance = new UsersOnlineFragment();
        }
        return usersOnlineFragmentInstance;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users_online, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseHelper.setAdapter(adapter);
        adapter.setList(databaseHelper.getAllOnlineUsers());
        adapter.notifyDataSetChanged();
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.navigation_friends_list_recycler);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        rv.setAdapter(adapter);

    }
}
