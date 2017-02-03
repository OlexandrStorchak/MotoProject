package com.example.alex.motoproject.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;

public final class NotificationBuilderUtil {
    public static Notification buildNotification(
            Context context, int notificationId) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("MotoProject")
                        .setContentText("Інтернет вимкнено. Ввімкнути")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true);

        String settingsPath;
        switch (notificationId) {
            case NetworkStateReceiver.INTERNET_NOTIFICATION_ID:
                mBuilder.setContentText("Інтернет вимкнено. Ввімкнути");
                settingsPath = Settings.ACTION_SETTINGS;
                break;
            case NetworkStateReceiver.GPS_NOTIFICATION_ID:
                mBuilder.setContentText("GPS вимкнено. Ввімкнути");
                settingsPath = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
                break;
            default:
                return null;
        }

        //create pending intent used when tapping on the notification
        Intent callSettingIntent = new Intent(
                settingsPath)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        callSettingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(pendingIntent);

        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;

        return notification;
    }
}
