package com.example.alex.motoproject.firebase;


import android.support.annotation.NonNull;

import com.example.alex.motoproject.mainActivity.MainActivity;
import com.example.alex.motoproject.mainActivity.MainActivityPresenter;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseLoginController extends MainActivity implements FirebaseAuth.AuthStateListener {
    public static boolean loginWithEmail = false; // Flag for validate with email login method


    private FirebaseAuth mFirebaseAuth;
    private MainActivityPresenter mainActivityPresenter;


    public FirebaseLoginController(MainActivityPresenter mainActivityPresenter) {
        this.mainActivityPresenter = mainActivityPresenter;
    }


    public void start() {
        mFirebaseAuth = FirebaseAuth.getInstance();

        mFirebaseAuth.addAuthStateListener(this);


    }

    public void stop() {
        mFirebaseAuth.removeAuthStateListener(this);
    }

    public void signOut() {
        new FirebaseDatabaseHelper().setUserOfflineOnDisconnect();
        //For Firebase logout
        mFirebaseAuth.signOut();
        //For Facebook logout
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
                    mainActivityPresenter.onLogin(mFirebaseCurrentUser);

                } else {
                    //User is login with email. Must confirm by email
                    mFirebaseCurrentUser.sendEmailVerification();
                    //TODO: alert to check email

                    mainActivityPresenter.onLogout();
                }

            } else {
                // User is signed out with email
                mainActivityPresenter.onLogout();
            }
        } else {

            //Sign in method by Google account
            if (mFirebaseCurrentUser != null) {
                //Sign in with Google account
                mainActivityPresenter.onLogin(mFirebaseCurrentUser);


            } else {
                // User is signed out with Google account
                mainActivityPresenter.onLogout();
            }
        }
    }


}


