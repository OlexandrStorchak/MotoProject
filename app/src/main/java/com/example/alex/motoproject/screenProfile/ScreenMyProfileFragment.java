package com.example.alex.motoproject.screenProfile;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.CurrentUserProfileReadyEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.MyProfileFirebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;

public class ScreenMyProfileFragment extends Fragment {


    private static final int PICK_IMAGE_REQUEST = 13;
    private TextView email;
    private TextView name;
    private TextView nickName;
    private TextView motorcycle;
    private TextView aboutMe;
    private EditText nameEdit;
    private EditText nickNameEdit;
    private EditText motorcycleEdit;
    private EditText aboutMeEdit;
    private ImageView avatar;
    private ImageView mapIndicator;
    private Spinner mapVisibility;
    private ImageView saveProfileData;
    private ImageView editProfileData;
    private LinearLayout gpsPanel;


    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private SharedPreferences profileSet;
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    String currentUserId;

    public static final String PROFSET = "profSett";

    public static final String PROFILE_GPS_MODE_PUBLIC = "public";
    private static final String PROFILE_GPS_MODE_FRIENDS = "friends";
    private static final String PROFILE_GPS_MODE_SOS = "sos";
    private String currentUid;

    public ScreenMyProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        App.getCoreComponent().inject(this);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileSet = getContext().getSharedPreferences(PROFSET, Context.MODE_PRIVATE);
        avatar = (ImageView) view.findViewById(R.id.profile_avatar);
        email = (TextView) view.findViewById(R.id.profile_email);
        name = (TextView) view.findViewById(R.id.profile_name);
        motorcycle = (TextView) view.findViewById(R.id.profile_motorcycle);
        nickName = (TextView) view.findViewById(R.id.profile_nick_name);
        aboutMe = (TextView) view.findViewById(R.id.profile_about_me);
        mapIndicator = (ImageView) view.findViewById(R.id.profile_show_on_map_indicator);
        mFirebaseDatabaseHelper.getCurrentUserModel();
        mapVisibility = (Spinner) view.findViewById(R.id.profile_set_gps_visibility);
        nameEdit = (EditText) view.findViewById(R.id.profile_name_edit);
        nickNameEdit = (EditText) view.findViewById(R.id.profile_nick_name_edit);
        motorcycleEdit = (EditText) view.findViewById(R.id.profile_motorcycle_edit);
        aboutMeEdit = (EditText) view.findViewById(R.id.profile_about_me_edit);

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        gpsPanel = (LinearLayout) view.findViewById(R.id.profile_gps_panel);

        saveProfileData = (ImageView) view.findViewById(R.id.profile_btn_save);

        saveProfileData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyProfileFirebase profile = new MyProfileFirebase();
                profile.setId(mFirebaseDatabaseHelper.getCurrentUser().getUid());
                profile.setAvatar(mFirebaseDatabaseHelper.getCurrentUser().getPhotoUrl().toString());
                profile.setEmail(mFirebaseDatabaseHelper.getCurrentUser().getEmail());

                profile.setMotorcycle(motorcycleEdit.getText().toString());
                profile.setName(nameEdit.getText().toString());
                profile.setAboutMe(aboutMeEdit.getText().toString());
                profile.setNickName(nickNameEdit.getText().toString());

                mFirebaseDatabaseHelper.saveMyProfile(profile);
                mFirebaseDatabaseHelper.getCurrentUserModel();
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getActivity()
                                .getSystemService(Activity.INPUT_METHOD_SERVICE);

                inputMethodManager.hideSoftInputFromWindow(getActivity()
                        .getCurrentFocus().getWindowToken(), 0);

            }
        });
        editProfileData = (ImageView) view.findViewById(R.id.profile_btn_edit);
        editProfileData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editMode(View.GONE, View.VISIBLE);

            }
        });


        mapVisibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = profileSet.edit();
                switch (i) {
                    case 0:
                        editor.putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(), PROFILE_GPS_MODE_PUBLIC);
                        mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_PUBLIC);

                        break;
                    case 1:
                        editor.putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(), PROFILE_GPS_MODE_FRIENDS);
                        mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_FRIENDS);

                        break;
                    case 2:
                        editor.putString(mFirebaseDatabaseHelper.getCurrentUser().getUid(), PROFILE_GPS_MODE_SOS);
                        mFirebaseDatabaseHelper.setUserOnline(PROFILE_GPS_MODE_SOS);

                        break;
                }
                editor.apply();
                onStart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences preferences = getContext().getSharedPreferences(PROFSET, Context.MODE_PRIVATE);

        switch (preferences.getString(mFirebaseDatabaseHelper.getCurrentUser().getUid(), null)) {
            case PROFILE_GPS_MODE_SOS:
                mapIndicator.setImageResource(R.mipmap.ic_map_indicator_red);
                mapVisibility.setSelection(2);
                break;
            case PROFILE_GPS_MODE_FRIENDS:
                mapIndicator.setImageResource(R.mipmap.ic_map_indicator_yellow);
                mapVisibility.setSelection(1);
                break;
            case PROFILE_GPS_MODE_PUBLIC:
                mapIndicator.setImageResource(R.mipmap.ic_map_indicator_green);
                mapVisibility.setSelection(0);
                break;
        }
    }

    private void editMode(int textViews, int editViews) {

        name.setVisibility(textViews);
        nameEdit.setVisibility(editViews);
        nameEdit.setText(name.getText());

        email.setVisibility(textViews);

        nickName.setVisibility(textViews);
        nickNameEdit.setVisibility(editViews);
        nickNameEdit.setText(nickName.getText());

        motorcycle.setVisibility(textViews);
        motorcycleEdit.setVisibility(editViews);
        motorcycleEdit.setText(motorcycle.getText());

        aboutMe.setVisibility(textViews);
        aboutMeEdit.setVisibility(editViews);
        aboutMeEdit.setText(aboutMe.getText());

        saveProfileData.setVisibility(editViews);
        editProfileData.setVisibility(textViews);

        gpsPanel.setVisibility(textViews);
    }

    @Subscribe
    public void onCurrentUserModelReadyEvent(CurrentUserProfileReadyEvent user) {
        editMode(View.VISIBLE, View.GONE);
        currentUid = user.getMyProfileFirebase().getId();
        email.setText(user.getMyProfileFirebase().getEmail());

        Picasso.with(getApplicationContext())
                .load(user.getMyProfileFirebase().getAvatar())
                .resize(avatar.getMaxWidth(), avatar.getMaxHeight())
                .centerCrop()
                .into(avatar);
        name.setText(user.getMyProfileFirebase().getName());
        nickName.setText(user.getMyProfileFirebase().getNickName());
        aboutMe.setText(user.getMyProfileFirebase().getAboutMe());
        motorcycle.setText(user.getMyProfileFirebase().getMotorcycle());


    }

    public void setUserId(String currentUserId) {
        this.currentUserId = currentUserId;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Get Avatar"), PICK_IMAGE_REQUEST);
    }
    //handling the image chooser activity result

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri filePath;
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);
                avatar.setImageBitmap(bitmap);
                uploadFile(filePath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //this method will upload the file
    private void uploadFile(Uri filePath) {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Завантаження");
            progressDialog.show();

            final StorageReference avatarRef =
                    storage.getReference().child("avatars/" + currentUid + ".jpeg");
            avatarRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            progressDialog.dismiss();
                            mFirebaseDatabaseHelper.
                                    setCurrentUserAvatar(taskSnapshot.getDownloadUrl().toString());
                            mFirebaseDatabaseHelper.getCurrentUserModel();
                            Toast.makeText(getApplicationContext(), "Завантажено ",
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //if the upload is not successfull
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Завантаження перервалось",
                                    Toast.LENGTH_LONG).show();


                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred())
                                    / taskSnapshot.getTotalByteCount();

                            progressDialog.setMessage("Завантажено : " + ((int) progress) + " %");
                        }

                    });
        }

    }
}
