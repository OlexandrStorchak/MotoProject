package com.example.alex.motoproject.mainActivity;


import android.support.v4.app.FragmentManager;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.screenLogin.AuthFragment;
import com.example.alex.motoproject.screenLogin.SignUpFragment;
import com.example.alex.motoproject.screenMap.MapFragment;
import com.example.alex.motoproject.screenOnlineUsers.OnlineUsersFragment;

import static com.example.alex.motoproject.mainActivity.FragmentContract.FRAGMENT_AUTH;
import static com.example.alex.motoproject.mainActivity.FragmentContract.FRAGMENT_MAP;
import static com.example.alex.motoproject.mainActivity.FragmentContract.FRAGMENT_ONLINE_USERS;
import static com.example.alex.motoproject.mainActivity.FragmentContract.FRAGMENT_SIGN_UP;

public class FragmentReplace extends MainActivity {
    FragmentManager fragmentManager;

    public FragmentReplace(FragmentManager fragmentManager) {

        this.fragmentManager = fragmentManager;
    }

    // manage fragments
    public void replaceFragment(String fragmentName) {
        android.support.v4.app.FragmentTransaction fragmentTransaction
                = fragmentManager.beginTransaction();
        switch (fragmentName) {
            case FRAGMENT_SIGN_UP:
                fragmentTransaction.replace(R.id.main_activity_frame, SignUpFragment.getInstance());
                fragmentTransaction.addToBackStack(FRAGMENT_SIGN_UP);
                fragmentTransaction.commit();
                break;

            case FRAGMENT_AUTH:
                fragmentTransaction.replace(R.id.main_activity_frame, new AuthFragment());
                fragmentTransaction.commit();
                break;


            case FRAGMENT_MAP:
                fragmentTransaction.replace(R.id.main_activity_frame,
                        MapFragment.getInstance(),
                        FRAGMENT_MAP);
                fragmentTransaction.commitAllowingStateLoss();
                break;

            case FRAGMENT_ONLINE_USERS:

                fragmentTransaction.replace(R.id.main_activity_frame, OnlineUsersFragment.getInstance());
                fragmentTransaction.commit();
                break;
        }
    }
}
