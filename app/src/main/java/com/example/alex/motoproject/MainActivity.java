package com.example.alex.motoproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import com.example.alex.motoproject.fragments.SingUpFragment;
import com.example.alex.motoproject.fragments.WelcomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "log";
    private static final String FRAGMENT_SING_UP = "fragmentSingUp";
    private static final String FRAGMENT_AUTH = "fragmentAuth";
    private static final String FRAGMENT_WELCOME = "fragmentWelcome";


    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    //FireBase vars :
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener fireBaseAuthStateListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();


        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //FireBase auth listener
        fireBaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseAuthCurrentUser = firebaseAuth.getCurrentUser();

                if (firebaseAuthCurrentUser != null & firebaseAuthCurrentUser.isEmailVerified()) {
                    // User is signed in
                    navigationView.getMenu().setGroupVisible(R.id.nav_group_main,true);

                    replaceFragment(FRAGMENT_WELCOME);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseAuthCurrentUser.getUid());
                } else {
                    navigationView.getMenu().setGroupVisible(R.id.nav_group_main,false);
                    showToast("Activate you account!");
                    // User is signed out
                    replaceFragment(FRAGMENT_AUTH);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        Log.d(TAG, "onCreate: Main activity ");
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
                //TODO fragment maps
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
            case R.id.nav_sing_out:
                firebaseAuth.signOut();
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
        firebaseAuth.addAuthStateListener(fireBaseAuthStateListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        if (fireBaseAuthStateListener != null) {
            firebaseAuth.removeAuthStateListener(fireBaseAuthStateListener);
        }
    }



    //Method for add new firebaseAuthCurrentUser into FireBase Auth, SingUp
    public void addNewUserToFireBase(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the firebaseUser. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in firebaseUser can be handled in the listener.
                        firebaseAuth.getCurrentUser().sendEmailVerification();

                        if (!task.isSuccessful()) {
                            Log.d(TAG, "onComplete: ");
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    //Sing in firebaseAuthCurrentUser into FireBase Auth
    public void singInUserToFireBase(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the firebaseUser. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in firebaseUser can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
    //get curent firebaseAuthCurrentUser information from FireBase Auth
//    public void getCurentFireBaseUser() {
//        firebaseAuthCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseAuthCurrentUser != null) {
//            // Name, email address, and profile photo Url
//            String name = firebaseAuthCurrentUser.getDisplayName();
//            String email = firebaseAuthCurrentUser.getEmail();
//            Uri photoUrl = firebaseAuthCurrentUser.getPhotoUrl();
//
//            // The firebaseUser's ID, unique to the Firebase project. Do NOT use this value to
//            // authenticate with your backend server, if you have one. Use
//            // FirebaseUser.getToken() instead.
//            String uid = firebaseAuthCurrentUser.getUid();
//        }
//    }

    // manage fragments
    public void replaceFragment(String fragmentName) {
        android.support.v4.app.FragmentTransaction fragmentTransaction
                = fragmentManager.beginTransaction();
        switch (fragmentName) {
            case FRAGMENT_SING_UP:
                SingUpFragment singUpFragment = new SingUpFragment();

                fragmentTransaction.replace(R.id.main_activity_frame, singUpFragment);
                fragmentTransaction.addToBackStack(FRAGMENT_SING_UP);
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

    public void showToast(String text){
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
}
