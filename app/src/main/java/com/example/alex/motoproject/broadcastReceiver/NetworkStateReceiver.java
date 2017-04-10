package com.example.alex.motoproject.broadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.app.App;
import com.example.alex.motoproject.event.CancelAlertEvent;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.event.InternetStatusChangedEvent;
import com.example.alex.motoproject.event.ShowAlertEvent;
import com.example.alex.motoproject.screenMain.AlertControl;

import org.greenrobot.eventbus.EventBus;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NetworkStateReceiver extends BroadcastReceiver {

    public static final int INTERNET_NOTIFICATION_ID = 1;
    public static final int GPS_NOTIFICATION_ID = 2;

    private NotificationManager mNotifyMgr;
    private App mApp;

    public NetworkStateReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        mApp = (App) context.getApplicationContext();

        if (isInternetEnabled()) {
            if (isMainActivityVisible()) {
                postCancelAlertEvent(INTERNET_NOTIFICATION_ID);
                postInternetStatusChangedEvent(true);
            } else {
                cancelNotificationIfExists(INTERNET_NOTIFICATION_ID);
            }
        } else {
            if (isMainActivityVisible()) {
                postShowAlertEvent(INTERNET_NOTIFICATION_ID);
                postInternetStatusChangedEvent(false);
            } else {
                showNotification(INTERNET_NOTIFICATION_ID);
            }
        }

        if (!isGpsNeeded()) return;

        if (isGpsEnabled()) {
            if (isMainActivityVisible()) {
                postCancelAlertEvent(GPS_NOTIFICATION_ID);
                postGpsStatusChangedEvent(true);
            } else {
                cancelNotificationIfExists(GPS_NOTIFICATION_ID);
            }
        } else {
            if (isMainActivityVisible()) {
                postShowAlertEvent(GPS_NOTIFICATION_ID);
                postGpsStatusChangedEvent(false);
            } else {
                showNotification(GPS_NOTIFICATION_ID);
            }
        }
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager)
                mApp.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isInternetEnabled() {
        ConnectivityManager cm = (ConnectivityManager)
                mApp.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.getState() == NetworkInfo.State.CONNECTED;
    }

    //if LocationListenerService is off, no need for location monitoring
    private boolean isGpsNeeded() {
        return (mApp).isLocationListenerServiceOn();
    }

    private boolean isMainActivityVisible() {
        return (mApp).isMainActivityVisible();
    }

    private void showNotification(int notificationId) {
        Notification notification = buildNotification(notificationId);
        // get an instance of the NotificationManager service
        mNotifyMgr = (NotificationManager)
                mApp.getSystemService(NOTIFICATION_SERVICE);
        // send notification
        mNotifyMgr.notify(notificationId, notification);
    }

    private void cancelNotificationIfExists(int notificationId) {
        if (mNotifyMgr != null) {
            mNotifyMgr.cancel(notificationId);
        }
    }

    private void postShowAlertEvent(int notificationId) {
        switch (notificationId) {
            case INTERNET_NOTIFICATION_ID:
                EventBus.getDefault().post(new ShowAlertEvent(AlertControl.ALERT_INTERNET_OFF));
                break;
            case GPS_NOTIFICATION_ID:
                EventBus.getDefault().post(new ShowAlertEvent(AlertControl.ALERT_GPS_OFF));
                break;
        }
    }

    private void postCancelAlertEvent(int notificationId) {
        switch (notificationId) {
            case INTERNET_NOTIFICATION_ID:
                EventBus.getDefault().post(new CancelAlertEvent(AlertControl.ALERT_INTERNET_OFF));
                break;
            case GPS_NOTIFICATION_ID:
                EventBus.getDefault().post(new CancelAlertEvent(AlertControl.ALERT_GPS_OFF));
        }
    }

    private void postGpsStatusChangedEvent(boolean gpsOn) {
        EventBus.getDefault().postSticky(new GpsStatusChangedEvent(gpsOn));
    }

    private void postInternetStatusChangedEvent(boolean internetOn) {
        EventBus.getDefault().postSticky(new InternetStatusChangedEvent(internetOn));
    }

    private Notification buildNotification(int notificationId) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mApp)
                        .setSmallIcon(R.drawable.ic_notification_motorcycle)
                        .setContentTitle(mApp.getString(R.string.app_name))
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true);

        String settingsPath;
        switch (notificationId) {
            case NetworkStateReceiver.INTERNET_NOTIFICATION_ID:
                mBuilder.setContentText(mApp.getString(R.string.internet_is_off));
                settingsPath = Settings.ACTION_SETTINGS;
                break;
            case NetworkStateReceiver.GPS_NOTIFICATION_ID:
                mBuilder.setContentText(mApp.getString(R.string.gps_is_off));
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
                        mApp,
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
