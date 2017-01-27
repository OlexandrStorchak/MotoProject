package com.example.alex.motoproject.fragments;


import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.alex.motoproject.MainActivity;
import com.example.alex.motoproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class SignUpFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    private static SignUpFragment signUpFragmentInstance;


    private static final String TAG = "log";
    private EditText mEmail, mPassword, mRepeatPassword;
    private FirebaseAuth mFireBaseAuth;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment getInstance(){
        if(signUpFragmentInstance==null){
            signUpFragmentInstance=new SignUpFragment();
        }
        return signUpFragmentInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFireBaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmail = (EditText) view.findViewById(R.id.sign_up_email);
        mPassword = (EditText) view.findViewById(R.id.sign_up_pass);
        mRepeatPassword = (EditText) view.findViewById(R.id.sign_up_repeat_pass);

        Button mButtonSubmit = (Button) view.findViewById(R.id.sign_up_btn_ok);


        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {


                if (mEmail.getText().length() == 0) {
                    mEmail.setError(getString(R.string.email_is_empty));

                }
                if (mPassword.getText().length() < 5) {

                    mPassword.setText("");
                    mPassword.setError(getString(R.string.less_6_chars));
                }
                if (mRepeatPassword.getText().length() < 5) {

                    mRepeatPassword.setText("");
                    mRepeatPassword.setError(getString(R.string.hint_repeat_pass));
                }
                if (!mPassword.getText().toString().equals(mRepeatPassword.getText().toString())) {

                    mPassword.setText("");
                    mPassword.setError(getString(R.string.hint_repeat_pass));
                    mRepeatPassword.setText("");
                    mRepeatPassword.setError(getString(R.string.pass_not_mutch));
                } else if (mPassword.getText().toString().equals(mRepeatPassword.getText().toString())
                        & mEmail.getText().length() > 0
                        & mPassword.getText().length() > 5) {

                    addNewUserToFireBase(mEmail.getText().toString(), mPassword.getText().toString());
                    ((MainActivity) getActivity()).replaceFragment("fragmentAuth");
                    ((MainActivity) getActivity()).showDialog();


                }
            }
        });
    }

    //Method for add new firebaseAuthCurrentUser into FireBase Auth, SignUp
    public void addNewUserToFireBase(String email, String password) {
        mFireBaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((getActivity()), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the firebaseUser. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in firebaseUser can be handled in the listener.
                        if (mFireBaseAuth.getCurrentUser() != null) {
                            mFireBaseAuth.getCurrentUser().sendEmailVerification();
                            mFireBaseAuth.signOut();
                        } else {
                            Log.d(TAG, "onComplete: addNewFirebase User: curent user is null");
                        }

                        if (!task.isSuccessful()) {
                            Log.d(TAG, "onComplete: ");
                            ((MainActivity) getActivity()).showToast("Authentication failed");
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFireBaseAuth = null;
    }
}
