package com.example.alex.motoproject.screenLogin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.alex.motoproject.firebase.FirebaseLoginController.loginWithEmail;


public class ScreenLoginFragment extends Fragment {

    private final int GOOGLE_SIGN_IN = 13;
    private String TAG = "log";
    private EditText mEmail, mPassword;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFireBaseAuth;
    private GoogleApiClient mGoogleApiClient = null;
    private boolean firstStart = true;
    private Button mButtonSignInGoogle;
    private Button mButtonSignUp;
    private Button mButtonSubmit;
    private LoginManager loginManager = LoginManager.getInstance();
    private CallbackManager callbackManager = CallbackManager.Factory.create();
    private Button mButtonSignInFacebook;

    public ScreenLoginFragment() {
        // Required empty public constructor
    }


    public CallbackManager getCallbackManager() {
        return callbackManager;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d(TAG, "onSuccess: FACEBOOK");
                final GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code

                                Log.v("LoginActivity", response.toString());
                            }
                        });
                Log.d(TAG, "onCompleted: " + request.toString());

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
                mButtonSignUp.setVisibility(View.VISIBLE);
                mButtonSignInGoogle.setVisibility(View.VISIBLE);
                mButtonSubmit.setVisibility(View.VISIBLE);
                mButtonSignInFacebook.setVisibility(View.VISIBLE);
                Log.d(TAG, "onCancel: FACEBOOK");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                mButtonSignUp.setVisibility(View.VISIBLE);
                mButtonSignInGoogle.setVisibility(View.VISIBLE);
                mButtonSubmit.setVisibility(View.VISIBLE);
                mButtonSignInFacebook.setVisibility(View.VISIBLE);
                Log.d(TAG, "onError: FACEBOOK");
            }
        });


        mFireBaseAuth = FirebaseAuth.getInstance();
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


        Log.d(TAG, "onViewCreated: test");
        mEmail = (EditText) view.findViewById(R.id.auth_email);
        mPassword = (EditText) view.findViewById(R.id.auth_pass);

        mProgressBar = (ProgressBar) view.findViewById(R.id.auth_progress_bar);

        mButtonSubmit = (Button) view.findViewById(R.id.auth_btn_ok);


        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithEmail = true;
                if (firstStart) {

                    mEmail.setVisibility(View.VISIBLE);
                    mPassword.setVisibility(View.VISIBLE);
                    setClearEditText();
                    firstStart = false;
                } else {
                    if (mEmail.getText().length() == 0) {
                        mEmail.setError(getResources().getString(R.string.email_is_empty));
                        Log.d(TAG, "onClick: emaeil is empty");
                    }
                    if (mPassword.getText().length() == 0) {
                        mPassword.setError(getResources().getString(R.string.enter_password));
                    } else if (mPassword.getText().length() <= 5) {
                        mPassword.setError(getResources().getString(R.string.less_6_chars));
                    }
                    if (mPassword.getText().length() > 5 & mEmail.getText().length() > 0) {
                        loginWithEmail = true;
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
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_activity_frame, new ScreenSignUpFragment())
                        .addToBackStack("signUp").commit();
            }
        });

        mButtonSignInGoogle = (Button) view.findViewById(R.id.auth_btn_google_sign_in);
        mButtonSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.INVISIBLE);
                loginWithEmail = false;
                signInGoogle();
                mEmail.setVisibility(View.GONE);
                mPassword.setVisibility(View.GONE);
                mButtonSignUp.setVisibility(View.GONE);
                mButtonSubmit.setVisibility(View.GONE);
                mButtonSignInGoogle.setVisibility(View.GONE);
                mButtonSignInFacebook.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });


        mButtonSignInFacebook = (Button) view.findViewById(R.id.auth_btn_facebook_sign_in);

        mButtonSignInFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithEmail = false;
                mButtonSignUp.setVisibility(View.INVISIBLE);
                mButtonSignInGoogle.setVisibility(View.INVISIBLE);
                mButtonSubmit.setVisibility(View.INVISIBLE);
                mButtonSignInFacebook.setVisibility(View.INVISIBLE);
                mEmail.setVisibility(View.GONE);
                mPassword.setVisibility(View.GONE);

                Collection<String> permissions = Arrays.asList("public_profile", "email");

                loginManager.logInWithReadPermissions(getActivity(), permissions);
            }
        });


    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

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
        Log.d(TAG, "onDestroy: ");
        mFireBaseAuth = null;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    //Sign in firebaseAuthCurrentUser into FireBase Auth
    public void signInUserToFireBase(String email, String password) {
        mFireBaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(((MainActivity) getContext()), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the firebaseUser. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in firebaseUser can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());

                            mProgressBar.setVisibility(View.GONE);
                            firstStart = true;
                        }

                        // ...
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
                .enableAutoManage((FragmentActivity) getContext()
                        , new GoogleApiClient.OnConnectionFailedListener() {
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
        Log.d(TAG, "firebaseAuthWithGoogle:");


        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(((MainActivity) getContext()), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());

                        }

                    }
                });
    }

    public void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener((MainActivity) getContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());

                        }


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
                } else {
                    Log.d(TAG, "onActivityResult: ");
                }
            } else if (resultCode == RESULT_CANCELED) {

                mProgressBar.setVisibility(View.GONE);
                firstStart = true;
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.stopAutoManage(getActivity());
                    mGoogleApiClient.disconnect();
                    mButtonSignUp.setVisibility(View.VISIBLE);
                    mButtonSubmit.setVisibility(View.VISIBLE);
                    mButtonSignInFacebook.setVisibility(View.VISIBLE);
                    mButtonSignInGoogle.setVisibility(View.VISIBLE);
                    mButtonSignInGoogle.setClickable(true);
                }
            } else {

                mProgressBar.setVisibility(View.GONE);
                firstStart = true;
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.stopAutoManage(getActivity());
                    mGoogleApiClient.disconnect();
                    mButtonSignUp.setVisibility(View.VISIBLE);
                    mButtonSubmit.setVisibility(View.VISIBLE);
                    mButtonSignInFacebook.setVisibility(View.VISIBLE);
                    mButtonSignInGoogle.setVisibility(View.VISIBLE);
                    mButtonSignInGoogle.setClickable(true);
                }
            }
        } else {
            Log.d(TAG, "onActivityResult: facebook");
            callbackManager.onActivityResult(requestCode, resultCode, data);

        }
    }


    public void setClearEditText() {
        mEmail.setText("");
        mPassword.setText("");
    }
}
