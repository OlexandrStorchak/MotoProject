package com.example.alex.motoproject.networkStateReceiver;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.example.alex.motoproject.event.GpsStatusChangedEvent;

import org.greenrobot.eventbus.EventBus;

public class GpsStateReceiver extends NetworkStateReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (isGpsEnabled()) {
            if (isMainActivityVisible()) {
                postGpsStatusChangedEvent(true);
            } else {
                cancelNotificationIfExists(GPS_NOTIFICATION_ID);
            }
        } else {
            if (isMainActivityVisible()) {
                postGpsStatusChangedEvent(false);
            } else {
                showNotification(GPS_NOTIFICATION_ID);
            }
        }
    }


    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager)
                app.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void postGpsStatusChangedEvent(boolean gpsOn) {
        EventBus.getDefault().postSticky(new GpsStatusChangedEvent(gpsOn));
    }
}
