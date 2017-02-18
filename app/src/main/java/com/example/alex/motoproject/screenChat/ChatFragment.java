package com.example.alex.motoproject.screenChat;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements FirebaseDatabaseHelper.ChatUpdateListener {
    private static final int MESSAGE_MAX_CHARS = 200;
    private List<ChatMessage> mMessages = new ArrayList<>();
    private FirebaseDatabaseHelper mDatabaseHelper = new FirebaseDatabaseHelper();
    private RecyclerView mRecyclerView;
    private EditText mEditText;
    private ImageButton mSendButton;
    private ChatAdapter mAdapter = new ChatAdapter(mMessages);
    private Parcelable savedInstanceStateRecycler;
    private LinearLayoutManager mLayoutManager;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDatabaseHelper.registerChatMessagesListener(this);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onStart() {
        if (savedInstanceStateRecycler != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceStateRecycler);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        savedInstanceStateRecycler = mRecyclerView.getLayoutManager().onSaveInstanceState();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mDatabaseHelper.unregisterChatMessagesListener();
        super.onDestroyView();
    }

//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//
//        if(savedInstanceState != null) {
//            Parcelable savedRecyclerLayoutState =
//                    savedInstanceState.getParcelable(BUNDLE_SCROLL_POSITION);
//            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
//        }
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelable(BUNDLE_SCROLL_POSITION,
//                mRecyclerView.getLayoutManager().onSaveInstanceState());
//    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditText = (EditText) view.findViewById(R.id.edittext_message_chat);
        mSendButton = (ImageButton) view.findViewById(R.id.button_send_chat);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_chat);
        setupMessageSending();
        setupTextFilter();
        setupRecyclerView();
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
                mRecyclerView.smoothScrollToPosition(mMessages.size());
                mDatabaseHelper.sendChatMessage(mEditText.getText().toString());
                mEditText.setText("");
            }
        });
    }

    private void setupTextFilter() {
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MESSAGE_MAX_CHARS)});
    }

    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager)
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });
    }

    @Override
    public void onChatMessageNewData(ChatMessage message) {
        // TODO: 18.02.2017 use notify item changed instead!
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNewChatMessage(ChatMessage message) {
        mMessages.add(message);
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        if (mMessages.size() > 1 &&
                mLayoutManager.findLastCompletelyVisibleItemPosition() == mMessages.size() - 2) {
            mRecyclerView.smoothScrollToPosition(mMessages.size());
        }
    }
}
