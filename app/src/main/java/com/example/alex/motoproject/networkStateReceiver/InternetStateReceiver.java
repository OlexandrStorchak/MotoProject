package com.example.alex.motoproject.networkStateReceiver;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.alex.motoproject.event.InternetStatusChangedEvent;

import org.greenrobot.eventbus.EventBus;

public class InternetStateReceiver extends NetworkStateReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (isInternetEnabled()) {
            if (isMainActivityVisible()) {
                postInternetStatusChangedEvent(true);
            } else {
                cancelNotificationIfExists(INTERNET_NOTIFICATION_ID);
            }
        } else {
            if (isMainActivityVisible()) {
                postInternetStatusChangedEvent(false);
            } else {
                showNotification(INTERNET_NOTIFICATION_ID);
            }
        }
    }

    private boolean isInternetEnabled() {
        ConnectivityManager cm = (ConnectivityManager)
                app.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.getState() == NetworkInfo.State.CONNECTED;
    }

    private void postInternetStatusChangedEvent(boolean internetOn) {
        EventBus.getDefault().postSticky(new InternetStatusChangedEvent(internetOn));
    }
}
