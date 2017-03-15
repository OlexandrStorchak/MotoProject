package com.example.alex.motoproject.screenProfile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OnlineUserProfileReadyEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.UsersProfileFirebase;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.function.ToDoubleBiFunction;


public class ScreenUserProfileFragment extends Fragment {

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;


    LinearLayout buttons;
    private TextView name;
    private TextView motorcycle;
    private TextView nickName;
    private TextView email;
    private ImageView avatar;
    private ImageView removeFriend;
    private ImageView addToFriend;


    public ScreenUserProfileFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        App.getCoreComponent().inject(this);
        EventBus.getDefault().register(this);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_screen_user_profile, container, false);

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addToFriend = (ImageView) view.findViewById(R.id.profile_add_friend);
        removeFriend = (ImageView) view.findViewById(R.id.profile_remove_from_friends);

        name = (TextView) view.findViewById(R.id.profile_user_name);
        motorcycle = (TextView) view.findViewById(R.id.profile_user_motorcycle);
        nickName = (TextView) view.findViewById(R.id.profile_user_status);
        email = (TextView) view.findViewById(R.id.profile_user_email);
        avatar = (ImageView) view.findViewById(R.id.profile_user_avatar);
        buttons = (LinearLayout) view.findViewById(R.id.profile_user_buttons);


    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().setTitle("MotoProject");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onOnlineUserProfileReady(OnlineUserProfileReadyEvent event) {

        final UsersProfileFirebase user = event.getUsersProfileFirebase();

        getActivity().setTitle(user.getName());
        name.setText(user.getName());
        nickName.setText(user.getNickName());
        email.setText(user.getEmail());
        motorcycle.setText(user.getMotorcycle());
        Picasso.with(getContext())
                .load(user.getAvatar())
                .resize(avatar.getMaxWidth(), avatar.getMaxHeight())
                .centerCrop()
                .into(avatar);

        //TODO add or remove person from friendList
        //If friend allready added
//        if (friendAlreadyAdded){
//            removeFriend.setVisibility(View.GONE);
//            addToFriend.setVisibility(View.VISIBLE);
//        }

        addToFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),
                        "add to friend " + user.getName(),
                        Toast.LENGTH_SHORT).show();
                removeFriend.setVisibility(View.VISIBLE);
                addToFriend.setVisibility(View.GONE);
                mFirebaseDatabaseHelper.sendFriendRequest(user.getId());

            }
        });
        removeFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),
                        "send message " + user.getName(),
                        Toast.LENGTH_SHORT).show();
                removeFriend.setVisibility(View.GONE);
                addToFriend.setVisibility(View.VISIBLE);
            }
        });

    }

}
