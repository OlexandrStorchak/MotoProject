package com.example.alex.motoproject.mainActivity;


import android.support.annotation.NonNull;

import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginController extends MainActivity implements FirebaseAuth.AuthStateListener {


    private FirebaseAuth mFirebaseAuth;
    private PresenterImp presenterImp;
    private FirebaseDatabaseHelper databaseHelper;

    public LoginController(FirebaseDatabaseHelper databaseHelper, PresenterImp presenterImp) {
        this.databaseHelper = databaseHelper;
        this.presenterImp = presenterImp;
    }


    protected void start() {
        //Firebase auth instance

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.addAuthStateListener(this);
    }

    protected void stop() {
        mFirebaseAuth.removeAuthStateListener(this);
    }

    protected void signOut() {
        mFirebaseAuth.signOut();
        LoginManager.getInstance().logOut();

    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

        FirebaseUser mFirebaseCurrentUser = firebaseAuth.getCurrentUser();
        if (loginWithEmail) {
            //Sign in method by email
            if (mFirebaseCurrentUser != null) {
                if (mFirebaseCurrentUser.isEmailVerified()) {
                    // User is signed in with email
                    //isSignedIn();
                    presenterImp.isLogedIn(mFirebaseCurrentUser);

                } else {
                    //User is login with email must confirm it by email
                    mFirebaseCurrentUser.sendEmailVerification();
                    showToast("Check your email!");
                    //isSignedOut();
                    presenterImp.isLogedOut();
                }

            } else {
                // User is signed out with email

                //isSignedOut();
                presenterImp.isLogedOut();
            }
        } else {

            //Sign in method by Google account
            if (mFirebaseCurrentUser != null) {
                //Sign in with Google account
                //isSignedIn();
                presenterImp.isLogedIn(mFirebaseCurrentUser);

            } else {
                // User is signed out with Google account
                //isSignedOut();
                presenterImp.isLogedOut();
            }

        }


    }


}


