package com.example.alex.motoproject.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OnClickChatDialogFragmentEvent;

import org.greenrobot.eventbus.EventBus;

public class ChatLocLimitDialogFragment extends DialogFragment
        implements SeekBar.OnSeekBarChangeListener {

    private static final int MAX_SEEKBAR_VALUE = 20;

    private TextView mLimitTextView;
    private int mLimit;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mLimit = getActivity().getPreferences(Context.MODE_PRIVATE)
                .getInt(getString(R.string.chat_location_limit_preferences), 0);

        View view = View.inflate(getContext(), R.layout.dialog_chat_location_limit, null);
        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekbar_chat_limit);
        mLimitTextView = (TextView) view.findViewById(R.id.textview_value_chat_limit);

        seekBar.setMax(MAX_SEEKBAR_VALUE);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setProgress(mLimit);

        setTextLimitTextView();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.chat_location_limit_title);
        builder.setView(view);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().getPreferences(Context.MODE_PRIVATE).edit()
                        .putInt(getString(R.string.chat_location_limit_preferences), mLimit)
                        .apply();
                EventBus.getDefault().post(new OnClickChatDialogFragmentEvent(mLimit));
            }
        });

        builder.setNegativeButton(R.string.close, null);

        return builder.create();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mLimit = seekBar.getProgress();
        setTextLimitTextView();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void setTextLimitTextView() {
        if (mLimit > 0) {
            mLimitTextView.setText(String.valueOf(mLimit) + "" + getString(R.string.km));
        } else {
            mLimitTextView.setText(getString(R.string.off));
        }
    }
}
