package com.example.alex.motoproject.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.ShowUserProfileEvent;
import com.example.alex.motoproject.util.ArgKeys;
import com.example.alex.motoproject.util.CropCircleTransformation;

import org.greenrobot.eventbus.EventBus;

public class MapUserDetailsDialogFragment extends DialogFragment {

    private String mUid;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_details_user_map, null);
        TextView nameView = (TextView) view.findViewById(R.id.name);
        ImageView avatarView = (ImageView) view.findViewById(R.id.avatar);

        Bundle args = getArguments();

        mUid = args.getString(ArgKeys.KEY_UID);
        String name = args.getString(ArgKeys.KEY_NAME);
        String avatarRef = args.getString(ArgKeys.KEY_AVATAR_REF);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setView(view);
        builder.setPositiveButton(R.string.to_profile, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EventBus.getDefault().post(new ShowUserProfileEvent(mUid));
            }
        });

        nameView.setText(name);
//        Picasso.with(getContext()).load(avatarRef)
//                .resize(avatarView.getMaxWidth(), avatarView.getMaxHeight())
//                .centerCrop()
//                .transform(new CircleTransform())
//                .into(avatarView);
        Glide.with(getContext()).load(avatarRef)
                .override(avatarView.getMaxWidth(), avatarView.getMaxHeight())
                .centerCrop()
                .transform(new CropCircleTransformation(getContext()))
                .into(avatarView);

        return builder.create();
    }
}
