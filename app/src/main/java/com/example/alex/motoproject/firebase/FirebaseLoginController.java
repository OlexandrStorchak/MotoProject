package com.example.alex.motoproject.firebase;


import android.support.annotation.NonNull;

import com.example.alex.motoproject.mainActivity.LoginActivityPresenter;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

public class FirebaseLoginController implements FirebaseAuth.AuthStateListener {
    // Flag for validate with email handleUser method
    public static boolean loginWithEmail = false;
    private static LoginActivityPresenter loginActivityPresenter;
    private static FirebaseLoginController controller;
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private FirebaseAuth mFirebaseAuth;

    public FirebaseLoginController(LoginActivityPresenter presenter) {
        loginActivityPresenter = presenter;
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

//    public static FirebaseLoginController getInstance(MainActivityPresenter presenter) {
//        mainActivityPresenter = presenter;
//        if (controller == null) {
//            controller = new FirebaseLoginController(presenter);
//        }
//        return controller;
//    }

    public void start() {
        mFirebaseAuth.addAuthStateListener(this);
    }

    public void stop() {
        mFirebaseAuth.removeAuthStateListener(this);
    }

    public void signOut() {
        //For Firebase logout
        mFirebaseAuth.signOut();
        //For Facebook logout
        LoginManager.getInstance().logOut();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (loginWithEmail) {
            //Sign in method by email
            if (currentUser != null) {
                if (currentUser.isEmailVerified()) {
                    // User is signed in with email
                    loginActivityPresenter.onLogin(currentUser);
                } else {
                    //User is handleUser with email. Must confirm by email
                    currentUser.sendEmailVerification();
                    //TODO: alert to check email
                    loginActivityPresenter.onLogout();
                }
            } else {
                // User is signed out with email
                loginActivityPresenter.onLogout();
            }
        } else {
            //Sign in method by Google account
            if (currentUser != null) {
                //Sign in with Google account
                loginActivityPresenter.onLogin(currentUser);
            } else {
                // User is signed out with Google account
                loginActivityPresenter.onLogout();
            }
        }
    }

}


