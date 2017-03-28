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
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;


public class MainService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("logi", "onCreate: MainService");
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference ref = mFirebaseDatabase.getReference().child("sos");

        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Log.i("logi", "on DataChange" + dataSnapshot.getChildrenCount());
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        MainServiceSosModel model = postSnapshot.getValue(MainServiceSosModel.class);
                        Log.i("logi", "onDataChange: " + model.getTime());
                        if (!(model.getUserId().equals(new FirebaseDatabaseHelper().getCurrentUser().getUid()))) {
                            showNotification(dataSnapshot.getChildrenCount(), model.getUserName(), model.getDescription());
                            ref.keepSynced(false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showNotification(long childrenCount, String userName, String description) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_sos)

                        .setContentTitle("" + userName)
                        .setContentText(description)
                        .setShowWhen(false)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setLights(Color.RED, 1000, 1000)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000});
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
