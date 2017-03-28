package com.example.alex.motoproject.screenProfile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.example.alex.motoproject.firebase.Constants;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.UsersProfileFirebase;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import static com.example.alex.motoproject.util.ArgKeys.USER_DATA;

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

    private UsersProfileFirebase mUserData;


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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(USER_DATA, mUserData);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mUserData = savedInstanceState.getParcelable(USER_DATA);
            displayUserData();
        }
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

    private void displayUserData() {



        getActivity().setTitle(mUserData.getName());
        name.setText(mUserData.getName());
        nickName.setText(mUserData.getNickName());
        email.setText(mUserData.getEmail());
        motorcycle.setText(mUserData.getMotorcycle());

        String ava = mUserData.getAvatar();
        Log.i("log", "onOnlineUserProfileReady: "+ava);
        //Google avatars increase size
        if (ava.contains(".googleusercontent.com/")) {
            ava = ava.replace("/s96-c", "/s300-c");
            Log.i("log", "onOnlineUserProfileReady: "+ava);
        }


        getActivity().setTitle(mUserData.getName());
        name.setText(mUserData.getName());
        nickName.setText(mUserData.getNickName());
        email.setText(mUserData.getEmail());
        motorcycle.setText(mUserData.getMotorcycle());
        Picasso.with(getContext())
                .load(ava)
                .resize(avatar.getMaxWidth(), avatar.getMaxHeight())
                .centerCrop()
                .into(avatar);

        //If friend already added
        if (mFirebaseDatabaseHelper.isInFriendList(mUserData.getId(), Constants.RELATION_FRIEND)) {
            removeFriend.setVisibility(View.VISIBLE);
            addToFriend.setVisibility(View.GONE);
        }

        addToFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),
                        "Add " + mUserData.getName(),
                        Toast.LENGTH_SHORT).show();
                removeFriend.setVisibility(View.VISIBLE);
                addToFriend.setVisibility(View.GONE);
                mFirebaseDatabaseHelper.sendFriendRequest(mUserData.getId());


            }
        });
        removeFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),
                        "Remove " + mUserData.getName(),
                        Toast.LENGTH_SHORT).show();
                removeFriend.setVisibility(View.GONE);
                addToFriend.setVisibility(View.VISIBLE);
                mFirebaseDatabaseHelper.setRelationToUser(mUserData.getId(), null);
                mFirebaseDatabaseHelper.setUserRelation(mUserData.getId(), null);

            }
        });
    }

    @Subscribe
    public void onOnlineUserProfileReady(OnlineUserProfileReadyEvent event) {
        mUserData = event.getUserProfileFirebase();
        displayUserData();
    }
}
