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
import com.example.alex.motoproject.events.CurrentUserProfileReadyEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.MyProfileFirebase;
import com.example.alex.motoproject.screenOnlineUsers.OnlineUsersModel;
import com.example.alex.motoproject.utils.CircleTransform;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ScreenMyProfileFragment extends Fragment {
    private TextView email;
    private ImageView avatar;
    private FirebaseDatabaseHelper firebaseDabaseHelper;
    String currentUserId;

    public ScreenMyProfileFragment() {
        // Required empty public constructor
    }

    public void setHelper(FirebaseDatabaseHelper firebaseDatabaseHelper) {
        this.firebaseDabaseHelper = firebaseDatabaseHelper;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
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

        firebaseDabaseHelper.getCurrentUserModel();


    }
    @Subscribe
    public void onCurrentUserModelReadyEvent(CurrentUserProfileReadyEvent user) {

        email.setText(user.getMyProfileFirebase().getEmail());

        Picasso.with(getApplicationContext())
                .load(user.getMyProfileFirebase().getAvatar())
                .resize(avatar.getMaxWidth(), avatar.getMaxHeight())
                .centerCrop()
                .into(avatar);

    }
public void setUserId(String currentUserId){
        this.currentUserId=currentUserId;

}

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
