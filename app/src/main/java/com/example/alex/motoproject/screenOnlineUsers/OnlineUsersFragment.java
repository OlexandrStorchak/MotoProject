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
import com.example.alex.motoproject.mainActivity.ManageFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_MAP;

public class OnlineUsersFragment extends Fragment {

    public static OnlineUsersFragment usersOnlineFragmentInstance;
    @Inject
    FirebaseDatabaseHelper databaseHelper;
    OnlineUsersAdapter adapter = new OnlineUsersAdapter(null);
    public OnlineUsersFragment() {
        // Required empty public constructor
    }

    public static OnlineUsersFragment getInstance() {
        if (usersOnlineFragmentInstance == null) {
            usersOnlineFragmentInstance = new OnlineUsersFragment();
        }
        return usersOnlineFragmentInstance;
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        databaseHelper.unregisterOnlineUsersDataListener();
        databaseHelper.getOnlineUserHashMap().clear();
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
        App.getFirebaseDatabaseHelperComponent().inject(this);
    }

    @Subscribe
    public void onFriendDataReady(FriendDataReadyEvent event) {
        adapter.setList(databaseHelper.getOnlineUserHashMap());

        RecyclerView rv = (RecyclerView) getActivity().findViewById(R.id.navigation_friends_list_recycler);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
    }

    public void showMapFragment() {
        new ManageFragment(getFragmentManager()).replaceFragment(FRAGMENT_MAP);
    }
}
