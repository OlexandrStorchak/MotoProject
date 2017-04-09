package com.example.alex.motoproject.screenProfile;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.app.App;
import com.example.alex.motoproject.event.CurrentUserProfileReadyEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.firebase.UserProfileFirebase;
import com.example.alex.motoproject.util.DimensHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.example.alex.motoproject.service.LocationListenerService.GPS_RATE;
import static com.example.alex.motoproject.service.LocationListenerService.LOCATION_REQUEST_FREQUENCY_DEFAULT;
import static com.example.alex.motoproject.service.LocationListenerService.LOCATION_REQUEST_FREQUENCY_HIGH;
import static com.example.alex.motoproject.service.LocationListenerService.LOCATION_REQUEST_FREQUENCY_LOW;
import static com.example.alex.motoproject.util.ArgKeys.ABOUT_ME;
import static com.example.alex.motoproject.util.ArgKeys.EDIT_MODE;
import static com.example.alex.motoproject.util.ArgKeys.KEY_NAME;
import static com.example.alex.motoproject.util.ArgKeys.MOTORCYCLE;
import static com.example.alex.motoproject.util.ArgKeys.NICKNAME;
import static com.example.alex.motoproject.util.ArgKeys.USER_DATA;
import static com.facebook.FacebookSdk.getApplicationContext;

public class ScreenMyProfileFragment extends Fragment {

    public static final String PROFSET = "profSett";
    public static final String PROFILE_GPS_MODE_PUBLIC = "public";
    public static final String PROFILE_GPS_MODE_FRIENDS = "friends";
    public static final String PROFILE_GPS_MODE_SOS = "sos";
    public static final String PROFILE_GPS_MODE_NOGPS = "noGps";
    private static final int PICK_IMAGE_REQUEST = 189;
    private static final int MAX_BYTES_SIZE = 10000000;
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
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
    private Spinner mapRate;
    private ImageView saveProfileData;
    private ImageView editProfileData;
    private LinearLayout gpsRatePanel;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private UserProfileFirebase userProfileFirebase;
    private String currentUid;

    private SharedPreferences preferencesRate;

    private boolean mEditMode;

    public ScreenMyProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        userProfileFirebase = savedInstanceState.getParcelable(USER_DATA);
        displayUserData();

        mEditMode = savedInstanceState.getBoolean(EDIT_MODE);
        if (mEditMode) {
            nameEdit.setText(savedInstanceState.getString(KEY_NAME));
            nickNameEdit.setText(savedInstanceState.getString(NICKNAME));
            motorcycleEdit.setText(savedInstanceState.getString(MOTORCYCLE));
            aboutMeEdit.setText(savedInstanceState.getString(ABOUT_ME));
            setEditMode(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(EDIT_MODE, mEditMode);
        outState.putParcelable(USER_DATA, userProfileFirebase);

        if (mEditMode) {
            outState.putString(KEY_NAME, nameEdit.getText().toString());
            outState.putString(NICKNAME, nickNameEdit.getText().toString());
            outState.putString(MOTORCYCLE, motorcycleEdit.getText().toString());
            outState.putString(ABOUT_ME, aboutMeEdit.getText().toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        App.getCoreComponent().inject(this);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferencesRate = getContext().getSharedPreferences(GPS_RATE, MODE_PRIVATE);

        avatar = (ImageView) view.findViewById(R.id.profile_avatar);
        email = (TextView) view.findViewById(R.id.profile_email);
        name = (TextView) view.findViewById(R.id.profile_name);
        motorcycle = (TextView) view.findViewById(R.id.profile_motorcycle);
        nickName = (TextView) view.findViewById(R.id.profile_nick_name);
        aboutMe = (TextView) view.findViewById(R.id.profile_about_user);

        if (savedInstanceState == null) {
            mFirebaseDatabaseHelper.getCurrentUserModel();
        }
        nameEdit = (EditText) view.findViewById(R.id.profile_name_edit);
        nickNameEdit = (EditText) view.findViewById(R.id.profile_nick_name_edit);
        motorcycleEdit = (EditText) view.findViewById(R.id.profile_motorcycle_edit);
        aboutMeEdit = (EditText) view.findViewById(R.id.profile_about_me_edit);
        gpsRatePanel = (LinearLayout) view.findViewById(R.id.profile_gps_rate_panel);

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        saveProfileData = (ImageView) view.findViewById(R.id.profile_btn_save);

        saveProfileData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfileFirebase.setId(mFirebaseDatabaseHelper.getCurrentUser().getUid());
                userProfileFirebase.setEmail(mFirebaseDatabaseHelper.getCurrentUser().getEmail());

                userProfileFirebase.setMotorcycle(motorcycleEdit.getText().toString());
                userProfileFirebase.setName(nameEdit.getText().toString());
                userProfileFirebase.setAboutMe(aboutMeEdit.getText().toString());
                userProfileFirebase.setNickName(nickNameEdit.getText().toString());

                mFirebaseDatabaseHelper.saveMyProfile(userProfileFirebase);

//                mFirebaseDatabaseHelper.getCurrentUserModel();
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getActivity()
                                .getSystemService(Activity.INPUT_METHOD_SERVICE);

                inputMethodManager.hideSoftInputFromWindow(getActivity()
                        .getCurrentFocus().getWindowToken(), 0);

                setEditMode(false);
            }
        });
        editProfileData = (ImageView) view.findViewById(R.id.profile_btn_edit);
        editProfileData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEditMode(true);
                putStandartDataEditTexts();
            }
        });

        mapRate = (Spinner) view.findViewById(R.id.profile_set_gps_rate);
        mapRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // TODO: 05.03.2017 add method contract to LocationListenerService
                switch (i) {
                    case 0:
                        preferencesRate.edit()
                                .putString(currentUid, LOCATION_REQUEST_FREQUENCY_HIGH).apply();
                        break;
                    case 1:
                        preferencesRate.edit()
                                .putString(currentUid, LOCATION_REQUEST_FREQUENCY_DEFAULT).apply();
                        break;
                    case 2:
                        preferencesRate.edit()
                                .putString(currentUid, LOCATION_REQUEST_FREQUENCY_LOW).apply();
                        break;
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setEditMode(boolean editMode) {
        mEditMode = editMode;

        int textViews, editViews;
        if (editMode) { //hide TextViews and show EditViews
            textViews = View.GONE;
            editViews = View.VISIBLE;
        } else {
            textViews = View.VISIBLE;
            editViews = View.GONE;
        }

        name.setVisibility(textViews);
        nameEdit.setVisibility(editViews);

        email.setVisibility(textViews);

        nickName.setVisibility(textViews);
        nickNameEdit.setVisibility(editViews);

        motorcycle.setVisibility(textViews);
        motorcycleEdit.setVisibility(editViews);

        aboutMe.setVisibility(textViews);
        aboutMeEdit.setVisibility(editViews);

        saveProfileData.setVisibility(editViews);
        editProfileData.setVisibility(textViews);

        gpsRatePanel.setVisibility(textViews);
    }

    private void putStandartDataEditTexts() {
        nameEdit.setText(name.getText());
        nickNameEdit.setText(nickName.getText());
        motorcycleEdit.setText(motorcycle.getText());
        aboutMeEdit.setText(aboutMe.getText());
    }

    @Subscribe
    public void onCurrentUserModelReadyEvent(CurrentUserProfileReadyEvent user) {
        setEditMode(false);
        userProfileFirebase = user.getUserProfileFirebase();
        displayUserData();
    }

    private void displayUserData() {
        currentUid = userProfileFirebase.getId();
        email.setText(userProfileFirebase.getEmail());
        final String ava = userProfileFirebase.getAvatar();

        DimensHelper.getScaledAvatar(ava, avatar.getWidth(), new DimensHelper.AvatarRefReceiver() {
            @Override
            public void onRefReady(String ref) {
                //        Picasso.with(getApplicationContext())
//                .load(ava)
//                .resize(avatar.getMaxWidth(), avatar.getMaxHeight())
//                .centerCrop()
//                .into(avatar);
                Glide.with(getApplicationContext())
                        .load(ref)
                        .override(avatar.getMaxWidth(), avatar.getMaxHeight())
                        .centerCrop()
                        .into(avatar);
            }

            @Override
            public void onError() {

            }
        });

        name.setText(userProfileFirebase.getName());
        nickName.setText(userProfileFirebase.getNickName());
        aboutMe.setText(userProfileFirebase.getAboutMe());
        motorcycle.setText(userProfileFirebase.getMotorcycle());

        String gpsRate = preferencesRate.getString(currentUid, null);
        if (gpsRate == null) {
            gpsRate = LOCATION_REQUEST_FREQUENCY_DEFAULT;
        }

        switch (gpsRate) {
            case LOCATION_REQUEST_FREQUENCY_LOW:
                mapRate.setSelection(2);
                break;
            case LOCATION_REQUEST_FREQUENCY_DEFAULT:
                mapRate.setSelection(1);
                break;
            case LOCATION_REQUEST_FREQUENCY_HIGH:
                mapRate.setSelection(0);
                break;
        }
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
                //if file not large for current device and can be load
                getAvatarStream(2, filePath);


            } catch (IOException | OutOfMemoryError e) {


                try {
                    //if file to large for current device
                    getAvatarStream(4, filePath);

                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }

            }
        }
    }

    private void getAvatarStream(int size, Uri filePath) throws FileNotFoundException, OutOfMemoryError {
        InputStream iStream = getApplicationContext().getContentResolver().openInputStream(filePath);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = size;

        Bitmap bitmap = BitmapFactory.decodeStream(iStream, null, options);

        avatar.setImageBitmap(bitmap);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        byte[] dataBytes = baos.toByteArray();
        uploadFile(dataBytes);
    }

    //this method will upload the file
    private void uploadFile(byte[] dataBytes) {
        if (dataBytes != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Завантаження");
            progressDialog.show();

            final StorageReference avatarRef =
                    storage.getReference().child("avatars/" + currentUid + ".jpeg");
            avatarRef.putBytes(dataBytes)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successful
                            progressDialog.dismiss();
                            mFirebaseDatabaseHelper.
                                    setCurrentUserAvatar(taskSnapshot.getDownloadUrl().toString());
                            // mFirebaseDatabaseHelper.getCurrentUserModel();
                            Toast.makeText(getApplicationContext(), "Завантажено ",
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //if the upload is not successful
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