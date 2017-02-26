package com.example.alex.motoproject.mainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.CurrentUserProfileReadyEvent;
import com.example.alex.motoproject.event.ShowUserProfile;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.FirebaseLoginController;
import com.example.alex.motoproject.screenChat.ChatFragment;
import com.example.alex.motoproject.screenLogin.ScreenLoginFragment;
import com.example.alex.motoproject.screenMap.ScreenMapFragment;
import com.example.alex.motoproject.screenOnlineUsers.ScreenOnlineUsersFragment;
import com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment;
import com.example.alex.motoproject.screenProfile.ScreenUserProfileFragment;
import com.example.alex.motoproject.service.LocationListenerService;
import com.example.alex.motoproject.util.CircleTransform;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements
        MainViewInterface, FragmentManager.OnBackStackChangedListener {

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    protected ScreenMapFragment screenMapFragment = new ScreenMapFragment();
    private ScreenOnlineUsersFragment screenOnlineUsersFragment
            = new ScreenOnlineUsersFragment();
    private ScreenLoginFragment screenLoginFragment = new ScreenLoginFragment();
    private ScreenMyProfileFragment screenProfileFragment = new ScreenMyProfileFragment();
    private ChatFragment chatFragment = new ChatFragment();

    AlertControl alertControl = new AlertControl(this);


    private TextView mNameHeader;
    private TextView mEmailHeader;
    private ImageView mAvatarHeader;
    private Button mNavigationBtnMap;
    private Button mNavigationBtnSignOut;
    private DrawerLayout mDrawerLayout;

    private FragmentManager fm = getSupportFragmentManager();

    private FirebaseLoginController loginController;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;


    private View.OnClickListener backButtonBack = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    };
    private View.OnClickListener drawerMenu = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        App.getCoreComponent().inject(this);

        MainActivityPresenter presenterImp = new MainActivityPresenter(this);

        loginController = new FirebaseLoginController(presenterImp);

        loginController.start();

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(
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

        mAvatarHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fm.beginTransaction()
                        .addToBackStack("profile")
                        .replace(R.id.main_activity_frame, screenProfileFragment)
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
                mFirebaseDatabaseHelper.setUserOffline();
                loginController.signOut();
                stopService(new Intent(MainActivity.this, LocationListenerService.class));


            }
        });
        //Button in Navigation Drawer for display Friends List
        Button mNavigationBtnUsersOnline =
                (Button) mNavigationView.findViewById(R.id.navigation_btn_users_online);
        mNavigationBtnUsersOnline.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                replaceFragment(screenOnlineUsersFragment);
                mDrawerLayout.closeDrawers();

            }
        });


        //Button in Navigation Drawer for displaying chat
        Button navigationBtnChat = (Button) mNavigationView.findViewById(R.id.navigation_btn_chat);
        navigationBtnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fm.beginTransaction()
                        .replace(R.id.main_activity_frame, chatFragment)
                        .commit();
                mDrawerLayout.closeDrawers();
            }
        });

    }


    @Override
    public void onBackPressed() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();

        }
        fm.popBackStack();
    }


    @Override
    protected void onStart() {
        super.onStart();
        alertControl.registerNetworkStateReceiver(getApplicationContext());
        alertControl.registerEventBus();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        alertControl.unregisterNetworkStateReceiver();
        alertControl.unRegisterEventBus();
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
        if (alertControl.mAlert != null) {
            alertControl.mAlert.dismiss();
        }

        if (!alertControl.isServiceOn()) {

            Log.d("log", "onDestroy: service is Off");
        }
        super.onDestroy();
        EventBus.getDefault().unregister(this);


    }


    @Subscribe
    public void onShowOnlineUserProfile(ShowUserProfile model) {

        ScreenUserProfileFragment userProfile = new ScreenUserProfileFragment();

        fm.beginTransaction().addToBackStack("online")
                .replace(R.id.main_activity_frame, userProfile)
                .commit();
        mFirebaseDatabaseHelper.getUserModel(model.getUserId());
    }

    @Subscribe
    public void onCurrentUserModelReadyEvent(CurrentUserProfileReadyEvent user) {
        mNameHeader.setText(user.getMyProfileFirebase().getName());
        mEmailHeader.setText(user.getMyProfileFirebase().getEmail());

        Picasso.with(getApplicationContext())
                .load(user.getMyProfileFirebase().getAvatar())
                .resize(mAvatarHeader.getMaxWidth(), mAvatarHeader.getMaxHeight())
                .centerCrop()
                .transform(new CircleTransform())
                .into(mAvatarHeader);

    }


    @Override
    public void login(FirebaseUser user) {
        // TODO: 11.02.2017 let users choose avatars
        mFirebaseDatabaseHelper.addUserToFirebase(
                user.getUid(),
                user.getEmail(),
                user.getDisplayName(),
                String.valueOf(user.getPhotoUrl()));
        mFirebaseDatabaseHelper.getCurrentUserModel();

        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }

        mNavigationBtnSignOut.setVisibility(View.VISIBLE);
        mNavigationBtnMap.setVisibility(View.VISIBLE);
        mAvatarHeader.setVisibility(View.VISIBLE);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        replaceFragment(screenMapFragment);
        mFirebaseDatabaseHelper.setUserOnline("noGps");
        fm.addOnBackStackChangedListener(this);


    }


    @Override
    public void logout() {
        fm.removeOnBackStackChangedListener(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
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
        fm.beginTransaction()
                .replace(R.id.main_activity_frame, fragment)
                .commit();
    }


    @Override
    public void onBackStackChanged() {
        Log.d("log", "onBackStackChanged: " + fm.getBackStackEntryCount());
        if (fm.getBackStackEntryCount() > 0) {
            toolbar.setNavigationIcon(R.mipmap.ic_back_button);
            toolbar.setNavigationOnClickListener(backButtonBack);
            mDrawerLayout.closeDrawers();
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toolbar.setNavigationOnClickListener(drawerMenu);
            toggle.syncState();

        }
    }
}
