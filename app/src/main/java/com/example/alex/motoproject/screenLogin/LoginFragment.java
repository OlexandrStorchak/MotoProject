package com.example.alex.motoproject.screenLogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.alex.motoproject.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Collection;

import static android.app.Activity.RESULT_OK;
import static com.example.alex.motoproject.firebase.FirebaseLoginController.mLoginWithEmail;
import static com.example.alex.motoproject.util.ArgKeys.EMAIL;
import static com.example.alex.motoproject.util.ArgKeys.EMAIL_AND_PASSWORD_DISPLAYED;
import static com.example.alex.motoproject.util.ArgKeys.PASSWORD;


public class LoginFragment extends Fragment {

    private final int GOOGLE_SIGN_IN = 13;

    private EditText mEmail, mPassword;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFireBaseAuth;
    private GoogleApiClient mGoogleApiClient;
    private boolean firstStart = true;
    private Button mButtonSignInGoogle;
    private Button mButtonSignUp;
    private Button mButtonSubmit;
    private LoginManager loginManager = LoginManager.getInstance();
    private CallbackManager callbackManager = CallbackManager.Factory.create();
    private Button mButtonSignInFacebook;

    private boolean mEmailAndPasswordFieldsDisplayed;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
                mButtonSignUp.setVisibility(View.VISIBLE);
                mButtonSignInGoogle.setVisibility(View.VISIBLE);
                mButtonSubmit.setVisibility(View.VISIBLE);
                mButtonSignInFacebook.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                mButtonSignUp.setVisibility(View.VISIBLE);
                mButtonSignInGoogle.setVisibility(View.VISIBLE);
                mButtonSubmit.setVisibility(View.VISIBLE);
                mButtonSignInFacebook.setVisibility(View.VISIBLE);

            }
        });

        mFireBaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        mEmail.setText(savedInstanceState.getString(EMAIL));
        mPassword.setText(savedInstanceState.getString(PASSWORD));

        if (savedInstanceState.getBoolean(EMAIL_AND_PASSWORD_DISPLAYED)) {
            mEmailAndPasswordFieldsDisplayed = true;
            mEmail.setVisibility(View.VISIBLE);
            mPassword.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EMAIL, mEmail.getText().toString());
        outState.putString(PASSWORD, mPassword.getText().toString());
        outState.putBoolean(EMAIL_AND_PASSWORD_DISPLAYED, mEmailAndPasswordFieldsDisplayed);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmail = (EditText) view.findViewById(R.id.auth_email);
        mPassword = (EditText) view.findViewById(R.id.auth_pass);
        mProgressBar = (ProgressBar) view.findViewById(R.id.auth_progress_bar);
        mButtonSubmit = (Button) view.findViewById(R.id.auth_btn_ok);

        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginWithEmail = true;
                if (firstStart) {
                    mEmailAndPasswordFieldsDisplayed = true;
                    mEmail.setVisibility(View.VISIBLE);
                    mPassword.setVisibility(View.VISIBLE);
                    setClearEditText();
                    firstStart = false;
                } else {
                    mEmailAndPasswordFieldsDisplayed = false;
                    if (mEmail.getText().length() == 0) {
                        mEmail.setError(getResources().getString(R.string.email_is_empty));

                    }
                    if (mPassword.getText().length() == 0) {
                        mPassword.setError(getResources().getString(R.string.enter_password));
                    } else if (mPassword.getText().length() <= 5) {
                        mPassword.setError(getResources().getString(R.string.less_6_chars));
                    }
                    if (mPassword.getText().length() > 5 & mEmail.getText().length() > 0) {
                        mLoginWithEmail = true;
                        mEmail.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        mPassword.setVisibility(View.GONE);
                        signInUserToFireBase(
                                mEmail.getText().toString(), mPassword.getText().toString());

                    }
                }

            }
        });

        mButtonSignUp = (Button) view.findViewById(R.id.auth_btn_sign_in);
        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoginActivityInterface) getActivity()).onSignUpButtonClick();
            }
        });

        mButtonSignInGoogle = (Button) view.findViewById(R.id.auth_btn_google_sign_in);
        mButtonSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.INVISIBLE);
                mLoginWithEmail = false;
                signInGoogle();
                mEmail.setVisibility(View.GONE);
                mPassword.setVisibility(View.GONE);
                setButtonsVisible(false);
//                mButtonSignUp.setVisibility(View.GONE);
//                mButtonSubmit.setVisibility(View.GONE);
//                mButtonSignInGoogle.setVisibility(View.GONE);
//                mButtonSignInFacebook.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });


        mButtonSignInFacebook = (Button) view.findViewById(R.id.auth_btn_facebook_sign_in);

        mButtonSignInFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginWithEmail = false;

                Collection<String> permissions = Arrays.asList("public_profile", "email");

                loginManager.logInWithReadPermissions(LoginFragment.this, permissions);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        firstStart = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mFireBaseAuth = null;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    private void setButtonsVisible(boolean visible) {
        int visibility;
        if (visible) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }

//        mButtonSignInFacebook.setEnabled(enabled);
//        mButtonSignInGoogle.setEnabled(enabled);
//        mButtonSignUp.setEnabled(enabled);
//        mButtonSubmit.setEnabled(enabled);
        mButtonSignInGoogle.setVisibility(visibility);
        mButtonSubmit.setVisibility(visibility);
        mButtonSignUp.setVisibility(visibility);
        mButtonSignInFacebook.setVisibility(visibility);
    }

    private void hideKeyboard() {
        View view = getView();
        if (view == null) return;

        InputMethodManager inputMethodManager =
                (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showErrorSnackbar() {
        if (getView() == null) return;
        Snackbar.make(getView(), R.string.auth_error, Snackbar.LENGTH_LONG).show();
    }

    private void handlePossibleAuthError(Task<AuthResult> task) {
        if (!task.isSuccessful()) {
            showErrorSnackbar();
            mProgressBar.setVisibility(View.GONE);
            setButtonsVisible(true);
        }
    }

    //Sign in firebaseAuthCurrentUser into FireBase Auth
    public void signInUserToFireBase(String email, String password) {
        setButtonsVisible(false);
        hideKeyboard();
        mFireBaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //If sign in fails, display a message to the firebaseUser. Else
                        //the auth state listener will be notified and the AuthListener
                        //will be notified
                        if (!task.isSuccessful()) {
                            showErrorSnackbar();
                            mProgressBar.setVisibility(View.GONE);
                            setButtonsVisible(true);
                            firstStart = true;
                        }
                    }
                });
    }

    //Sign in with Google
    public void signInGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage((FragmentActivity) getContext(),
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                            }
                        } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        setButtonsVisible(false);
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        handlePossibleAuthError(task);
                    }
                });
    }

    public void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        handlePossibleAuthError(task);
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (resultCode == RESULT_OK) {
                if (result.isSuccess()) {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);
                }


            } else {
                mProgressBar.setVisibility(View.GONE);
                setButtonsVisible(true);
                firstStart = true;
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.stopAutoManage(getActivity());
                    mGoogleApiClient.disconnect();
                    showErrorSnackbar();

                }
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void setClearEditText() {
        mEmail.setText("");
        mPassword.setText("");
    }

    public interface LoginActivityInterface {
        void onSignUpButtonClick();
    }
}
