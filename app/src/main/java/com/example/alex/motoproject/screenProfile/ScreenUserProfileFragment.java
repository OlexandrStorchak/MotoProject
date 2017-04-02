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

import com.bumptech.glide.Glide;
import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OnlineUserProfileReadyEvent;
import com.example.alex.motoproject.firebase.Constants;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.UsersProfileFirebase;
import com.example.alex.motoproject.util.DimensHelper;

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
        getActivity().setTitle(getContext().getString(R.string.app_name));
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

        String avatarRef = mUserData.getAvatar();

//        if (avatarRef.contains(".googleusercontent.com/")) {
//            int newSize = avatar.getWidth();
//            avatarRef = avatarRef.replace("/s96-c", "/s" + newSize + "-c"); //Increase Google avatar size
//            Log.i("log", "onOnlineUserProfileReady: "+ avatarRef);
//        }
        DimensHelper.getScaledAvatar(avatarRef,
                avatar.getMaxWidth(), new DimensHelper.AvatarRefReceiver() {
                    @Override
                    public void onRefReady(String ref) {
                        //        Picasso.with(getContext())
//                .load(avatarRef)
//                .resize(avatar.getMaxWidth(), avatar.getMaxHeight())
//                .centerCrop()
//                .into(avatar);
                        Glide.with(getContext())
                                .load(ref)
                                .override(avatar.getMaxWidth(), avatar.getMaxHeight())
                                .centerCrop()
                                .into(avatar);
                    }

                    @Override
                    public void onError() {

                    }
                });

        getActivity().setTitle(mUserData.getName());
        name.setText(mUserData.getName());
        nickName.setText(mUserData.getNickName());
        email.setText(mUserData.getEmail());
        motorcycle.setText(mUserData.getMotorcycle());

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
