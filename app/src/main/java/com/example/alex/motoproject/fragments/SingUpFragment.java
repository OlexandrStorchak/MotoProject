package com.example.alex.motoproject.fragments;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.alex.motoproject.MainActivity;
import com.example.alex.motoproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class SingUpFragment extends Fragment {


    private static final String TAG = "log";
    private EditText mEmail, mPassword, mRepeatPassword;
    private FirebaseAuth mFireBaseAuth;

    public SingUpFragment() {
        // Required empty public constructor
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
        return inflater.inflate(R.layout.fragment_sing_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmail = (EditText) view.findViewById(R.id.sing_up_email);
        mPassword = (EditText) view.findViewById(R.id.sing_up_pass);
        mRepeatPassword = (EditText) view.findViewById(R.id.sing_up_repeat_pass);
        TextView mTitle = (TextView) view.findViewById(R.id.sing_up_title);
        Button mButtonSubmit = (Button) view.findViewById(R.id.sing_up_btn_ok);


        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                String mEmail = SingUpFragment.this.mEmail.getText().toString();
                String mPass = mPassword.getText().toString();
                String mRepeatPass = mRepeatPassword.getText().toString();

                if (mEmail.length() == 0) {
                    SingUpFragment.this.mEmail.setHint("mEmail is empty");

                } else if (mPass.length() < 5) {

                    mPassword.setText("");
                    mPassword.setHint("min 6 characters");
                } else if (mRepeatPass.length() < 5) {

                    mRepeatPassword.setText("");
                    mRepeatPassword.setHint("repeat password");
                } else if (!mPassword.getText().toString().equals(mRepeatPassword.getText().toString())) {

                    mPassword.setText("");
                    mPassword.setHint("enter mPassword end repeat");
                    mRepeatPassword.setText("");
                    mRepeatPassword.setHint("mPassword not mutch");
                } else if (mPassword.getText().toString().equals(mRepeatPassword.getText().toString()) & SingUpFragment.this.mEmail.length() > 0) {

                    addNewUserToFireBase(mEmail, mPass);
                    ((MainActivity) getActivity()).replaceFragment("fragmentAuth");
                }
            }
        });
    }

    //Method for add new firebaseAuthCurrentUser into FireBase Auth, SingUp
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
