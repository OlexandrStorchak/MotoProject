package com.example.alex.motoproject.screenLogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseLoginController;
import com.example.alex.motoproject.mainActivity.MainActivity;

import static com.example.alex.motoproject.util.ArgKeys.SHOW_MAP_FRAGMENT;
import static com.example.alex.motoproject.util.ArgKeys.SIGN_OUT;

public class LoginActivity extends AppCompatActivity
        implements ScreenLoginFragment.LoginActivityInterface {

    private static final String LOGIN_FRAGMENT_TAG = "loginFragment";

    ScreenLoginFragment mLoginFragment;
    FirebaseLoginController mLoginController = new FirebaseLoginController(this);

    @Override
    protected void onStop() {
        super.onStop();
        mLoginController.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLoginController.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (getIntent().getBooleanExtra(SIGN_OUT, false)) {
            mLoginController.signOut();
        }

        mLoginFragment = (ScreenLoginFragment)
                getSupportFragmentManager().findFragmentByTag(LOGIN_FRAGMENT_TAG);
        if (mLoginFragment == null) {
            mLoginFragment = new ScreenLoginFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_login, mLoginFragment, LOGIN_FRAGMENT_TAG)
                .commit();
    }

    public void login() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class)
                .putExtra(SHOW_MAP_FRAGMENT, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }

    @Override
    public void onSignUpButtonClick() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_login, new ScreenSignUpFragment())
                .addToBackStack(null)
                .commit();
    }
}
