package com.example.alex.motoproject.screenChat;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import org.greenrobot.eventbus.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private static final int MESSAGE_MAX_CHARS = 200;
    private FirebaseDatabaseHelper mDatabaseHelper = new FirebaseDatabaseHelper();
    private EditText mEditText;
    private ImageButton mSendButton;
    private ChatAdapter mAdapter = new ChatAdapter();

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(mAdapter);
        mDatabaseHelper.registerChatMessagesListener();
        super.onStart();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(mAdapter);
        mDatabaseHelper.unregisterChatMessagesListener();
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditText = (EditText) view.findViewById(R.id.edittext_message_chat);
        mSendButton = (ImageButton) view.findViewById(R.id.button_send_chat);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_chat);
        setupMessageSending();
        setupTextFilter();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
    }

    private void setupMessageSending() {
        mSendButton.setVisibility(View.GONE);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    mSendButton.setVisibility(View.VISIBLE);
                } else {
                    mSendButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseHelper.sendChatMessage(mEditText.getText().toString());
                mEditText.setText("");
            }
        });
    }

    private void setupTextFilter() {
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MESSAGE_MAX_CHARS)});
    }
}
