package com.example.alex.motoproject.screenLogin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseLoginController;
import com.example.alex.motoproject.mainActivity.LoginActivityPresenter;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.example.alex.motoproject.mainActivity.MainViewInterface;
import com.google.firebase.auth.FirebaseUser;

import static com.example.alex.motoproject.util.ArgKeys.SHOW_MAP_FRAGMENT;
import static com.example.alex.motoproject.util.ArgKeys.SIGN_OUT;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity
        implements MainViewInterface, ScreenLoginFragment.LoginActivity {

    private static final String LOGIN_FRAGMENT_TAG = "loginFragment";
    private final Handler mHideHandler = new Handler();
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    ScreenLoginFragment mLoginFragment;
    LoginActivityPresenter mPresenter = LoginActivityPresenter.getInstance(this);
    FirebaseLoginController mLoginController = new FirebaseLoginController(mPresenter);

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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void login(FirebaseUser user) {
        startActivity(new Intent(LoginActivity.this, MainActivity.class)
                .putExtra(SHOW_MAP_FRAGMENT, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
    }

    @Override
    public void logout() {

    }

//    @Override
//    public void onBackPressed() {
//        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//            getSupportFragmentManager().popBackStack();
//        } else {
//            finish();
//        }
//    }

    //Needed for Facebook handleUser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Send result to ScreenLoginFragment for Facebook auth.manager
//        screenLoginFragment.getCallbackManager().onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onSignUpButtonClick() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_login, new ScreenSignUpFragment())
                .addToBackStack(null)
                .commit();
    }
}
