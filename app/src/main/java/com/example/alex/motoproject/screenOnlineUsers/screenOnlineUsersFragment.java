package com.example.alex.motoproject.screenOnlineUsers;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.events.FriendDataReadyEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

public class ScreenOnlineUsersFragment extends Fragment {





    private RecyclerView rv;
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    OnlineUsersAdapter adapter = new OnlineUsersAdapter(null);


    public ScreenOnlineUsersFragment() {

        // Required empty public constructor

    }


    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        mFirebaseDatabaseHelper.unregisterOnlineUsersDataListener();
        mFirebaseDatabaseHelper.getOnlineUserHashMap().clear();
        super.onStop();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        mFirebaseDatabaseHelper.registerOnlineUsersListener();
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        App.getFirebaseDatabaseHelperComponent().inject(this);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users_online, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv = (RecyclerView) view.findViewById(R.id.navigation_friends_list_recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Subscribe
    public void onFriendDataReady(FriendDataReadyEvent event) {
        adapter.setList(mFirebaseDatabaseHelper.getOnlineUserHashMap());
        rv.setAdapter(adapter);


    }


}
