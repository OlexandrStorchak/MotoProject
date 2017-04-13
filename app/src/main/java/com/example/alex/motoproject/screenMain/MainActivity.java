package com.example.alex.motoproject.screenMain;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.app.App;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.event.OpenMapEvent;
import com.example.alex.motoproject.event.ShowUserProfileEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.UserProfileFirebase;
import com.example.alex.motoproject.locationService.LocationService;
import com.example.alex.motoproject.mainService.MainService;
import com.example.alex.motoproject.screenChat.ChatFragment;
import com.example.alex.motoproject.screenLogin.LoginActivity;
import com.example.alex.motoproject.screenMap.MapFragment;
import com.example.alex.motoproject.screenProfile.MyProfileFragment;
import com.example.alex.motoproject.screenProfile.UserProfileFragment;
import com.example.alex.motoproject.screenUsers.UsersFragment;
import com.example.alex.motoproject.transformation.PicassoCircleTransform;
import com.example.alex.motoproject.util.DimensHelper;
import com.example.alex.motoproject.util.KeyboardUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import static com.example.alex.motoproject.firebase.FirebaseConstants.STATUS_NO_GPS;
import static com.example.alex.motoproject.screenProfile.MyProfileFragment.PROFILE_GPS_MODE_FRIENDS;
import static com.example.alex.motoproject.screenProfile.MyProfileFragment.PROFILE_GPS_MODE_NOGPS;
import static com.example.alex.motoproject.screenProfile.MyProfileFragment.PROFILE_GPS_MODE_PUBLIC;
import static com.example.alex.motoproject.screenProfile.MyProfileFragment.PROFILE_GPS_MODE_SOS;
import static com.example.alex.motoproject.screenProfile.MyProfileFragment.PROFSET;
import static com.example.alex.motoproject.util.ArgKeys.ACTIONBAR_STATUS;
import static com.example.alex.motoproject.util.ArgKeys.EMAIL;
import static com.example.alex.motoproject.util.ArgKeys.KEY_AVATAR_REF;
import static com.example.alex.motoproject.util.ArgKeys.KEY_LIST_TYPE;
import static com.example.alex.motoproject.util.ArgKeys.KEY_NAME;
import static com.example.alex.motoproject.util.ArgKeys.KEY_UID;
import static com.example.alex.motoproject.util.ArgKeys.KEY_USER_COORDS;
import static com.example.alex.motoproject.util.ArgKeys.SHOW_CHAT_FRAGMENT;
import static com.example.alex.motoproject.util.ArgKeys.SHOW_MAP_FRAGMENT;
import static com.example.alex.motoproject.util.ArgKeys.SIGN_OUT;


public class MainActivity extends AppCompatActivity implements
        FragmentManager.OnBackStackChangedListener, FirebaseDatabaseHelper.AuthLoadingListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int USER_LIST_TYPE_FRIENDS = 10;

    private static final int ACTIONBAR_SHOWED = 1;
    private static final int ACTIONBAR_UP_BUTTON = 2;

    private static final String MAP_FRAGMENT_TAG = "mapFragment";
    private static final String ONLINE_USERS_FRAGMENT_TAG = "onlineUsersFragment";
    private static final String FRIENDS_FRAGMENT_TAG = "friendsFragment";
    private static final String CHAT_FRAGMENT = "chatFragment";

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    MapFragment mapFragment;
    UsersFragment onlineUsersFragment;
    UsersFragment friendsFragment;
    ChatFragment chatFragment;

    AlertControl alertControl = new AlertControl(this);

    private App mApp;

    private FragmentManager mFragmentManager = getSupportFragmentManager();

    private LinearLayout mGpsStatus;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;

    private TextView mNameHeader;
    private TextView mEmailHeader;
    private ImageView mAvatarHeader;

    private Spinner mMapVisibility;
    private ImageView mapIndicator;
    private Button mButtonStartRide;

    private View.OnClickListener mUpButton = new View.OnClickListener() {
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

    private String mName;
    private String mEmail;
    private String mAvatarRef;

    private int actionbarStatus = ACTIONBAR_SHOWED;
    private boolean mWillRecreate;

    private String mUserStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getCoreComponent().inject(this);
        App.getCoreComponent().inject(alertControl);

        if (mFirebaseDatabaseHelper.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
            return;
        } else {
            startService(new Intent(this, MainService.class));
        }

        mApp = (App) getApplicationContext();

//        MainActivityPresenter presenterImp = MainActivityPresenter.getInstance(this);

        mapFragment = (MapFragment)
                getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (mapFragment == null) {
            mapFragment = new MapFragment();
        }

        onlineUsersFragment = (UsersFragment)
                getSupportFragmentManager().findFragmentByTag(ONLINE_USERS_FRAGMENT_TAG);
        if (onlineUsersFragment == null) {
            onlineUsersFragment = new UsersFragment();
        }

        friendsFragment = (UsersFragment)
                getSupportFragmentManager().findFragmentByTag(FRIENDS_FRAGMENT_TAG);
        if (friendsFragment == null) {
            friendsFragment = new UsersFragment();
        }

        chatFragment = (ChatFragment)
                getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT);
        if (chatFragment == null) {
            chatFragment = new ChatFragment();
        }

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // Do whatever you want here
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                KeyboardUtil.hideKeyboard(MainActivity.this);
            }
        };
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

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
                mFragmentManager.beginTransaction()
                        .replace(R.id.main_activity_frame, new MyProfileFragment())
                        .addToBackStack(null)
                        .commit();

                mDrawerLayout.closeDrawers();
            }
        });
        mGpsStatus = (LinearLayout) mNavigationView.findViewById(R.id.profile_gps_panel);
        mButtonStartRide = (Button) mNavigationView.findViewById(R.id.navigation_btn_ride);
        if (mApp.isLocationListenerServiceOn()) {
            mButtonStartRide.setText(R.string.stop_location_service_button_tittle);
            mButtonStartRide.setTextColor(ContextCompat.getColor(this, R.color.red800));
            mButtonStartRide.setBackground(ContextCompat.getDrawable(this, R.drawable.button_stop));
        }
        mButtonStartRide.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                startOrStopRideService();
            }
        });

        //Button in Navigation Drawer for show the Map fragment
        Button navigationBtnMap = (Button) mNavigationView.findViewById(R.id.navigation_btn_map);
        navigationBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_activity_frame, mapFragment, MAP_FRAGMENT_TAG)
                        .commit();

                mDrawerLayout.closeDrawers();
            }
        });

        //Button in Navigation Drawer for SignOut
        Button navigationBtnSignOut = (Button) mNavigationView.findViewById(R.id.navigation_btn_signout);
        navigationBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawers();
                stopService(new Intent(MainActivity.this, LocationService.class));
                mFirebaseDatabaseHelper.setUserOnline(null); //delete user from online users table
                startActivity(new Intent(MainActivity.this, LoginActivity.class)
                        .putExtra(SIGN_OUT, true)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                stopService(new Intent(MainActivity.this, MainService.class));
            }
        });

        //Button in Navigation Drawer for displaying online users list
        Button mNavigationBtnUsersOnline =
                (Button) mNavigationView.findViewById(R.id.navigation_btn_users_online);
        mNavigationBtnUsersOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                replaceFragment(onlineUsersFragment);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_activity_frame,
                                onlineUsersFragment,
                                ONLINE_USERS_FRAGMENT_TAG).commit();
                mDrawerLayout.closeDrawers();
            }
        });

        //Button in Navigation Drawer for displaying friend list
        Button navigationBtnFriends =
                (Button) mNavigationView.findViewById(R.id.navigation_btn_friends);
        navigationBtnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (friendsFragment.getArguments() == null) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_LIST_TYPE, USER_LIST_TYPE_FRIENDS);
                    friendsFragment.setArguments(bundle);
                } else {
                    friendsFragment.getArguments().putInt(KEY_LIST_TYPE, USER_LIST_TYPE_FRIENDS);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_activity_frame, friendsFragment, FRIENDS_FRAGMENT_TAG)
                        .commit();
                mDrawerLayout.closeDrawers();
            }
        });
        //Button in Navigation Drawer for displaying chat
        Button navigationBtnChat = (Button) mNavigationView.findViewById(R.id.navigation_btn_chat);
        navigationBtnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFragmentManager.beginTransaction()
                        .replace(R.id.main_activity_frame, chatFragment)
                        .commit();
                mDrawerLayout.closeDrawers();
            }
        });

        mMapVisibility = (Spinner) mNavigationView.findViewById(R.id.profile_set_gps_visibility);
        mapIndicator = (ImageView) mNavigationView.findViewById(R.id.profile_show_on_map_indicator);
        mMapVisibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mFirebaseDatabaseHelper.getCurrentUser() != null) {
                    SharedPreferences preferences = getApplicationContext()
                            .getSharedPreferences(PROFSET, Context.MODE_PRIVATE);
                    switch (i) {
                        case 0:
                            preferences.edit()
                                    .putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(),
                                            PROFILE_GPS_MODE_PUBLIC).apply();
                            mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_PUBLIC);
                            mapIndicator.setImageResource(R.drawable.ic_map_indicator_green);
                            mGpsStatus.setBackground(ContextCompat
                                    .getDrawable(MainActivity.this, R.drawable.button_start));
                            break;
                        case 1:
                            preferences.edit()
                                    .putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(),
                                            PROFILE_GPS_MODE_FRIENDS).apply();
                            mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_FRIENDS);
                            mapIndicator.setImageResource(R.drawable.ic_map_indicator_yellow);
                            mGpsStatus.setBackground(ContextCompat
                                    .getDrawable(MainActivity.this, R.drawable.button_ready));
                            break;
                        case 2:
                            preferences.edit()
                                    .putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(),
                                            PROFILE_GPS_MODE_SOS).apply();
                            mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_SOS);
                            mapIndicator.setImageResource(R.drawable.ic_map_indicator_red);
                            mGpsStatus.setBackground(ContextCompat
                                    .getDrawable(MainActivity.this, R.drawable.button_stop));
                            break;
                        case 3:
                            preferences.edit()
                                    .putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(),
                                            PROFILE_GPS_MODE_NOGPS).apply();
                            mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_NOGPS);
                            mapIndicator.setImageResource(R.drawable.ic_map_indicator_red);
                            mGpsStatus.setBackground(ContextCompat
                                    .getDrawable(MainActivity.this, R.drawable.button_stop));
                            break;
                    }
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
        mFirebaseDatabaseHelper.registerAuthLoadingListener(this);

        EventBus.getDefault().register(this);

        if (mApp.isLocationListenerServiceOn()) {
//            mGpsStatus.setVisibility(View.VISIBLE);
            mButtonStartRide.setText(R.string.stop_location_service_button_tittle);

        } else if (checkLocationPermission()) {
//            mGpsStatus.setVisibility(View.GONE);
            mButtonStartRide.setText(R.string.start_location_service_button_title);
        }

        if (savedInstanceState == null) {
            mApp.registerNetworkReceiver();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        }
        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getBooleanExtra(SHOW_CHAT_FRAGMENT, false)) {
            intent.removeExtra(SHOW_CHAT_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame, chatFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        } else if (intent.getBooleanExtra(SHOW_MAP_FRAGMENT, false)) {
            intent.removeExtra(SHOW_MAP_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        }
    }

    private void startOrStopRideService() {
        if (!mApp.isLocationListenerServiceOn()) { //Turn on
            alertControl.handleLocation();

//            mGpsStatus.setVisibility(View.VISIBLE);
            mapFragment.setSosVisibility(View.VISIBLE);

            mButtonStartRide.setText(R.string.stop_location_service_button_tittle);
            mButtonStartRide.setTextColor(ContextCompat.getColor(this, R.color.red800));
            mButtonStartRide.setBackground(ContextCompat.getDrawable(this, R.drawable.button_stop));
        } else { //Turn off
            if (checkLocationPermission()) {
                GoogleMap googleMap = mapFragment.getGoogleMap();
                if (googleMap != null) {
                    googleMap.setMyLocationEnabled(false);
                }
            }

            chatFragment.hideShareLocationButton();
            getApplication().stopService(
                    new Intent(getApplicationContext(), LocationService.class));
            mGpsStatus.setVisibility(View.GONE);
            mapFragment.setSosVisibility(View.GONE);

            mButtonStartRide.setText(R.string.start_location_service_button_title);
            mButtonStartRide.setTextColor(ContextCompat.getColor(this, R.color.green800));
            mButtonStartRide.setBackground(ContextCompat.getDrawable(this, R.drawable.button_start));

            mFirebaseDatabaseHelper.setUserOnline(STATUS_NO_GPS);
        }
    }

    public void startLocationListenerService() {
        startService(new Intent(this, LocationService.class));
    }

    @Override
    public void onBackPressed() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        alertControl.registerEventBus();

        mFragmentManager.addOnBackStackChangedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        alertControl.unregisterEventBus();
        mFragmentManager.removeOnBackStackChangedListener(this);
    }

    //Needed for Facebook handleUser
    // TODO: 11.04.2017 delete this
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Send result to ScreenLoginFragment for Facebook auth.manager
//        screenLoginFragment.getCallbackManager().onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (alertControl.alert != null) {
            alertControl.alert.dismiss();
        }

        if (!mWillRecreate) {
            mApp.unregisterNetworkReceiver();
        }

        mFirebaseDatabaseHelper.removeCurrentUserModelListener();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onShowOnlineUserProfile(ShowUserProfileEvent model) {
        KeyboardUtil.hideKeyboard(this);

        UserProfileFragment fragment = new UserProfileFragment();

        Bundle bundle = new Bundle();
        bundle.putString(KEY_UID, model.getUserId());
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Subscribe(sticky = true)
    public void onGpsStatusChangedEvent(GpsStatusChangedEvent event) {
        if (event.isGpsOn()) {
            mGpsStatus.setVisibility(View.VISIBLE);
        } else {
            mGpsStatus.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        mName = savedInstanceState.getString(KEY_NAME);
        mEmail = savedInstanceState.getString(EMAIL);
        mAvatarRef = savedInstanceState.getString(KEY_AVATAR_REF);

        if (getSupportActionBar() != null) {
            switch (savedInstanceState.getInt(ACTIONBAR_STATUS)) {
                case ACTIONBAR_SHOWED:
                    showActionBar();
                    break;
                case ACTIONBAR_UP_BUTTON:
                    lockDrawerAndShowUpButton();
                    break;
            }
        }

        if (mName == null || mEmail == null || mAvatarRef == null) {
            return;
        }
        setCurrentUserData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_NAME, mName);
        outState.putString(EMAIL, mEmail);
        outState.putString(KEY_AVATAR_REF, mAvatarRef);
        outState.putInt(ACTIONBAR_STATUS, actionbarStatus);

        mWillRecreate = true;
    }

    private void setCurrentUserData() {
        mNameHeader.setText(mName);
        mEmailHeader.setText(mEmail);

        DimensHelper.getScaledAvatar(mAvatarRef,
                mAvatarHeader.getMaxWidth(), new DimensHelper.AvatarRefReceiver() {
                    @Override
                    public void onRefReady(String ref) {
                        Picasso.with(getApplicationContext())
                                .load(ref)
                                .resize(mAvatarHeader.getMaxWidth(), mAvatarHeader.getMaxHeight())
                                .centerCrop()
                                .transform(new PicassoCircleTransform())
                                .into(mAvatarHeader);
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    @Subscribe
    public void onOpenMapEvent(OpenMapEvent event) {
        String uid = event.getUserId();
        LatLng userCoords = event.getLatLng();
        if (event.getUserId() != null) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_UID, uid);
            mapFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        } else if (userCoords != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_USER_COORDS, userCoords);
            mapFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        alertControl.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void handleUser(FirebaseUser user) {
        mFirebaseDatabaseHelper.addUserToFirebase(user.getUid(),
                user.getEmail(),
                user.getDisplayName(),
                String.valueOf(user.getPhotoUrl()));
        mFirebaseDatabaseHelper.addListenerCurrentUserModel(new FirebaseDatabaseHelper
                .UserProfileReceiver() {
            @Override
            public void onReady(UserProfileFirebase profile) {
                mName = profile.getName();
                mEmail = profile.getEmail();
                mAvatarRef = profile.getAvatar();
                setCurrentUserData();
            }
        });

        SharedPreferences sharedPreferences = getApplicationContext()
                .getSharedPreferences(PROFSET, MODE_PRIVATE);

        if (sharedPreferences.getString(mFirebaseDatabaseHelper.getCurrentUser().getUid(), null) == null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(), PROFILE_GPS_MODE_PUBLIC);
            editor.apply();
        }

        String gpsMode = sharedPreferences.getString(mFirebaseDatabaseHelper.getCurrentUser().getUid(), null);

        if (gpsMode == null) {
            gpsMode = PROFILE_GPS_MODE_PUBLIC;
        }
        switch (gpsMode) {
            case PROFILE_GPS_MODE_NOGPS:
                mGpsStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.button_stop));
                mapIndicator.setImageResource(R.drawable.ic_map_indicator_red);
                mMapVisibility.setSelection(3);
                break;
            case PROFILE_GPS_MODE_SOS:
                mGpsStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.button_stop));
                mapIndicator.setImageResource(R.drawable.ic_map_indicator_red);
                mMapVisibility.setSelection(2);
                break;
            case PROFILE_GPS_MODE_FRIENDS:
                mGpsStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.button_ready));
                mapIndicator.setImageResource(R.drawable.ic_map_indicator_yellow);
                mMapVisibility.setSelection(1);
                break;
            case PROFILE_GPS_MODE_PUBLIC:
                mGpsStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.button_start));
                mapIndicator.setImageResource(R.drawable.ic_map_indicator_green);
                mMapVisibility.setSelection(0);
                break;
        }

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        mFirebaseDatabaseHelper.setUserOnline(STATUS_NO_GPS);
    }

    private void showActionBar() {
        if (getSupportActionBar() != null) {
            actionbarStatus = ACTIONBAR_SHOWED;
            getSupportActionBar().show();
        }
    }

    public void showDialogFragment(DialogFragment dialogFragment, String tag) {
        dialogFragment.show(mFragmentManager, tag);
    }

    public void showDialogFragment(DialogFragment fragment, String tag, Bundle args) {
        fragment.setArguments(args);
        fragment.show(mFragmentManager, tag);
    }

    @Override
    public void onBackStackChanged() {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            if (mFirebaseDatabaseHelper.getCurrentUser() != null) {
                //If the users is not logged in, do not show ActionBar
                lockDrawerAndShowUpButton();
            }
        } else {
            unlockDrawerAndShowActionbar();
        }
    }

    private void lockDrawerAndShowUpButton() {
        actionbarStatus = ACTIONBAR_UP_BUTTON;
        mToolbar.setNavigationIcon(R.drawable.ic_up_button);
        mToolbar.setNavigationOnClickListener(mUpButton);
        mDrawerLayout.closeDrawers();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void unlockDrawerAndShowActionbar() {
        actionbarStatus = ACTIONBAR_SHOWED;
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mToolbar.setNavigationOnClickListener(drawerMenu);
        mToggle.syncState();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onLoadFinished() {
        handleUser(mFirebaseDatabaseHelper.getCurrentUser());

        mFirebaseDatabaseHelper.getFriends();
        mFirebaseDatabaseHelper.setUserOfflineOnDisconnect();
    }
}
