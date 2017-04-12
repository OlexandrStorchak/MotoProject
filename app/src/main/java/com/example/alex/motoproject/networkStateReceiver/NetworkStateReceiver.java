package com.example.alex.motoproject.networkStateReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.app.App;

import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class NetworkStateReceiver extends BroadcastReceiver {
    public static final int INTERNET_NOTIFICATION_ID = 1;
    public static final int GPS_NOTIFICATION_ID = 2;

    NotificationManager notifyMgr;
    App app;

    @Override
    public void onReceive(Context context, Intent intent) {
        app = (App) context.getApplicationContext();
    }

    boolean isMainActivityVisible() {
        return (app).isMainActivityVisible();
    }

    void showNotification(int notificationId) {
        Notification notification = buildNotification(notificationId);
        //Get an instance of the NotificationManager service
        notifyMgr = (NotificationManager)
                app.getSystemService(NOTIFICATION_SERVICE);
        //Send notification
        notifyMgr.notify(notificationId, notification);
    }

    void cancelNotificationIfExists(int notificationId) {
        if (notifyMgr != null) {
            notifyMgr.cancel(notificationId);
        }
    }

    private Notification buildNotification(int notificationId) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(app)
                        .setSmallIcon(R.drawable.ic_notification_motorcycle)
                        .setContentTitle(app.getString(R.string.app_name))
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true);

        String settingsPath;
        switch (notificationId) {
            case InternetStateReceiver.INTERNET_NOTIFICATION_ID:
                mBuilder.setContentText(app.getString(R.string.internet_is_off));
                settingsPath = Settings.ACTION_SETTINGS;
                break;
            case InternetStateReceiver.GPS_NOTIFICATION_ID:
                mBuilder.setContentText(app.getString(R.string.gps_is_off));
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
                        app,
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
