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
import android.util.Log;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.app.App;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.alex.motoproject.firebase.FirebaseConstants.PATH_SOS;
import static com.example.alex.motoproject.util.ArgKeys.SHOW_CHAT_FRAGMENT;


public class MainService extends Service {
    private static final int FIVE_MINUTES = 300000;

    App mApp;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        final String currentUser = new FirebaseDatabaseHelper()
                .getCurrentUser().getUid();
        mApp = (App) getApplicationContext();
        final DatabaseReference ref = mFirebaseDatabase.getReference().child(PATH_SOS);
        ref.keepSynced(false);

        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ref.setValue(null);
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String id = (String) postSnapshot.child("userId").getValue();
                    if (!(id.equals(currentUser))) {
                        showNotification();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void showNotification() {
        Intent chatIntent = new Intent(this, MainActivity.class);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        chatIntent.putExtra(SHOW_CHAT_FRAGMENT, true);
        PendingIntent chatFragment =
                PendingIntent.getActivity(
                        this,
                        0,
                        chatIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_sos)

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
    public void onDestroy() {
        Log.i("logi", "onDestroy: MainService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("logi", "onStartComand: MainService");

        return super.onStartCommand(intent, flags, startId);
    }

}
