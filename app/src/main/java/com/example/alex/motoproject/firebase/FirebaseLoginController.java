package com.example.alex.motoproject.firebase;

import android.support.annotation.NonNull;

import com.example.alex.motoproject.screenLogin.LoginActivity;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

public class FirebaseLoginController implements FirebaseAuth.AuthStateListener {
    // Flag to validate with email handleUser method
    public static boolean mLoginWithEmail;

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    private WeakReference<LoginActivity> mLoginActivity;
    private FirebaseAuth mFirebaseAuth;

    public FirebaseLoginController(LoginActivity loginActivity) {
        mLoginActivity = new WeakReference<>(loginActivity);
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

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
        if (mLoginWithEmail) {
            //Sign in method by email
            if (currentUser != null) {
                if (currentUser.isEmailVerified()) {
                    // User is signed in with email
                    LoginActivity loginActivity = mLoginActivity.get();
                    if (loginActivity != null) {
                        loginActivity.login();
                    }
                } else {
                    //User is handleUser with email. Must confirm by email
                    currentUser.sendEmailVerification();
                    //TODO: alert to check email
                }
            }
        } else {
            //Sign in method by Google account
            if (currentUser != null) {
                //Sign in with Google account
                LoginActivity loginActivity = mLoginActivity.get();
                if (loginActivity != null) {
                    loginActivity.login();
                }
            }
        }
    }
}


