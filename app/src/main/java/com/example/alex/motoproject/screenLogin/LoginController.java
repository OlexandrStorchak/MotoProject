package com.example.alex.motoproject.screenLogin;


import android.support.annotation.NonNull;

import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.example.alex.motoproject.mainActivity.PresenterImp;
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

    public FirebaseAuth getmFirebaseAuth(){
        return mFirebaseAuth;
    }


    public void start() {


        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.addAuthStateListener(this);
    }

    public void stop() {
        mFirebaseAuth.removeAuthStateListener(this);
    }

    public void signOut() {
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
                    presenterImp.isLogedIn(mFirebaseCurrentUser);

                } else {
                    //User is login with email must confirm it by email
                    mFirebaseCurrentUser.sendEmailVerification();
                    //TODO: alert to check email

                    presenterImp.isLogedOut();
                }

            } else {
                // User is signed out with email

                presenterImp.isLogedOut();
            }
        } else {

            //Sign in method by Google account
            if (mFirebaseCurrentUser != null) {
                //Sign in with Google account

                presenterImp.isLogedIn(mFirebaseCurrentUser);

            } else {
                // User is signed out with Google account

                presenterImp.isLogedOut();
            }

        }


    }


}


