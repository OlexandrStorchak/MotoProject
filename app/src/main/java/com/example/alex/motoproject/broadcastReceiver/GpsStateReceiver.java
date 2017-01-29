package com.example.alex.motoproject.broadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.example.alex.motoproject.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class GpsStateReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 2;
    NotificationManager mNotifyMgr;

    public GpsStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        boolean gpsIsOff = !locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER);
        if (gpsIsOff) {
            notifyGpsIsOff(context);
        } else if (mNotifyMgr != null) {
            //gps is now on, no need for notification
            mNotifyMgr.cancel(NOTIFICATION_ID);
        }
    }

    private void notifyGpsIsOff(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("MotoProject")
                        .setContentText("GPS вимкнено. Ввімкнути")
                        .setPriority(Notification.PRIORITY_HIGH)
                        //TODO make this notification cause vibration
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true);

        //create pending intent used when tapping on the notification
        Intent callLocationSettingIntent = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        callLocationSettingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(pendingIntent);

        // get an instance of the NotificationManager service
        mNotifyMgr = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
        // send notification
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
