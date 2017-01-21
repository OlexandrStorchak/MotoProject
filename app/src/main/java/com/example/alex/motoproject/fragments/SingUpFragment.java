package com.example.alex.motoproject.fragments;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.alex.motoproject.MainActivity;
import com.example.alex.motoproject.R;


public class SingUpFragment extends Fragment {


    EditText email, pass, repeatPass;
    TextView title;
    Button submit;

    public SingUpFragment() {
        // Required empty public constructor
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
        email = (EditText) view.findViewById(R.id.sing_up_email);
        pass = (EditText) view.findViewById(R.id.sing_up_pass);
        repeatPass = (EditText) view.findViewById(R.id.sing_up_repeat_pass);
        title = (TextView) view.findViewById(R.id.sing_up_title);
        submit = (Button) view.findViewById(R.id.sing_up_btn_ok);


        submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                String mEmail = email.getText().toString();
                String mPass = pass.getText().toString();
                String mRepeatPass = repeatPass.getText().toString();

                if (mEmail.length() == 0) {
                    email.setHint("email is empty");

                } else if (mPass.length() < 5) {

                    pass.setText("");
                    pass.setHint("min 6 characters");
                } else if (mRepeatPass.length() < 5) {

                    repeatPass.setText("");
                    repeatPass.setHint("repeat password");
                } else if (!pass.getText().toString().equals(repeatPass.getText().toString())) {

                    pass.setText("");
                    pass.setHint("enter pass end repeat");
                    repeatPass.setText("");
                    repeatPass.setHint("pass not mutch");
                } else if (pass.getText().toString().equals(repeatPass.getText().toString()) & email.length() > 0) {

                    ((MainActivity) getActivity()).addNewUserToFireBase(mEmail, mPass);
                    ((MainActivity) getActivity()).replaceFragment("fragmentAuth");
                }
            }
        });
    }
}
