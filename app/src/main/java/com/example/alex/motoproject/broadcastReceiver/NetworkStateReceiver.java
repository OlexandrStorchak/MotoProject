package com.example.alex.motoproject.broadcastReceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.MainActivity;
import com.example.alex.motoproject.NotificationBuilderUtil;
import com.example.alex.motoproject.ShowAlertEvent;

import org.greenrobot.eventbus.EventBus;

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

        boolean networkIsOn = (activeNetwork != null);

        if (networkIsOn) { //Internet connection is on
            if ((activeNetwork.getState() ==
                    NetworkInfo.State.CONNECTED) &&
                    (mNotifyMgr != null)) {
                //if a notification was created before, dismiss it
                mNotifyMgr.cancel(NOTIFICATION_ID);
            }
        } else {
            //Internet connection is turned off
            App app = (App) context.getApplicationContext();
            boolean isMainActivityVisible = app.getMainActivityVisibility();
            if (isMainActivityVisible) {
                EventBus.getDefault().post(new ShowAlertEvent(MainActivity.ALERT_INTERNET_OFF));
            } else {
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
