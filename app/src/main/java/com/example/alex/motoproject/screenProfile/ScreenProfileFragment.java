package com.example.alex.motoproject.screenProfile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.screenOnlineUsers.OnlineUsersModel;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ScreenProfileFragment extends Fragment {
    private TextView email;
    private ImageView avatar;
    private FirebaseDatabaseHelper firebaseDabaseHelper;

    public ScreenProfileFragment() {
        // Required empty public constructor
    }

    public void setHelper(FirebaseDatabaseHelper firebaseDatabaseHelper) {
        this.firebaseDabaseHelper = firebaseDatabaseHelper;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        avatar = (ImageView) view.findViewById(R.id.profile_avatar);
        email = (TextView) view.findViewById(R.id.profile_email);
        MyProfile(firebaseDabaseHelper.getCurrentUser());

    }


    public void MyProfile(FirebaseUser user) {

        email.setText(user.getEmail());


        Picasso.with(getApplicationContext())
                .load(user.getPhotoUrl())
                .resize(avatar.getMaxWidth(), avatar.getMaxHeight())
                .centerCrop()
                .into(avatar);
    }

    public void UserProfile (OnlineUsersModel user){
        email.setText(user.getName());

        Picasso.with(getApplicationContext())
                .load(user.getAvatar())
                .resize(avatar.getMaxWidth(), avatar.getMaxHeight())
                .centerCrop()
                .into(avatar);
    }

}
