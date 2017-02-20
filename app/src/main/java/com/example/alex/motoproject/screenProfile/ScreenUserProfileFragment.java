package com.example.alex.motoproject.screenProfile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.screenOnlineUsers.OnlineUsersModel;
import com.squareup.picasso.Picasso;

import static com.facebook.FacebookSdk.getApplicationContext;


public class ScreenUserProfileFragment extends Fragment {

    OnlineUsersModel onlineUsersModel;
    private Toolbar toolbar;


    public ScreenUserProfileFragment() {
        // Required empty public constructor

    }

    public void setOnlineUsersModel(OnlineUsersModel onlineUsersModel) {
        this.onlineUsersModel = onlineUsersModel;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_screen_user_profile, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView addToFriend = (ImageView) view.findViewById(R.id.profile_add_friend);
        ImageView sendMessage = (ImageView) view.findViewById(R.id.profile_send_message);
        TextView name = (TextView) view.findViewById(R.id.profile_user_name);
        ImageView avatar = (ImageView) view.findViewById(R.id.profile_user_avatar);
        name.setText(onlineUsersModel.getName());




        if (onlineUsersModel.getAvatar() != null) {
            Picasso.with(getApplicationContext())
                    .load(onlineUsersModel.getAvatar())
                    .resize(avatar.getMaxWidth(), avatar.getMaxHeight())
                    .centerCrop()

                    .into(avatar);
        }

        getActivity().setTitle(onlineUsersModel.getName());

        addToFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),
                        "add to friend " + onlineUsersModel.getName(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),
                        "send message " + onlineUsersModel.getName(),
                        Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onStop() {
        super.onStop();
        toolbar=null;
        getActivity().setTitle("MotoProject");

    }


}
