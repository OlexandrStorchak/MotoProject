package com.example.alex.motoproject.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.app.App;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.screenMain.MainActivity;
import com.example.alex.motoproject.util.DistanceUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;

import static com.example.alex.motoproject.firebase.FirebaseConstants.LAT;
import static com.example.alex.motoproject.firebase.FirebaseConstants.LNG;
import static com.example.alex.motoproject.firebase.FirebaseConstants.PATH_SOS;
import static com.example.alex.motoproject.firebase.FirebaseConstants.RELATION_FRIEND;
import static com.example.alex.motoproject.firebase.FirebaseConstants.USER_ID;
import static com.example.alex.motoproject.util.ArgKeys.SHOW_CHAT_FRAGMENT;


public class MainService extends Service {
    private static final int MAX_DISTANCE_METERS = 20000;

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    App mApp;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mApp = (App) getApplicationContext();
        App.getCoreComponent().inject(this);

        final String currentUser;
        try {
             currentUser= mFirebaseDatabaseHelper
                    .getCurrentUser().getUid();

        final FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

        final DatabaseReference ref = mFirebaseDatabase.getReference().child(PATH_SOS);

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    ref.setValue(null);
                    mFirebaseDatabaseHelper.getCurrentUserLocation(
                            new FirebaseDatabaseHelper.UsersLocationReceiver() {
                                @Override
                                public void onCurrentUserLocationReady(LatLng myCoords) {
                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                        String id = (String) postSnapshot.child(USER_ID).getValue();
                                        if ((id.equals(currentUser))) return;
                                        double lat = Double.parseDouble(
                                                (String) postSnapshot.child(LAT).getValue());
                                        double lng = Double.parseDouble(
                                                (String) postSnapshot.child(LNG).getValue());
                                        LatLng sosCoords = new LatLng(lat, lng);
                                        if (mFirebaseDatabaseHelper.isInFriendList(id, RELATION_FRIEND)
                                                || DistanceUtil.isClose(myCoords,
                                                sosCoords,
                                                MAX_DISTANCE_METERS)) {
                                            showNotification();
                                        }
                                    }
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void showNotification() {
        Intent chatIntent = new Intent(this, MainActivity.class);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        chatIntent.putExtra(SHOW_CHAT_FRAGMENT, true);
        PendingIntent chatFragment =
                PendingIntent.getActivity(
                        this,
                        0,
                        chatIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_sos)
                        .setContentTitle(getString(R.string.notification_tittle_need_help))
                        .setAutoCancel(true)
                        .setShowWhen(false)
                        .setContentIntent(chatFragment)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setLights(Color.RED, 1000, 1000);

        if (!mApp.isMainActivityVisible()) {
            mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000});
        }

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(4, mBuilder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}
