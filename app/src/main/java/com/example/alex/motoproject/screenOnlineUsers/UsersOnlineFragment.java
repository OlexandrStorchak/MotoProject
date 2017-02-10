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
import com.example.alex.motoproject.events.FriendDataReadyEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.mainActivity.FragmentReplace;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.example.alex.motoproject.mainActivity.FragmentContract.FRAGMENT_MAP;

public class UsersOnlineFragment extends Fragment {

    public static UsersOnlineFragment usersOnlineFragmentInstance;
    UsersOnlineAdapter adapter = new UsersOnlineAdapter(null);
    private FirebaseDatabaseHelper databaseHelper = new FirebaseDatabaseHelper();
    public UsersOnlineFragment() {
        // Required empty public constructor
    }

    public static UsersOnlineFragment getInstance() {
        if (usersOnlineFragmentInstance == null) {
            usersOnlineFragmentInstance = new UsersOnlineFragment();
        }
        return usersOnlineFragmentInstance;
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        databaseHelper.unregisterOnlineUsersListener();
        super.onStop();
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        databaseHelper.registerOnlineUsersListener();
        super.onStart();
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
    }

    @Subscribe
    public void onFriendDataReady(FriendDataReadyEvent event) {
        RecyclerView rv = (RecyclerView) getActivity()
                .findViewById(R.id.navigation_friends_list_recycler);
        adapter.notifyDataSetChanged();
        if (rv.getAdapter() == null) {
            setOnlineUsersAdapter();
        }
    }

    private void setOnlineUsersAdapter() {
        adapter.setList(databaseHelper.getOnlineUserList());

        RecyclerView rv = (RecyclerView) getActivity().findViewById(R.id.navigation_friends_list_recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
    }

    public void showMapFragment() {
        new FragmentReplace(getFragmentManager()).replaceFragment(FRAGMENT_MAP);
    }
}
