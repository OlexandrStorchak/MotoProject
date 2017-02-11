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
import com.example.alex.motoproject.events.CancelAlertEvent;
import com.example.alex.motoproject.events.ShowAlertEvent;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.example.alex.motoproject.utils.NotificationBuilderUtil;

import org.greenrobot.eventbus.EventBus;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NetworkStateReceiver extends BroadcastReceiver {

    // TODO: 11.02.2017 make only one instance of this class

    public static final int INTERNET_NOTIFICATION_ID = 1;
    public static final int GPS_NOTIFICATION_ID = 2;
    Context context;
    NotificationManager mNotifyMgr;
    App app;

    public NetworkStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        app = (App) context.getApplicationContext();

        if (isInternetEnabled()) {
            if (isMainActivityVisible()) {
                postCancelAlertEvent(INTERNET_NOTIFICATION_ID);
            } else {
                cancelNotificationIfExists(INTERNET_NOTIFICATION_ID);
            }
        } else {
            if (isMainActivityVisible()) {
                postShowAlertEvent(INTERNET_NOTIFICATION_ID);
            } else {
                showNotification(INTERNET_NOTIFICATION_ID);
            }
        }

        if (isGpsNeeded()) {
            if (isGpsEnabled()) {
                if (isMainActivityVisible()) {
                    postCancelAlertEvent(GPS_NOTIFICATION_ID);
                } else {
                    cancelNotificationIfExists(GPS_NOTIFICATION_ID);
                }
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

    //if LocationListenerService is off, no need for location monitoring
    private boolean isGpsNeeded() {
        return (app).isLocationListenerServiceOn();
    }

    private boolean isMainActivityVisible() {
        return (app).isMainActivityVisible();
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

    private void postCancelAlertEvent(int notificationId) {
        switch (notificationId) {
            case INTERNET_NOTIFICATION_ID:
                EventBus.getDefault().post(new CancelAlertEvent(MainActivity.ALERT_INTERNET_OFF));
                break;
            case GPS_NOTIFICATION_ID:
                EventBus.getDefault().post(new CancelAlertEvent(MainActivity.ALERT_GPS_OFF));
        }
    }
}
