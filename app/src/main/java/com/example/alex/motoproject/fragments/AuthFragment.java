package com.example.alex.motoproject.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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


public class AuthFragment extends Fragment {
        EditText email,pass;
        TextView title;
        Button submit;
        Button singin;
        Button singinGoogle;

    private static final String TAG = "log";

    public AuthFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        email = (EditText)view.findViewById(R.id.auth_email);
        pass = (EditText)view.findViewById(R.id.auth_pass);
        title = (TextView)view.findViewById(R.id.auth_title);
        submit = (Button)view.findViewById(R.id.auth_btn_ok);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (email.getText().length()==0){
                    ((MainActivity)getActivity()).showToast("Email is empty");
                } else if (pass.getText().length()==0){
                    ((MainActivity)getActivity()).showToast("Password is empty");
                } else
                ((MainActivity)getActivity()).singInUserToFireBase(
                        email.getText().toString(),pass.getText().toString());

            }
        });

        singin = (Button)view.findViewById(R.id.auth_btn_sing_in);
        singin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).replaceFragment("fragmentSingUp");
            }
        });

        singinGoogle = (Button)view.findViewById(R.id.auth_btn_google_sing_in);
        singinGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).signInGoogle();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    @Override
    public void onDetach() {
        super.onDetach();

    }


}
