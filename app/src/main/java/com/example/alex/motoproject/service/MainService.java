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

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainService extends Service {
    private static final int FIVE_MINUTES = 300000;
    public static final String CHAT_INTENT_KEY = "chat";
    private static final String CHAT_INTENT_VALUE = "showChat";
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
        final DatabaseReference ref = mFirebaseDatabase.getReference().child("sos");
        ref.keepSynced(false);
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long sosCount=0;
                if (dataSnapshot.getChildrenCount() > sosCount) {
                    long time = System.currentTimeMillis();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        sosCount = dataSnapshot.getChildrenCount();

                        MainServiceSosModel model = postSnapshot.getValue(MainServiceSosModel.class);
                        if (!(model.getUserId().equals(currentUser)) && model.getTime() != null) {

                            try {
                                long userTime = Long.parseLong(model.getTime());
                                if (userTime + FIVE_MINUTES > time) {
                                    showNotification(sosCount, model.getDescription());
                                }
                            } catch (NumberFormatException error) {
                                Log.i("error", "onDataChange: ");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void showNotification(long sosCount, String description) {
        Intent chatIntent = new Intent(this, MainActivity.class);
        chatIntent.putExtra(CHAT_INTENT_KEY,CHAT_INTENT_VALUE);
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
