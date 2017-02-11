package com.example.alex.motoproject.mainActivity;


import android.support.v4.app.FragmentManager;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.screenLogin.ScreenLoginFragment;
import com.example.alex.motoproject.screenLogin.SignUpFragment;
import com.example.alex.motoproject.screenMap.ScreenMapFragment;
import com.example.alex.motoproject.screenOnlineUsers.OnlineUsersFragment;

import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_AUTH;
import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_MAP;
import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_ONLINE_USERS;
import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_SIGN_UP;

public class ManageFragment extends MainActivity {
    FragmentManager fragmentManager;

    public ManageFragment(FragmentManager fragmentManager) {

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
                fragmentTransaction.replace(R.id.main_activity_frame, new ScreenLoginFragment());
                fragmentTransaction.commit();
                break;


            case FRAGMENT_MAP:
                fragmentTransaction.replace(R.id.main_activity_frame,
                        ScreenMapFragment.getInstance(),
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
