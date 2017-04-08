package com.example.alex.motoproject.mainActivity;

import android.Manifest;
import android.app.Activity;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.event.CurrentUserProfileReadyEvent;
import com.example.alex.motoproject.event.InternetStatusChangedEvent;
import com.example.alex.motoproject.event.OpenMapEvent;
import com.example.alex.motoproject.event.ShowUserProfileEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.screenChat.ChatFragment;
import com.example.alex.motoproject.screenLogin.LoginActivity;
import com.example.alex.motoproject.screenMap.ScreenMapFragment;
import com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment;
import com.example.alex.motoproject.screenProfile.ScreenUserProfileFragment;
import com.example.alex.motoproject.screenUsers.UsersFragment;
import com.example.alex.motoproject.service.LocationListenerService;
import com.example.alex.motoproject.service.MainService;
import com.example.alex.motoproject.util.CircleTransform;
import com.example.alex.motoproject.util.DimensHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import static com.example.alex.motoproject.firebase.Constants.STATUS_NO_GPS;
import static com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment.PROFILE_GPS_MODE_FRIENDS;
import static com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment.PROFILE_GPS_MODE_NOGPS;
import static com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment.PROFILE_GPS_MODE_PUBLIC;
import static com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment.PROFILE_GPS_MODE_SOS;
import static com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment.PROFSET;
import static com.example.alex.motoproject.util.ArgKeys.ACTIONBAR_STATUS;
import static com.example.alex.motoproject.util.ArgKeys.EMAIL;
import static com.example.alex.motoproject.util.ArgKeys.KEY_AVATAR_REF;
import static com.example.alex.motoproject.util.ArgKeys.KEY_LIST_TYPE;
import static com.example.alex.motoproject.util.ArgKeys.KEY_NAME;
import static com.example.alex.motoproject.util.ArgKeys.KEY_UID;
import static com.example.alex.motoproject.util.ArgKeys.KEY_USER_COORDS;
import static com.example.alex.motoproject.util.ArgKeys.SHOW_MAP_FRAGMENT;
import static com.example.alex.motoproject.util.ArgKeys.SIGN_OUT;


public class MainActivity extends AppCompatActivity implements
        FragmentManager.OnBackStackChangedListener, FirebaseDatabaseHelper.AuthLoadingListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int USER_LIST_TYPE_FRIENDS = 10;

    private static final int ACTIONBAR_HIDDEN = 0;
    private static final int ACTIONBAR_SHOWED = 1;
    private static final int ACTIONBAR_UP_BUTTON = 2;

    private static final String MAP_FRAGMENT_TAG = "mapFragment";
    private static final String ONLINE_USERS_FRAGMENT_TAG = "onlineUsersFragment";
    private static final String FRIENDS_FRAGMENT_TAG = "friendsFragment";
    private static final String MY_PROFILE_FRAGMENT_TAG = "myProfileFragment";
    private static final String PROFILE_FRAGMENT_TAG = "profileFragmentTag";
    private static final String CHAT_FRAGMENT = "chatFragment";

    private static final String TAG = "MainActivity";

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    @Inject
    NetworkStateReceiver mNetworkStateReceiver;

    ScreenMapFragment screenMapFragment;
    UsersFragment onlineUsersFragment;
    UsersFragment friendsFragment;
    ScreenMyProfileFragment screenMyProfileFragment;
    ChatFragment chatFragment;
    AlertControl alertControl = new AlertControl(this);

    private App mApp;

    private Intent mainServiceIntent;

    private FragmentManager mFragmentManager = getSupportFragmentManager();

    private LinearLayout mGpsStatus;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;

    private TextView mNameHeader;
    private TextView mEmailHeader;
    private ImageView mAvatarHeader;

    private Spinner mapVisibility;
    private ImageView mapIndicator;
    private Button mButtonStartRide;

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

    private String mName;
    private String mEmail;
    private String mAvatarRef;

    private int actionbarStatus = ACTIONBAR_SHOWED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        App.getCoreComponent().inject(this);
        App.getCoreComponent().inject(alertControl);

        mainServiceIntent = new Intent(this, MainService.class);

        if (mFirebaseDatabaseHelper.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
            return;
        }

        mApp = (App) getApplicationContext();

//        MainActivityPresenter presenterImp = MainActivityPresenter.getInstance(this);

        screenMapFragment = (ScreenMapFragment)
                getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        if (screenMapFragment == null) {
            screenMapFragment = new ScreenMapFragment();
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

        screenMyProfileFragment = (ScreenMyProfileFragment)
                getSupportFragmentManager().findFragmentByTag(MY_PROFILE_FRAGMENT_TAG);
        if (screenMyProfileFragment == null) {
            screenMyProfileFragment = new ScreenMyProfileFragment();
        }

        chatFragment = (ChatFragment)
                getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT);
        if (chatFragment == null) {
            chatFragment = new ChatFragment();
        }

//        loginController = new FirebaseLoginController(presenterImp);
//        loginController.start();

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
                hideKeyboard();
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
                        .addToBackStack(null)
                        .replace(R.id.main_activity_frame,
                                screenMyProfileFragment,
                                MY_PROFILE_FRAGMENT_TAG).commit();

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
                startRideService();
            }
        });

        //Button in Navigation Drawer for show the Map fragment
        Button navigationBtnMap = (Button) mNavigationView.findViewById(R.id.navigation_btn_map);
        navigationBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                replaceFragment(screenMapFragment);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_activity_frame, screenMapFragment, MAP_FRAGMENT_TAG)
                        .commit();

                screenMapFragment.onMapCk();
                mDrawerLayout.closeDrawers();
            }
        });

        //Button in Navigation Drawer for SignOut
        Button navigationBtnSignOut = (Button) mNavigationView.findViewById(R.id.navigation_btn_signout);
        navigationBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawers();
                stopService(new Intent(MainActivity.this, LocationListenerService.class));
                mFirebaseDatabaseHelper.setUserOnline(null); //delete user from online users table
                startActivity(new Intent(MainActivity.this, LoginActivity.class)
                        .putExtra(SIGN_OUT, true)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
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
//                replaceFragment(friendsFragment, USER_LIST_TYPE_FRIENDS);
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

        mapVisibility = (Spinner) mNavigationView.findViewById(R.id.profile_set_gps_visibility);
        mapIndicator = (ImageView) mNavigationView.findViewById(R.id.profile_show_on_map_indicator);
        mapVisibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                            mapIndicator.setImageResource(R.mipmap.ic_map_indicator_green);
                            mGpsStatus.setBackground(ContextCompat
                                    .getDrawable(MainActivity.this, R.drawable.button_start));
                            break;
                        case 1:
                            preferences.edit()
                                    .putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(),
                                            PROFILE_GPS_MODE_FRIENDS).apply();
                            mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_FRIENDS);
                            mapIndicator.setImageResource(R.mipmap.ic_map_indicator_yellow);
                            mGpsStatus.setBackground(ContextCompat
                                    .getDrawable(MainActivity.this, R.drawable.button_ready));
                            break;
                        case 2:
                            preferences.edit()
                                    .putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(),
                                            PROFILE_GPS_MODE_SOS).apply();
                            mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_SOS);
                            mapIndicator.setImageResource(R.mipmap.ic_map_indicator_red);
                            mGpsStatus.setBackground(ContextCompat
                                    .getDrawable(MainActivity.this, R.drawable.button_stop));
                            break;
                        case 3:
                            preferences.edit()
                                    .putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(),
                                            PROFILE_GPS_MODE_NOGPS).apply();
                            mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_NOGPS);
                            mapIndicator.setImageResource(R.mipmap.ic_map_indicator_red);
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

        if (mApp.isLocationListenerServiceOn()) {
            mGpsStatus.setVisibility(View.VISIBLE);
            mButtonStartRide.setText(R.string.stop_location_service_button_tittle);

        } else if (checkLocationPermission()) {
            mGpsStatus.setVisibility(View.GONE);
            mButtonStartRide.setText(R.string.start_location_service_button_title);
        }

        if (getIntent().getBooleanExtra(SHOW_MAP_FRAGMENT, false) || savedInstanceState == null) {
            getIntent().removeExtra(SHOW_MAP_FRAGMENT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame, screenMapFragment)
                    .commit();
        }

        handleUser(mFirebaseDatabaseHelper.getCurrentUser());
        Log.d(TAG, "onCreate: ");
    }

    private void startRideService() {
        if (!mApp.isLocationListenerServiceOn()) {
            alertControl.handleLocation();
            mGpsStatus.setVisibility(View.VISIBLE);
            mButtonStartRide.setText(R.string.stop_location_service_button_tittle);
            mButtonStartRide.setTextColor(ContextCompat.getColor(this, R.color.red800));
            mButtonStartRide.setBackground(ContextCompat.getDrawable(this, R.drawable.button_stop));
            screenMapFragment.setSosVisibility(View.VISIBLE);

        } else if (checkLocationPermission()) {
            screenMapFragment.getGoogleMap().setMyLocationEnabled(false);
            chatFragment.hideShareLocationButton();
            getApplication().stopService(
                    new Intent(getApplicationContext(), LocationListenerService.class));
            mGpsStatus.setVisibility(View.GONE);
            screenMapFragment.setSosVisibility(View.GONE);

            mButtonStartRide.setText(R.string.start_location_service_button_title);
            mButtonStartRide.setTextColor(ContextCompat.getColor(this, R.color.green800));
            mButtonStartRide.setBackground(ContextCompat.getDrawable(this, R.drawable.button_start));

            mFirebaseDatabaseHelper.setUserOnline(STATUS_NO_GPS);
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view == null) return;

        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
        alertControl.plusNetworkStateReceiver();
        alertControl.registerNetworkStateReceiver();
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

        alertControl.unregisterNetworkStateReceiver();
        alertControl.unregisterEventBus();
//        loginController.stop();

        mFragmentManager.removeOnBackStackChangedListener(this);
        Log.d(TAG, "onStop: ");
    }

    //Needed for Facebook handleUser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Send result to ScreenLoginFragment for Facebook auth.manager
//        screenLoginFragment.getCallbackManager().onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (alertControl.mAlert != null) {
            alertControl.mAlert.dismiss();
        }

        EventBus.getDefault().unregister(this);
        stopService(mainServiceIntent);
        Log.d(TAG, "onDestroy: ");
    }

    @Subscribe
    public void onShowOnlineUserProfile(ShowUserProfileEvent model) {
        ScreenUserProfileFragment userProfile = new ScreenUserProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_frame, userProfile, PROFILE_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
        mFirebaseDatabaseHelper.getUserModel(model.getUserId());
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
                case ACTIONBAR_HIDDEN:
                    hideActionBar();
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

//        if (savedInstanceState.getBoolean(RIDE_SERVICE_ON)) {
//            mGpsStatus.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_NAME, mName);
        outState.putString(EMAIL, mEmail);
        outState.putString(KEY_AVATAR_REF, mAvatarRef);
//        outState.putBoolean(RIDE_SERVICE_ON, rideServiceOn);
        outState.putInt(ACTIONBAR_STATUS, actionbarStatus);
    }

    @Subscribe
    public void onCurrentUserModelReadyEvent(CurrentUserProfileReadyEvent user) {
        mName = user.getMyProfileFirebase().getName();
        mEmail = user.getMyProfileFirebase().getEmail();
        mAvatarRef = user.getMyProfileFirebase().getAvatar();
        setCurrentUserData();
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
                                .transform(new CircleTransform())
                                .into(mAvatarHeader);
//        Glide.with(this)
//                .load(user.getMyProfileFirebase().getAvatar())
//                .override(mAvatarHeader.getMaxWidth() * 2, mAvatarHeader.getMaxHeight() * 2)
//                .transform(new CropCircleTransformation(this))
//                .into(mAvatarHeader);
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
            screenMapFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame, screenMapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        } else if (userCoords != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_USER_COORDS, userCoords);
            screenMapFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_frame, screenMapFragment, MAP_FRAGMENT_TAG)
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
        startService(mainServiceIntent);

        mFirebaseDatabaseHelper.addUserToFirebase(
                user.getUid(),
                user.getEmail(),
                user.getDisplayName(),
                String.valueOf(user.getPhotoUrl()));
        mFirebaseDatabaseHelper.getCurrentUserModel();

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
                mapIndicator.setImageResource(R.mipmap.ic_map_indicator_red);
                mapVisibility.setSelection(3);
                break;
            case PROFILE_GPS_MODE_SOS:
                mGpsStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.button_stop));
                mapIndicator.setImageResource(R.mipmap.ic_map_indicator_red);
                mapVisibility.setSelection(2);
                break;
            case PROFILE_GPS_MODE_FRIENDS:
                mGpsStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.button_ready));
                mapIndicator.setImageResource(R.mipmap.ic_map_indicator_yellow);
                mapVisibility.setSelection(1);
                break;
            case PROFILE_GPS_MODE_PUBLIC:
                mGpsStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.button_start));
                mapIndicator.setImageResource(R.mipmap.ic_map_indicator_green);
                mapVisibility.setSelection(0);
                break;
        }

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        mFirebaseDatabaseHelper.setUserOnline(sharedPreferences
                .getString(mFirebaseDatabaseHelper.getCurrentUser().getUid(), null));
    }

    private void showActionBar() {
        if (getSupportActionBar() != null) {
            actionbarStatus = ACTIONBAR_SHOWED;
            getSupportActionBar().show();
        }
    }

    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            actionbarStatus = ACTIONBAR_HIDDEN;
            getSupportActionBar().hide();
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
        mToolbar.setNavigationIcon(R.mipmap.ic_back_button);
        mToolbar.setNavigationOnClickListener(backButtonBack);
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
        mFirebaseDatabaseHelper.getFriends();
        mFirebaseDatabaseHelper.setUserOfflineOnDisconnect();
    }

    @Subscribe(sticky = true)
    public void onInternetStatusChanged(InternetStatusChangedEvent event) {
        int visibility;
        if (event.isInternetOn()) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }
        if (mButtonStartRide != null) mButtonStartRide.setVisibility(visibility);
    }
}
