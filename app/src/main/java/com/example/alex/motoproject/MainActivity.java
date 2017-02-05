package com.example.alex.motoproject;

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
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.events.CancelAlertEvent;
import com.example.alex.motoproject.events.ShowAlertEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.fragments.AuthFragment;
import com.example.alex.motoproject.fragments.CheckEmailDialogFragment;
import com.example.alex.motoproject.fragments.MapFragment;
import com.example.alex.motoproject.fragments.SignUpFragment;
import com.example.alex.motoproject.fragments.UsersOnlineFragment;
import com.example.alex.motoproject.utils.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MapFragment.MapFragmentListener {

    public static final int ALERT_GPS_OFF = 20;
    public static final int ALERT_INTERNET_OFF = 21;
    public static final int ALERT_PERMISSION_RATIONALE = 22;
    public static final int ALERT_PERMISSION_NEVER_ASK_AGAIN = 23;
    public static final int PERMISSION_LOCATION_REQUEST_CODE = 10;
    public static final String FRAGMENT_MAP = "fragmentMap";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String FRAGMENT_SIGN_UP = "fragmentSignUp";
    private static final String FRAGMENT_AUTH = "fragmentAuth";
    private static final String FRAGMENT_ONLINE_USERS = "fragmentOnlineUsers";
    public static boolean loginWithEmail = false; // Flag for validate with email login method
    public static MainActivity mainActivity;
    FragmentManager mFragmentManager;
    ArrayList<Integer> mActiveAlerts = new ArrayList<>();
    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
    private NetworkStateReceiver mNetworkStateReceiver;
    private AlertDialog alert;
    //FireBase vars :
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private FirebaseDatabaseHelper databaseHelper = new FirebaseDatabaseHelper();
    private TextView mNameHeader;
    private TextView mEmailHeader;
    private ImageView mAvatarHeader;
    private FirebaseUser mFirebaseCurrentUser;
    private Button mNavigationBtnMap;
    private Button mNavigationBtnSignOut;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;


        //Firebase auth instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init FragmentManager
        mFragmentManager = getSupportFragmentManager();

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
        mNavigationBtnMap = (Button) mNavigationView.findViewById(R.id.navigatio_btn_map);
        mNavigationBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(FRAGMENT_MAP);
                mDrawerLayout.closeDrawers();
            }
        });
        //Button in Navigation Drawer for SignOut
        mNavigationBtnSignOut = (Button) mNavigationView.findViewById(R.id.navigation_btn_signout);
        mNavigationBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseHelper.removeFromOnline(mFirebaseCurrentUser.getUid());
                mFirebaseAuth.signOut();
            }
        });
        //Button in Navigation Drawer for display Friends List
        Button mNavigationBtnFriendsList = (Button) mNavigationView.findViewById(R.id.navigation_btn_friends);
        mNavigationBtnFriendsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(FRAGMENT_ONLINE_USERS);
                mDrawerLayout.closeDrawers();

            }
        });


//        // Button in Navigation Drawer, which visible when click to friends list, for back to main menu
//        Button mNavigationBtnBackToMenu = (Button) mNavigationView.findViewById(R.id.navigatio_btn_back_to_menu);
//        mNavigationBtnBackToMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                View mFriendList = findViewById(R.id.navigation_friends_layout);
//                mFriendList.setVisibility(View.GONE);
//                View mMenu = findViewById(R.id.navigation_menu_layout);
//                mMenu.setVisibility(View.VISIBLE);
//            }
//        });

        Log.d(LOG_TAG, "onCreate: Main activity ");

        //FireBase auth listener
        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseCurrentUser = firebaseAuth.getCurrentUser();
                if (loginWithEmail) {


                    //Sign in method by email
                    if (mFirebaseCurrentUser != null) {
                        if (mFirebaseCurrentUser.isEmailVerified()) {
                            // User is signed in with email
                            isSignedIn();


                        } else {
                            //User is login with email must confirm it by email
                            mFirebaseCurrentUser.sendEmailVerification();
                            showToast("Check your email!");
                            isSignedOut();
                        }

                    } else {
                        // User is signed out with email

                        isSignedOut();
                    }
                } else {

                    //Sign in method by Google account
                    if (mFirebaseCurrentUser != null) {
                        //Sign in with Google account
                        isSignedIn();

                    } else {
                        // User is signed out with Google account
                        isSignedOut();

                    }

                }
            }
        };
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            // Check the request was not cancelled
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                handleLocation();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //user checked never ask again
                    showAlert(ALERT_PERMISSION_NEVER_ASK_AGAIN);

                } else {
                    //user did not check never ask again, show rationale
                    showAlert(ALERT_PERMISSION_RATIONALE);
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        registerNetworkStateReceiver();
        EventBus.getDefault().register(this);
        // Attach the listener of Firebase Auth
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);

    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterNetworkStateReceiver();
        EventBus.getDefault().unregister(this);
        Log.d(LOG_TAG, "onStop: ");
        if (mFirebaseAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
        }
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

                fragmentTransaction.replace(R.id.main_activity_frame, UsersOnlineFragment.getInstance());
                fragmentTransaction.commit();
                break;
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void showDialog() {
        new CheckEmailDialogFragment().show(getFragmentManager(), "dialog");
    }


    private void isSignedIn() {
        String avatarUri = null;
        mNavigationBtnSignOut.setVisibility(View.VISIBLE);
        mNavigationBtnMap.setVisibility(View.VISIBLE);

        mNameHeader.setText(mFirebaseCurrentUser.getDisplayName());
        mEmailHeader.setText(mFirebaseCurrentUser.getEmail());
        mAvatarHeader.setVisibility(View.VISIBLE);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (mFirebaseCurrentUser.getPhotoUrl() != null) {
            avatarUri = mFirebaseCurrentUser.getPhotoUrl().toString();
            Picasso.with(getApplicationContext())
                    .load(avatarUri)
                    .resize(mAvatarHeader.getMaxWidth(), mAvatarHeader.getMaxHeight())
                    .centerCrop()
                    .transform(new CircleTransform())
                    .into(mAvatarHeader);

        }


        replaceFragment(FRAGMENT_MAP);
        Log.d(LOG_TAG, "isSignedIn: test");

        databaseHelper.createDatabase(mFirebaseCurrentUser.getUid(),
                mFirebaseCurrentUser.getEmail(),
                mFirebaseCurrentUser.getDisplayName());

        databaseHelper.addToOnline(mFirebaseCurrentUser.getUid(),
                mFirebaseCurrentUser.getEmail(),
                avatarUri
        );

    }

    private void isSignedOut() {


        replaceFragment(FRAGMENT_AUTH);
        mNavigationBtnSignOut.setVisibility(View.GONE);
        mNavigationBtnMap.setVisibility(View.GONE);
        mNameHeader.setText("");
        mEmailHeader.setText("");
        mAvatarHeader.setVisibility(View.INVISIBLE);
        mDrawerLayout.closeDrawers();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

    }

    @Override
    protected void onDestroy() {
        if (alert != null) {
            alert.dismiss();
        }
//        unregisterNetworkStateReceiver();
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy: ");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume: ");
    }

    public void showAlert(final int alertType) {
        if (mActiveAlerts.contains(alertType)) {
            return; //do nothing if this alert has already been created
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
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                PERMISSION_LOCATION_REQUEST_CODE);
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
        }

        alert = alertDialogBuilder.create();
        alert.setOnDismissListener(new DialogInterface.OnDismissListener()

                                   {
                                       @Override
                                       public void onDismiss(DialogInterface dialogInterface) {
                                           if (mActiveAlerts.contains(alertType))
                                               mActiveAlerts.remove((Integer) alertType);
                                       }
                                   }

        );
        alert.show();
        if (!mActiveAlerts.contains(alertType))
            mActiveAlerts.add(alertType);
    }


    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void handleLocation() {
        if (checkLocationPermission()) { //permission granted
            MapFragment fragment = (MapFragment)
                    fragmentManager.findFragmentByTag(FRAGMENT_MAP);
            fragment.onLocationAllowed();
        } else { //permission was not granted, show the permission prompt
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }
    }

    @Subscribe
    //the method called when received an event from EventBus asking for showing alert
    public void onShouldShowAlertEvent(ShowAlertEvent event) {
        int receivedAlertType = event.alertType;
        if (!mActiveAlerts.contains(receivedAlertType))
            showAlert(event.alertType);
    }


    @Subscribe
    //the method called when received an event from EventBus asking for canceling alert
    public void onShouldCancelEvent(CancelAlertEvent event) {
        int receivedAlertType = event.alertType;
        if (mActiveAlerts.contains(receivedAlertType)) {
            if (alert != null) {
                alert.dismiss();
            }
        }

    }

    private void registerNetworkStateReceiver() {
        //if LocationListenerService is on, this receiver has already been registered
        boolean isServiceOn =
                ((App) getApplication()).isLocationListenerServiceOn();
        if (!isServiceOn) {
            IntentFilter intentFilter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);

            mNetworkStateReceiver = new NetworkStateReceiver();
            registerReceiver(
                    mNetworkStateReceiver, intentFilter);
        }
    }

    private void unregisterNetworkStateReceiver() {
        boolean isServiceOn =
                ((App) getApplication()).isLocationListenerServiceOn();
        if (!isServiceOn) {
            try {
                unregisterReceiver(mNetworkStateReceiver);
            } catch (IllegalArgumentException e) {
                Log.v(LOG_TAG, "receiver was unregistered before onDestroy");
            }
//            unregisterReceiver(mNetworkStateReceiver);
        }
    }
}

