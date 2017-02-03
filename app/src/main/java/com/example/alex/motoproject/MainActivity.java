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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.events.CancelAlertEvent;
import com.example.alex.motoproject.events.ShowAlertEvent;
import com.example.alex.motoproject.fragments.AuthFragment;
import com.example.alex.motoproject.fragments.MapFragment;
import com.example.alex.motoproject.fragments.SignUpFragment;
import com.example.alex.motoproject.fragments.WelcomeFragment;
import com.example.alex.motoproject.services.LocationListenerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import static com.example.alex.motoproject.fragments.MapFragment.LOG_TAG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MapFragment.MapFragmentListener {

    public static final int ALERT_GPS_OFF = 20;
    public static final int ALERT_INTERNET_OFF = 21;
    public static final int ALERT_PERMISSION_RATIONALE = 22;
    public static final int ALERT_PERMISSION_NEVER_ASK_AGAIN = 23;
    public static final int PERMISSION_LOCATION_REQUEST_CODE = 10;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FRAGMENT_SIGN_UP = "fragmentSignUp";
    private static final String FRAGMENT_AUTH = "fragmentAuth";
    private static final String FRAGMENT_WELCOME = "fragmentWelcome";
    private static final String FRAGMENT_MAP = "fragmentMap";
    private static final String FRAGMENT_MAP_TAG = FRAGMENT_MAP;
    public static boolean loginWithEmail = false; // Flag for validate with email login method
    FragmentManager mFragmentManager;
    ArrayList<Integer> mActiveAlerts = new ArrayList<>();
    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
    private NetworkStateReceiver mNetworkStateReceiver;
    private AlertDialog alert;
    //FireBase vars :
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Firebase auth instance
        mFirebaseAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init FragmentManager
        mFragmentManager = getSupportFragmentManager();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();


        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        registerNetworkStateReceiver();

        Log.d(TAG, "onCreate: Main activity ");

        //FireBase auth listener
        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseAuthCurrentUser = firebaseAuth.getCurrentUser();
                if (loginWithEmail) {
                    if (firebaseAuthCurrentUser != null) {
                        if (firebaseAuthCurrentUser.isEmailVerified()) {
                            // User is signed in
                            //start MapFragment if an intent has that command
                            if (getIntent().getExtras() != null &&
                                    getIntent().getExtras().getBoolean("isShouldLaunchMapFragment")) {
                                replaceFragment(FRAGMENT_MAP);
                            }
                            navigationView.getMenu().setGroupVisible(R.id.nav_group_main, true);
//                            replaceFragment(FRAGMENT_WELCOME);
                            replaceFragment(FRAGMENT_MAP);
                        } else {
                            navigationView.getMenu().setGroupVisible(R.id.nav_group_main, false);
                            firebaseAuthCurrentUser.sendEmailVerification();
                            showToast("Check your email!");
                            firebaseAuth.signOut();
                        }

                    } else {
                        // User is signed out
                        navigationView.getMenu().setGroupVisible(R.id.nav_group_main, false);
                        replaceFragment(FRAGMENT_AUTH);
                    }
                } else {
                    if (firebaseAuthCurrentUser != null) {
                        // User is signed in
                        navigationView.getMenu().setGroupVisible(R.id.nav_group_main, true);
                        replaceFragment(FRAGMENT_MAP);
                    } else {
                        // User is signed out
                        navigationView.getMenu().setGroupVisible(R.id.nav_group_main, false);
                        replaceFragment(FRAGMENT_AUTH);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        return super.onOptionsItemSelected(item);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_maps:
                replaceFragment(FRAGMENT_MAP);
                break;
            case R.id.nav_friends:
                //TODO fragment friends list
                break;
            case R.id.nav_chat:
                //TODO fragment public chat
                break;
            case R.id.nav_events:
                //TODO fragment all events
                break;
            case R.id.nav_info:
                //TODO fragment info of stores, repairs and helpers
                break;
            case R.id.nav_sign_out:
                mFirebaseAuth.signOut();
                stopService(
                        new Intent(this, LocationListenerService.class));
                break;

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        Log.d(TAG, "onStop: ");
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
                SignUpFragment signUpFragment = new SignUpFragment();

                fragmentTransaction.replace(R.id.main_activity_frame, signUpFragment);
                fragmentTransaction.addToBackStack(FRAGMENT_SIGN_UP);
                fragmentTransaction.commit();
                break;
            case FRAGMENT_WELCOME:
                WelcomeFragment welcomeFragment = new WelcomeFragment();

                fragmentTransaction.replace(R.id.main_activity_frame, welcomeFragment);
                fragmentTransaction.commit();
                break;
            case FRAGMENT_AUTH:
                AuthFragment authFragment = new AuthFragment();

                fragmentTransaction.replace(R.id.main_activity_frame, authFragment);
                fragmentTransaction.commit();
                break;
            case FRAGMENT_MAP:
                fragmentTransaction.replace(R.id.main_activity_frame,
                        MapFragment.getInstance(),
                        FRAGMENT_MAP_TAG);
                fragmentTransaction.commit();
                break;
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (alert != null) {
            alert.dismiss();
        }
//        unregisterNetworkStateReceiver();
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
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

                                        if (checkLocationPermission()) {
                                            MapFragment fragment = (MapFragment)
                                                    fragmentManager.findFragmentByTag(FRAGMENT_MAP_TAG);
                                            fragment.onLocationAllowed();
                                        }

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
                                                Settings.ACTION_WIRELESS_SETTINGS);
                                        startActivity(callWirelessSettingIntent);
                                    }
                                });
                alertDialogBuilder.setPositiveButton(R.string.turn_on_wifi,
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
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (mActiveAlerts.contains(alertType))
                    mActiveAlerts.remove((Integer) alertType);
            }
        });
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
                    fragmentManager.findFragmentByTag(FRAGMENT_MAP_TAG);
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
        }
    }
}
