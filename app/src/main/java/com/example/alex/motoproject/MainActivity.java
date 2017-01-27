package com.example.alex.motoproject;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alex.motoproject.adapters.FriendsListAdapter;
import com.example.alex.motoproject.fragments.AuthFragment;
import com.example.alex.motoproject.fragments.CheckEmailDialogFragment;
import com.example.alex.motoproject.fragments.MapFragment;
import com.example.alex.motoproject.fragments.SignUpFragment;
import com.example.alex.motoproject.models.FriendsListModel;
import com.example.alex.motoproject.utils.CircleTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FRAGMENT_SIGN_UP = "fragmentSignUp";
    private static final String FRAGMENT_AUTH = "fragmentAuth";
    private static final String FRAGMENT_MAP = "fragmentMap";
    public static boolean loginWithEmail = false; // Flag for validate with email login method
    FragmentManager mFragmentManager;
    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    //FireBase vars :
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private NavigationView mNavigationView;
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
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

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
                mFirebaseAuth.signOut();
            }
        });
        //Button in Navigation Drawer for display Friends List
        Button mNavigationBtnFriendsList = (Button) mNavigationView.findViewById(R.id.navigation_btn_friends);
        mNavigationBtnFriendsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View mFriendList = findViewById(R.id.navigation_friends_layout);
                mFriendList.setVisibility(View.VISIBLE);
                View mMenu = findViewById(R.id.navigation_menu_layout);
                mMenu.setVisibility(View.GONE);

                //TODO : Get real data from Firebase to list
                //This is dummy users
                List<FriendsListModel> list = new ArrayList<>();

                for (int i = 0; i < 10; i++) {
                    FriendsListModel user = new FriendsListModel();
                    list.add(user);
                    // Log.d("log", "onViewCreated: "+i);
                }

                FriendsListAdapter adapter = new FriendsListAdapter(list);

                RecyclerView rv = (RecyclerView) mNavigationView.findViewById(R.id.navigation_friends_list_recycler);

                rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                rv.setAdapter(adapter);
            }
        });

        // Button in Navigation Drawer, which visible when click to friends list, for back to main menu
        Button mNavigationBtnBackToMenu = (Button) mNavigationView.findViewById(R.id.navigatio_btn_back_to_menu);
        mNavigationBtnBackToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View mFriendList = findViewById(R.id.navigation_friends_layout);
                mFriendList.setVisibility(View.GONE);
                View mMenu = findViewById(R.id.navigation_menu_layout);
                mMenu.setVisibility(View.VISIBLE);
            }
        });


        Log.d(TAG, "onCreate: Main activity ");


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
                fragmentTransaction.replace(R.id.main_activity_frame, SignUpFragment.getInstance());
                fragmentTransaction.addToBackStack(FRAGMENT_SIGN_UP);
                fragmentTransaction.commit();
                break;

            case FRAGMENT_AUTH:
                fragmentTransaction.replace(R.id.main_activity_frame, new AuthFragment());
                fragmentTransaction.commit();
                break;
            case FRAGMENT_MAP:

                fragmentTransaction.replace(R.id.main_activity_frame, MapFragment.getInstance());
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
        //start MapFragment if an intent has that command
//                            if (getIntent().getExtras() != null &&
//                                    getIntent().getExtras().getBoolean("isShouldLaunchMapFragment")) {
//                                replaceFragment(FRAGMENT_MAP);
//                            }
        mNavigationBtnSignOut.setVisibility(View.VISIBLE);
        mNavigationBtnMap.setVisibility(View.VISIBLE);

        mNameHeader.setText(mFirebaseCurrentUser.getDisplayName());
        mEmailHeader.setText(mFirebaseCurrentUser.getEmail());
        mAvatarHeader.setVisibility(View.VISIBLE);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        Picasso.with(getApplicationContext())
                .load(mFirebaseCurrentUser.getPhotoUrl())
                .resize(mAvatarHeader.getMaxWidth(), mAvatarHeader.getMaxHeight())
                .centerCrop()
                .transform(new CircleTransform())
                .into(mAvatarHeader);
        replaceFragment(FRAGMENT_MAP);

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

