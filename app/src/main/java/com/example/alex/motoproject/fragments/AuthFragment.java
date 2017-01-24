package com.example.alex.motoproject.fragments;

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
import android.widget.TextView;

import com.example.alex.motoproject.MainActivity;
import com.example.alex.motoproject.R;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.example.alex.motoproject.MainActivity.loginWithEmail;


public class AuthFragment extends Fragment {
    private static final int GOOGLE_SIGN_IN = 13;
    private EditText mEmail, mPassword;
    private TextView mEmailHint, mPassHint;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFireBaseAuth;
    private GoogleApiClient mGoogleApiClient = null;
    private boolean firstStart = true;

    private static final String TAG = "log";

    public AuthFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mEmailHint = (TextView) view.findViewById(R.id.auth_email_hint);
        mPassHint = (TextView) view.findViewById(R.id.auth_pass_hint);
        mProgressBar = (ProgressBar) view.findViewById(R.id.auth_progress_bar);

        Button mButtonSubmit = (Button) view.findViewById(R.id.auth_btn_ok);


        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithEmail=true;
                if (firstStart) {
                    mEmail.setVisibility(View.VISIBLE);
                    mPassword.setVisibility(View.VISIBLE);
                    firstStart = false;
                } else {
                    if (mEmail.getText().length() == 0) {
                        mEmailHint.setVisibility(View.VISIBLE);
                        mEmailHint.setText(R.string.email_is_empty);
                        Log.d(TAG, "onClick: emaeil is empty");
                    }
                    if (mPassword.getText().length() == 0) {
                        //mEmailHint.setVisibility(View.INVISIBLE);
                        mPassHint.setVisibility(View.VISIBLE);
                        mPassHint.setText(R.string.enter_password);
                    } else if (mPassword.getText().length() <= 5) {
                        mPassHint.setVisibility(View.VISIBLE);
                        mPassHint.setText(R.string.less_6_chars);
                    }
                    if (mPassword.getText().length() > 5 & mEmail.getText().length() > 0) {
                        mEmailHint.setVisibility(View.GONE);
                        mPassHint.setVisibility(View.GONE);
                        mEmail.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        mPassword.setVisibility(View.GONE);
                        signInUserToFireBase(
                                mEmail.getText().toString(), mPassword.getText().toString());

                    }
                }

            }
        });

        Button mButtonSignIn = (Button) view.findViewById(R.id.auth_btn_sign_in);
        mButtonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).replaceFragment("fragmentSignUp");
            }
        });

        Button mButtonSignInGoogle = (Button) view.findViewById(R.id.auth_btn_google_sign_in);
        mButtonSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInGoogle();
                mEmailHint.setVisibility(View.GONE);
                mPassHint.setVisibility(View.GONE);
                mEmail.setVisibility(View.GONE);
                mPassword.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
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
        firstStart=true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mFireBaseAuth = null;
        if (mGoogleApiClient != null) {
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
                            ((MainActivity) getActivity()).showToast("no such account found");
                            mProgressBar.setVisibility(View.GONE);
                            firstStart=true;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                ((MainActivity) getActivity()).showToast("Google account connection failed");
            }
        }
    }
}
