package com.example.alex.motoproject;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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
import android.widget.Toast;


import com.example.alex.motoproject.fragments.AuthFragment;
import com.example.alex.motoproject.fragments.MapFragment;
import com.example.alex.motoproject.fragments.SignUpFragment;
import com.example.alex.motoproject.fragments.WelcomeFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FRAGMENT_SIGN_UP = "fragmentSignUp";
    private static final String FRAGMENT_AUTH = "fragmentAuth";
    private static final String FRAGMENT_WELCOME = "fragmentWelcome";
    private static final String FRAGMENT_MAP = "fragmentMap";
    public static boolean loginWithEmail = false; // Flag for validate with email login method
    FragmentManager mFragmentManager;
    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    //FireBase vars :
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private NavigationView navigationView;
    private TextView nameHeader;
    private TextView emailHeader;
    private ImageView avatarHeader;
    private FirebaseUser firebaseAuthCurrentUser;
    private Button navigationBtnMap;
    private Button navigationBtnSignOut;
    private DrawerLayout drawer;


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


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        navigationView = (NavigationView) findViewById(R.id.nav_view);

        final View header = navigationView.getHeaderView(0);
        nameHeader = (TextView) header.findViewById(R.id.header_name);
        emailHeader = (TextView) header.findViewById(R.id.header_email);
        avatarHeader = (ImageView) header.findViewById(R.id.header_avatar);



        navigationBtnMap = (Button) navigationView.findViewById(R.id.navigatio_btn_map);
        navigationBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(FRAGMENT_MAP);
            }
        });
        navigationBtnSignOut = (Button) navigationView.findViewById(R.id.navigation_btn_signout);
        navigationBtnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseAuth.signOut();
            }
        });

        Log.d(TAG, "onCreate: Main activity ");


        //FireBase auth listener
        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseAuthCurrentUser = firebaseAuth.getCurrentUser();
                if (loginWithEmail) {
                    //Sign in method by email
                    if (firebaseAuthCurrentUser != null) {
                        if (firebaseAuthCurrentUser.isEmailVerified()) {
                            // User is signed in with email
                            isSignedIn();

                        } else {
                            //User is login with email must confirm it by email
                            firebaseAuthCurrentUser.sendEmailVerification();
                            showToast("Check your email!");
                            isSignedOut();
                        }

                    } else {
                        // User is signed out with email
                        isSignedOut();
                    }
                } else {
                    //Sign in method by Google account
                    if (firebaseAuthCurrentUser != null) {
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

                fragmentTransaction.replace(R.id.main_activity_frame, MapFragment.getInstance());
                fragmentTransaction.commit();
                break;
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void isSignedIn() {
        //start MapFragment if an intent has that command
//                            if (getIntent().getExtras() != null &&
//                                    getIntent().getExtras().getBoolean("isShouldLaunchMapFragment")) {
//                                replaceFragment(FRAGMENT_MAP);
//                            }
        navigationBtnSignOut.setVisibility(View.VISIBLE);
        navigationBtnMap.setVisibility(View.VISIBLE);

        nameHeader.setText(firebaseAuthCurrentUser.getDisplayName());
        emailHeader.setText(firebaseAuthCurrentUser.getEmail());
        avatarHeader.setVisibility(View.VISIBLE);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        Picasso.with(getApplicationContext())
                .load(firebaseAuthCurrentUser.getPhotoUrl())
                .resize(avatarHeader.getMaxWidth(),avatarHeader.getMaxHeight())
                .centerCrop()
                .transform(new CircleTransform())
                .into(avatarHeader);
        replaceFragment(FRAGMENT_MAP);
    }

    private void isSignedOut() {


        replaceFragment(FRAGMENT_AUTH);
        navigationBtnSignOut.setVisibility(View.GONE);
        navigationBtnMap.setVisibility(View.GONE);
        nameHeader.setText("");
        emailHeader.setText("");
        avatarHeader.setVisibility(View.INVISIBLE);
        drawer.closeDrawers();
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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


    public class CircleTransform implements Transformation {

            @Override
            public Bitmap transform(Bitmap source) {
                int size = Math.min(source.getWidth(), source.getHeight());

                int x = (source.getWidth() - size) / 2;
                int y = (source.getHeight() - size) / 2;

                Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
                if (squaredBitmap != source) {
                    source.recycle();
                }

                Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
                paint.setShader(shader);
                paint.setAntiAlias(true);

                float r = size/2f;
                canvas.drawCircle(r, r, r, paint);

                squaredBitmap.recycle();
                return bitmap;
            }

            @Override
            public String key() {
                return "circle";
            }
        }
    }

