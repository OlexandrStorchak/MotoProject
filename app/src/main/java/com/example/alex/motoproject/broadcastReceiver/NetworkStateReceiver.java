package com.example.alex.motoproject.broadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.MainActivity;
import com.example.alex.motoproject.events.ShowAlertEvent;
import com.example.alex.motoproject.utils.NotificationBuilderUtil;

import org.greenrobot.eventbus.EventBus;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NetworkStateReceiver extends BroadcastReceiver {

    public static final int INTERNET_NOTIFICATION_ID = 1;
    public static final int GPS_NOTIFICATION_ID = 2;
    Context context;
    NotificationManager mNotifyMgr;

    public NetworkStateReceiver() {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent) {
//        ConnectivityManager cm = (ConnectivityManager)
//                context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//
//        boolean networkIsOn = (activeNetwork != null);
//
//        if (networkIsOn) { //Internet connection is on
//            if ((activeNetwork.getState() == NetworkInfo.State.CONNECTED) &&
//                    (mNotifyMgr != null)) {
//                //if a notification was created before, dismiss it
//                mNotifyMgr.cancel(INTERNET_NOTIFICATION_ID);
//            }
//        } else {
//            //Internet connection is turned off
//            App app = (App) context.getApplicationContext();
//            boolean isMainActivityVisible = app.isMainActivityVisible();
//            if (isMainActivityVisible) { //show alert
//                EventBus.getDefault().post(new ShowAlertEvent(MainActivity.ALERT_INTERNET_OFF));
//            } else { //show notification
//                Notification notification = NotificationBuilderUtil
//                        .buildNotification(context, INTERNET_NOTIFICATION_ID);
//                // get an instance of the NotificationManager service
//                mNotifyMgr = (NotificationManager)
//                        context.getSystemService(NOTIFICATION_SERVICE);
//                // send notification
//                mNotifyMgr.notify(INTERNET_NOTIFICATION_ID, notification);
//            }
//        }

        this.context = context;

        if (isInternetEnabled()) {
            cancelNotificationIfExists(INTERNET_NOTIFICATION_ID);
        } else {
            if (isMainActivityVisible()) {
                postShowAlertEvent(INTERNET_NOTIFICATION_ID);
            } else {
                showNotification(INTERNET_NOTIFICATION_ID);
            }
        }

        if (isGpsNeeded()) {
            if (isGpsEnabled()) {
                cancelNotificationIfExists(GPS_NOTIFICATION_ID);
            } else {
                if (isMainActivityVisible()) {
                    postShowAlertEvent(GPS_NOTIFICATION_ID);
                } else {
                    showNotification(GPS_NOTIFICATION_ID);
                }
            }
        }
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean isInternetEnabled() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.getState() == NetworkInfo.State.CONNECTED;
    }

    private boolean isGpsNeeded() {
        return ((App) context.getApplicationContext()).isLocationListenerServiceOn();
    }

    private boolean isMainActivityVisible() {
        return ((App) context.getApplicationContext()).isMainActivityVisible();
    }

    private void showNotification(int notificationId) {
        Notification notification = NotificationBuilderUtil
                .buildNotification(context, notificationId);
        // get an instance of the NotificationManager service
        mNotifyMgr = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
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
                EventBus.getDefault().post(new ShowAlertEvent(MainActivity.ALERT_INTERNET_OFF));
                break;
            case GPS_NOTIFICATION_ID:
                EventBus.getDefault().post(new ShowAlertEvent(MainActivity.ALERT_GPS_OFF));
                break;
        }
    }
}
