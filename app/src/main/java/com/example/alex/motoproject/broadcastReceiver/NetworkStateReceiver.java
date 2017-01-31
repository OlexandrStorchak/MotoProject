package com.example.alex.motoproject.broadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NetworkStateReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 1;
    NotificationManager mNotifyMgr;

    public NetworkStateReceiver() {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if ((activeNetwork != null) && //Internet connection is on
                (activeNetwork.getState() ==
                        NetworkInfo.State.CONNECTED) &&
                (mNotifyMgr != null)) {
            //if a notification was created before, dismiss it
            mNotifyMgr.cancel(NOTIFICATION_ID);
        } else {
            //Internet connection is turned off
            App app = (App) context.getApplicationContext();
            boolean isMainActivityVisible = app.getMainActivityVisibility();
            if (isMainActivityVisible) {

            } else {
                notifyInternetIsOff(context);
            }

        }
    }

    private void notifyInternetIsOff(Context context) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("MotoProject")
                        .setContentText("Інтернет вимкнено. Ввімкнути")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true);

        //create pending intent used when tapping on the notification
        Intent callWirelessSettingIntent = new Intent(
                Settings.ACTION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        callWirelessSettingIntent,
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
