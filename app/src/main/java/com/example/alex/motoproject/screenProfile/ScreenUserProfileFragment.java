package com.example.alex.motoproject.screenProfile;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.OnlineUserProfileReadyEvent;
import com.example.alex.motoproject.firebase.Constants;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.UsersProfileFirebase;
import com.example.alex.motoproject.util.DimensHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import static com.example.alex.motoproject.util.ArgKeys.BUTTONS_TRANSLATION_Y;
import static com.example.alex.motoproject.util.ArgKeys.USER_DATA;

public class ScreenUserProfileFragment extends Fragment {

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    private TextView mName;
    private TextView mMotorcycle;
    private TextView mNickName;
    private TextView mEmail;
    private TextView mAbout;
    private ImageView mAvatar;

    private LinearLayout mButtons;
    private ImageButton mRemoveFriend;
    private ImageButton mAddFriend;

    private UsersProfileFirebase mUserData;

    public ScreenUserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        App.getCoreComponent().inject(this);
        EventBus.getDefault().register(this);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_screen_user_profile, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(USER_DATA, mUserData);
        outState.putFloat(BUTTONS_TRANSLATION_Y, mButtons.getTranslationY());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mButtons.setTranslationY(savedInstanceState.getFloat(BUTTONS_TRANSLATION_Y));
            mUserData = savedInstanceState.getParcelable(USER_DATA);
            displayUserData();
        }
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButtons = (LinearLayout) view.findViewById(R.id.profile_user_buttons);
        mAddFriend = (ImageButton) view.findViewById(R.id.profile_add_friend);
        mRemoveFriend = (ImageButton) view.findViewById(R.id.profile_remove_from_friends);

        mAvatar = (ImageView) view.findViewById(R.id.profile_user_avatar);
        mEmail = (TextView) view.findViewById(R.id.profile_user_email);
        mName = (TextView) view.findViewById(R.id.profile_user_name);
        mMotorcycle = (TextView) view.findViewById(R.id.profile_user_motorcycle);
        mNickName = (TextView) view.findViewById(R.id.profile_user_nickname);
        mAbout = (TextView) view.findViewById(R.id.profile_about_user);

        final Space optionalSpace = (Space) view.findViewById(R.id.space_optional);

        final NestedScrollView scrollView =
                (NestedScrollView) view.findViewById(R.id.profile_user_scroll_view);
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX,
                                       int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) { //Scroll up
                    mButtons.animate().translationY(mButtons.getHeight());
                } else if (scrollY < oldScrollY) { //Scroll down
                    mButtons.animate().translationY(0);
                }
            }
        });

        scrollView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if (!scrollView.canScrollVertically(1) && !scrollView.canScrollVertically(-1)) {
                            //Add empty space to the end of ScrollView for buttons, so users
                            //are able to scroll down and see the full text in a profile
                            optionalSpace.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().setTitle(getContext().getString(R.string.app_name));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void displayUserData() {
        if (mUserData.getName() != null) {
            getActivity().setTitle(mUserData.getName());
            mName.setText(getString(R.string.name) + ": " + mUserData.getName());
        }
        if (mUserData.getNickName() != null) {
            mNickName.setText(getString(R.string.nick_name) + ": " + mUserData.getNickName());
        }
        if (mUserData.getEmail() != null) {
            mEmail.setText(getString(R.string.email) + ": " + mUserData.getEmail());
        }
        if (mUserData.getMotorcycle() != null) {
            mMotorcycle.setText(getString(R.string.motorcycle) + ": " + mUserData.getMotorcycle());
        }
        if (mUserData.getAboutMe() != null) {
            mAbout.setText(getString(R.string.about) + ": " + mUserData.getAboutMe());
        }

        String avatarRef = mUserData.getAvatar();

//        if (avatarRef.contains(".googleusercontent.com/")) {
//            int newSize = mAvatar.getWidth();
//            avatarRef = avatarRef.replace("/s96-c", "/s" + newSize + "-c"); //Increase Google mAvatar size
//            Log.i("log", "onOnlineUserProfileReady: "+ avatarRef);
//        }
        DimensHelper.getScaledAvatar(avatarRef,
                mAvatar.getMaxWidth(), new DimensHelper.AvatarRefReceiver() {
                    @Override
                    public void onRefReady(String ref) {
                        //        Picasso.with(getContext())
//                .load(avatarRef)
//                .resize(mAvatar.getMaxWidth(), mAvatar.getMaxHeight())
//                .centerCrop()
//                .into(mAvatar);
                        Glide.with(getContext())
                                .load(ref)
                                .override(mAvatar.getMaxWidth(), mAvatar.getMaxHeight())
                                .centerCrop()
                                .into(mAvatar);
                    }

                    @Override
                    public void onError() {

                    }
                });

        //If friend already added
        if (mFirebaseDatabaseHelper.isInFriendList(mUserData.getId(), Constants.RELATION_FRIEND)) {
            mRemoveFriend.setVisibility(View.VISIBLE);
            mAddFriend.setVisibility(View.GONE);
        }

        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRemoveFriend.setVisibility(View.VISIBLE);
                mAddFriend.setVisibility(View.GONE);
                mFirebaseDatabaseHelper.sendFriendRequest(mUserData.getId());
            }
        });
        mRemoveFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRemoveFriend.setVisibility(View.GONE);
                mAddFriend.setVisibility(View.VISIBLE);
                mFirebaseDatabaseHelper.setRelationToUser(mUserData.getId(), null);
                mFirebaseDatabaseHelper.setUserRelation(mUserData.getId(), null);
            }
        });
    }

    @Subscribe
    public void onOnlineUserProfileReady(OnlineUserProfileReadyEvent event) {
        mUserData = event.getUserProfileFirebase();
        displayUserData();
    }
}
