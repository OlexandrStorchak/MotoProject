package com.example.alex.motoproject.mainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.FirebaseLoginController;
import com.example.alex.motoproject.screenLogin.ScreenLoginFragment;
import com.example.alex.motoproject.screenMap.ScreenMapFragment;
import com.example.alex.motoproject.screenOnlineUsers.ScreenOnlineUsersFragment;
import com.example.alex.motoproject.screenProfile.ScreenProfileFragment;
import com.example.alex.motoproject.services.LocationListenerService;
import com.example.alex.motoproject.utils.CircleTransform;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements
        MainViewInterface {


    private static final String STANDART_AVATAR = "https://firebasestorage.googleapis.com/v0/b/profiletests-d3a61.appspot.com/o/ava4.png?alt=media&token=96951c00-fd27-445c-85a6-b636bd0cb9f5";

    protected ScreenMapFragment screenMapFragment = new ScreenMapFragment();
    private ScreenOnlineUsersFragment screenOnlineUsersFragment = new ScreenOnlineUsersFragment();
    private ScreenLoginFragment screenLoginFragment = new ScreenLoginFragment();
    private ScreenProfileFragment screenProfileFragment = new ScreenProfileFragment();
    AlertControll alertControll = new AlertControll(this);

    private TextView mNameHeader;
    private TextView mEmailHeader;
    private ImageView mAvatarHeader;
    private Button mNavigationBtnMap;
    private Button mNavigationBtnSignOut;
    private DrawerLayout mDrawerLayout;
    private TextView mProfileSetting;



    private FirebaseDatabaseHelper mDatabaseHelper = new FirebaseDatabaseHelper();

    private FirebaseLoginController loginController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivityPresenter presenterImp = new MainActivityPresenter(this);

        loginController = new FirebaseLoginController(presenterImp);

        loginController.start();

        screenProfileFragment.setHelper(mDatabaseHelper);

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        //Define view of Navigation Drawer
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        //Get View of Navigation menu Header
        final View header = mNavigationView.getHeaderView(0);

        //Define View elements of Header in Navigation Drawer
        mNameHeader = (TextView) header.findViewById(R.id.header_name);
        mEmailHeader = (TextView) header.findViewById(R.id.header_email);
        mAvatarHeader = (ImageView) header.findViewById(R.id.header_avatar);
        mProfileSetting = (TextView) header.findViewById(R.id.header_profile_setting);
        mAvatarHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack("profile")
                        .replace(R.id.main_activity_frame,screenProfileFragment)
                        .commit();

                mDrawerLayout.closeDrawers();
            }
        });


        //Button in Navigation Drawer for show the Map fragment
        mNavigationBtnMap = (Button) mNavigationView.findViewById(R.id.navigation_btn_map);
        mNavigationBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(screenMapFragment);
                screenMapFragment.onMapCk();
                mDrawerLayout.closeDrawers();

            }
        });
        //Button in Navigation Drawer for SignOut
        mNavigationBtnSignOut = (Button) mNavigationView.findViewById(R.id.navigation_btn_signout);
        mNavigationBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseHelper.setUserOffline();
                loginController.signOut();
                stopService(new Intent(MainActivity.this, LocationListenerService.class));


            }
        });
        //Button in Navigation Drawer for display Friends List
        Button mNavigationBtnUsersOnline = (Button) mNavigationView.findViewById(R.id.navigation_btn_users_online);
        mNavigationBtnUsersOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(screenOnlineUsersFragment);
                screenOnlineUsersFragment.setScreenMapFragment(screenMapFragment);
                mDrawerLayout.closeDrawers();

            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        alertControll.registerNetworkStateReceiver();
        alertControll.registerEventBus();
    }


    @Override
    protected void onStop() {
        super.onStop();
        alertControll.unregisterNetworkStateReceiver();
        alertControll.unRegisterEventBus();
        loginController.stop();
    }


    //Need for Facebook login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Send result to ScreenLoginFragment for Facebook auth.manager

        ScreenLoginFragment.getCallbackManager().onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onDestroy() {
        if (alertControll.mAlert != null) {
            alertControll.mAlert.dismiss();
        }

        if (!alertControll.isServiceOn()) {

            Log.d("log", "onDestroy: service is Off");
        }
        super.onDestroy();

    }


    @Override
    public void login(FirebaseUser user) {
        // TODO: 11.02.2017 let users choose avatars
        String avatarUri = STANDART_AVATAR;
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.show();
        }

        mNavigationBtnSignOut.setVisibility(View.VISIBLE);
        mNavigationBtnMap.setVisibility(View.VISIBLE);
        mAvatarHeader.setVisibility(View.GONE);

        mNameHeader.setText(user.getDisplayName());
        mEmailHeader.setText(user.getEmail());
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (user.getPhotoUrl() != null) {
            mAvatarHeader.setVisibility(View.VISIBLE);
            avatarUri = user.getPhotoUrl().toString();
            Picasso.with(getApplicationContext())
                    .load(avatarUri)
                    .resize(mAvatarHeader.getMaxWidth(), mAvatarHeader.getMaxHeight())
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(mAvatarHeader);
        }

        replaceFragment(screenMapFragment);
        mDatabaseHelper.addUserToFirebase(
                user.getUid(),
                user.getEmail(),
                user.getDisplayName(),
                avatarUri);
        mDatabaseHelper.setUserOnline("noGps");



    }

    @Override
    public void logout() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.hide();
        }
        replaceFragment(screenLoginFragment);
        mNavigationBtnSignOut.setVisibility(View.GONE);
        mNavigationBtnMap.setVisibility(View.GONE);
        mNameHeader.setText("");
        mEmailHeader.setText("");
        mAvatarHeader.setVisibility(View.INVISIBLE);
        mDrawerLayout.closeDrawers();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_frame, fragment)
                .commit();
    }


}