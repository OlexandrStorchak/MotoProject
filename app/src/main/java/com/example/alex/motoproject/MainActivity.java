package com.example.alex.motoproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.alex.motoproject.fragments.AuthFragment;
import com.example.alex.motoproject.fragments.MapFragment;
import com.example.alex.motoproject.fragments.SignUpFragment;
import com.example.alex.motoproject.fragments.WelcomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FRAGMENT_SIGN_UP = "fragmentSignUp";
    private static final String FRAGMENT_AUTH = "fragmentAuth";
    private static final String FRAGMENT_WELCOME = "fragmentWelcome";
    public static boolean loginWithEmail = false; // Flag for validate with email login method
    FragmentManager mFragmentManager;
    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

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

//        showMapFragment();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();


        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //start MapFragment if an intent has that command
        if (getIntent().getExtras() != null &&
                getIntent().getExtras().getBoolean("isShouldLaunchMapFragment")) {
            showMapFragment();
        }
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
                            navigationView.getMenu().setGroupVisible(R.id.nav_group_main, true);
//                            replaceFragment(FRAGMENT_WELCOME);
                            showMapFragment();
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
//                        replaceFragment(FRAGMENT_WELCOME);
                        showMapFragment();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_maps:
                showMapFragment();
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
                break;

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Attach the listener of Firebase Auth
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);

    }


    @Override
    protected void onStop() {
        super.onStop();
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
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
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

    private void showMapFragment() {
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        MapFragment mMapFragment = new MapFragment();
        mFragmentTransaction.replace(R.id.container, mMapFragment);
        mFragmentTransaction.commit();
    }
}
