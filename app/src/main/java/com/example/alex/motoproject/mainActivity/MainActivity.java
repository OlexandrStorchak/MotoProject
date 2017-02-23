package com.example.alex.motoproject.mainActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.events.CancelAlertEvent;
import com.example.alex.motoproject.events.ConfirmShareLocationInChatEvent;
import com.example.alex.motoproject.events.OpenMapWithLatLngEvent;
import com.example.alex.motoproject.events.ShareLocationInChatAllowedEvent;
import com.example.alex.motoproject.events.ShowAlertEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.screenLogin.ScreenLoginController;
import com.example.alex.motoproject.screenLogin.ScreenLoginFragment;
import com.example.alex.motoproject.screenMap.ScreenMapFragment;
import com.example.alex.motoproject.services.LocationListenerService;
import com.example.alex.motoproject.utils.CircleTransform;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_AUTH;
import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_CHAT;
import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_MAP;
import static com.example.alex.motoproject.mainActivity.ManageFragmentContract.FRAGMENT_ONLINE_USERS;

public class MainActivity extends AppCompatActivity implements ScreenMapFragment.MapFragmentListener, MainViewInterface {

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
    private FirebaseDatabaseHelper mDatabaseHelper = new FirebaseDatabaseHelper();
    private ManageFragment mFragmentReplace;
    private ScreenLoginController loginController;
    private android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        MainActivityPresenter presenterImp = new MainActivityPresenter(this);

        mFragmentReplace = new ManageFragment(getSupportFragmentManager());

        loginController = new ScreenLoginController(presenterImp);

        loginController.start();

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

        //Button in Navigation Drawer for show the Map fragment
        mNavigationBtnMap = (Button) mNavigationView.findViewById(R.id.navigation_btn_map);
        mNavigationBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFragmentReplace.replaceFragment(FRAGMENT_MAP);
                mDrawerLayout.closeDrawers();
                ScreenMapFragment.getInstance().onMapCk();
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
                mFragmentReplace.replaceFragment(FRAGMENT_ONLINE_USERS);
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
        registerNetworkStateReceiver();
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterNetworkStateReceiver();
        EventBus.getDefault().unregister(this);
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
        if (mAlert != null) {
            mAlert.dismiss();
        }

        if (!isServiceOn()) {
            logout();
        }
        super.onDestroy();


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
    //the method called when received an event from EventBus asking for showing mAlert
    public void onShouldShowAlertEvent(ShowAlertEvent event) {
        int receivedAlertType = event.alertType;
        if (!mActiveAlerts.contains(receivedAlertType))
            showAlert(event.alertType);
    }


    @Subscribe
    //the method called when received an event from EventBus asking for canceling mAlert
    public void onShouldCancelEvent(CancelAlertEvent event) {
        int receivedAlertType = event.alertType;
        if (mActiveAlerts.contains(receivedAlertType)) {
            if (mAlert != null) {
                mAlert.dismiss();
            }
        }

    }

    private void registerNetworkStateReceiver() {
        //if LocationListenerService is on, this receiver has already been registered
        if (!isServiceOn()) {
            IntentFilter intentFilter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);

            mNetworkStateReceiver = new NetworkStateReceiver();
            registerReceiver(
                    mNetworkStateReceiver, intentFilter);
        }
    }

    private void unregisterNetworkStateReceiver() {
        if (!isServiceOn()) {
            try {
                unregisterReceiver(mNetworkStateReceiver);
            } catch (IllegalArgumentException e) {
                Log.v(LOG_TAG, "receiver was unregistered before onStop");
            }
        }
    }

    private boolean isServiceOn() {
        return ((App) getApplication()).isLocationListenerServiceOn();
    }


    @Override

    public void login(FirebaseUser user) {
        // TODO: 11.02.2017 let users choose avatars
        String avatarUri = "https://firebasestorage.googleapis.com/v0/b/profiletests-d3a61.appspot.com/o/ava4.png?alt=media&token=96951c00-fd27-445c-85a6-b636bd0cb9f5";
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

        mFragmentReplace.replaceFragment(FRAGMENT_MAP);
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
        mFragmentReplace.replaceFragment(FRAGMENT_AUTH);
        mNavigationBtnSignOut.setVisibility(View.GONE);
        mNavigationBtnMap.setVisibility(View.GONE);
        mNameHeader.setText("");
        mEmailHeader.setText("");
        mAvatarHeader.setVisibility(View.INVISIBLE);
        mDrawerLayout.closeDrawers();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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