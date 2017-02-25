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

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.events.CurrentUserProfileReadyEvent;
import com.example.alex.motoproject.events.ShowUserProfile;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.events.CancelAlertEvent;
import com.example.alex.motoproject.events.ConfirmShareLocationInChatEvent;
import com.example.alex.motoproject.events.OpenMapWithLatLngEvent;
import com.example.alex.motoproject.events.ShareLocationInChatAllowedEvent;
import com.example.alex.motoproject.events.ShowAlertEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.FirebaseLoginController;
import com.example.alex.motoproject.screenLogin.ScreenLoginFragment;
import com.example.alex.motoproject.screenMap.ScreenMapFragment;
import com.example.alex.motoproject.screenOnlineUsers.ScreenOnlineUsersFragment;
import com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment;
import com.example.alex.motoproject.screenProfile.ScreenUserProfileFragment;
import com.example.alex.motoproject.services.LocationListenerService;
import com.example.alex.motoproject.utils.CircleTransform;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity implements
        MainViewInterface, FragmentManager.OnBackStackChangedListener {

import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_AUTH;
import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_CHAT;
import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_MAP;
import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_ONLINE_USERS;

    protected ScreenMapFragment screenMapFragment = new ScreenMapFragment();
    private ScreenOnlineUsersFragment screenOnlineUsersFragment
            = new ScreenOnlineUsersFragment();
    private ScreenLoginFragment screenLoginFragment = new ScreenLoginFragment();
    private ScreenMyProfileFragment screenProfileFragment = new ScreenMyProfileFragment();

    AlertControll alertControll = new AlertControll(this);

    public static final int ALERT_GPS_OFF = 20;
    public static final int ALERT_INTERNET_OFF = 21;
    public static final int ALERT_PERMISSION_RATIONALE = 22;
    public static final int ALERT_PERMISSION_NEVER_ASK_AGAIN = 23;
    public static final int ALERT_SHARE_LOCATION_CONFIRMATION = 24;
    public static final int PERMISSION_LOCATION_REQUEST_CODE = 10;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static boolean loginWithEmail = false; // Flag for validate with email login method
    ArrayList<Integer> mActiveAlerts = new ArrayList<>();
    private NetworkStateReceiver mNetworkStateReceiver;
    private AlertDialog mAlert;
    private TextView mNameHeader;
    private TextView mEmailHeader;
    private ImageView mAvatarHeader;
    private Button mNavigationBtnMap;
    private Button mNavigationBtnSignOut;
    private DrawerLayout mDrawerLayout;

    private FragmentManager fm = getSupportFragmentManager();

    private FirebaseDatabaseHelper mDatabaseHelper = new FirebaseDatabaseHelper();

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

        MainActivityPresenter presenterImp = new MainActivityPresenter(this);

        loginController = new FirebaseLoginController(presenterImp);

        loginController.start();

        screenProfileFragment.setHelper(mDatabaseHelper);

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
                mDatabaseHelper.setUserOffline();
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
                mFragmentReplace.replaceFragment(FRAGMENT_CHAT);
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
        alertControll.registerNetworkStateReceiver();
        alertControll.registerEventBus();
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        EventBus.getDefault().unregister(this);


    }

    public void showAlert(final int alertType) {
        if (mActiveAlerts.contains(alertType)) {
            return; //do nothing if this mAlert has already been created
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        switch (alertType) {
            case ALERT_GPS_OFF:
                //show when there is no GPS connection
                alertDialogBuilder.setMessage(R.string.gps_turned_off_alert)
                        .setPositiveButton(R.string.to_settings,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent callGPSSettingIntent = new Intent(
                                                android.provider.Settings
                                                        .ACTION_LOCATION_SOURCE_SETTINGS)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        startActivity(callGPSSettingIntent);

                                        handleLocation();
                                    }

                                });
                break;
            case ALERT_INTERNET_OFF:
                //show when there is no Internet connection
                alertDialogBuilder.setMessage(R.string.internet_turned_off_alert)
                        .setPositiveButton(R.string.turn_on_mobile_internet,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent callWirelessSettingIntent = new Intent(
// TODO: 05.02.2017 make this button start mobile internet settings
                                                Settings.ACTION_WIRELESS_SETTINGS);
                                        startActivity(callWirelessSettingIntent);
                                    }
                                });
                alertDialogBuilder.setNeutralButton(R.string.turn_on_wifi,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callWifiSettingIntent = new Intent(
                                        Settings
                                                .ACTION_WIFI_SETTINGS);
                                startActivity(callWifiSettingIntent);
                            }
                        });
                break;
            case ALERT_PERMISSION_RATIONALE:
                //show when user declines gps permission
                alertDialogBuilder.setMessage(R.string.location_rationale)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        requestLocationPermission();
                                    }
                                });
                alertDialogBuilder.setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
            case ALERT_PERMISSION_NEVER_ASK_AGAIN:
                //show when user declines gps permission and checks never ask again
                alertDialogBuilder.setMessage(R.string.how_to_change_location_setting)
                        .setCancelable(false)
                        .setPositiveButton(R.string.to_settings,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        Uri uri = Uri.fromParts(
                                                "package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                });
                alertDialogBuilder.setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
            case ALERT_SHARE_LOCATION_CONFIRMATION:
                //ask user if he really wants to share his location in chat
                alertDialogBuilder.setMessage(R.string.confirm_sharing_location_in_chat)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        EventBus.getDefault().post(new ShareLocationInChatAllowedEvent());
                                    }
                                });
                alertDialogBuilder.setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
        }

        mAlert = alertDialogBuilder.create();
        mAlert.setOnDismissListener(new DialogInterface.OnDismissListener()

                                    {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            if (mActiveAlerts.contains(alertType))
                                                mActiveAlerts.remove((Integer) alertType);
                                        }
                                    }

        );
        mAlert.show();
        if (!mActiveAlerts.contains(alertType))
            mActiveAlerts.add(alertType);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                handleLocation();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //user did not check never ask again, show rationale
                    showAlert(ALERT_PERMISSION_RATIONALE);
                } else {
                    //user checked never ask again
                    showAlert(ALERT_PERMISSION_NEVER_ASK_AGAIN);
                }
            }
        }
    }


    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_LOCATION_REQUEST_CODE);
    }

    public void handleLocation() {
        if (checkLocationPermission()) { //permission granted
            ScreenMapFragment fragment = (ScreenMapFragment)
                    fragmentManager.findFragmentByTag(FRAGMENT_MAP);
            fragment.onLocationAllowed();
        } else { //permission was not granted, show the permission prompt
            requestLocationPermission();
        }
    }

    @Subscribe
    public void onShowOnlineUserProfile(ShowUserProfile model) {

        ScreenUserProfileFragment userProfile = new ScreenUserProfileFragment();
        //userProfile.setOnlineUsersModel(model.getUserId());

        fm.beginTransaction().addToBackStack("online")
                .replace(R.id.main_activity_frame, userProfile)
                .commit();
        mDatabaseHelper.getUserModel(model.getUserId());
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
        mDatabaseHelper.addUserToFirebase(
                user.getUid(),
                user.getEmail(),
                user.getDisplayName(),
                String.valueOf(user.getPhotoUrl()));
mDatabaseHelper.getCurrentUserModel();
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }

        mNavigationBtnSignOut.setVisibility(View.VISIBLE);
        mNavigationBtnMap.setVisibility(View.VISIBLE);
        mAvatarHeader.setVisibility(View.VISIBLE);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        replaceFragment(screenMapFragment);
        mDatabaseHelper.setUserOnline("noGps");
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


        fm
                .beginTransaction()
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

    @Subscribe
    public void onOpenMapWithLatLngEvent(OpenMapWithLatLngEvent event) {
        mFragmentReplace.replaceFragment(FRAGMENT_MAP, event.getLatLng());
    }

    @Subscribe
    public void onConfirmShareLocationInChatEvent(ConfirmShareLocationInChatEvent event) {
        showAlert(ALERT_SHARE_LOCATION_CONFIRMATION);
    }
}