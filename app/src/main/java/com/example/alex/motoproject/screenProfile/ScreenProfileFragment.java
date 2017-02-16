package com.example.alex.motoproject.screenProfile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.screenOnlineUsers.OnlineUsersModel;

public class ScreenProfileFragment extends Fragment  {
private TextView email;



    public ScreenProfileFragment() {
        // Required empty public constructor
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

        email = (TextView)view.findViewById(R.id.profile_email);

    }
    public void setData(OnlineUsersModel user){
        email.setText(user.getUid());
    }

    public void setEmail(String e){
        email.setText(e);
    }


}
