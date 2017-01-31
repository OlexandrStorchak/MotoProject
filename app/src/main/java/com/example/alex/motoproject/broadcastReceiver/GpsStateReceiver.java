package com.example.alex.motoproject.broadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.MainActivity;
import com.example.alex.motoproject.NotificationBuilderUtil;
import com.example.alex.motoproject.ShowAlertEvent;

import org.greenrobot.eventbus.EventBus;

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

        boolean gpsIsOn = locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER);
        if (gpsIsOn && mNotifyMgr != null) {
            //gps is now on, no need for notification
            mNotifyMgr.cancel(NOTIFICATION_ID);
        } else {
            App app = (App) context.getApplicationContext();
            boolean isMainActivityVisible = app.getMainActivityVisibility();
            if (isMainActivityVisible) { //show alert
                EventBus.getDefault().post(new ShowAlertEvent(MainActivity.ALERT_GPS_OFF));
            } else { //show notification
                Notification notification = NotificationBuilderUtil
                        .buildNotification(context, NOTIFICATION_ID);
                // get an instance of the NotificationManager service
                mNotifyMgr = (NotificationManager)
                        context.getSystemService(NOTIFICATION_SERVICE);
                // send notification
                mNotifyMgr.notify(NOTIFICATION_ID, notification);
            }
        }
    }
}
