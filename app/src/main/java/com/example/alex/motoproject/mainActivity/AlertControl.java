package com.example.alex.motoproject.mainActivity;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.event.CancelAlertEvent;
import com.example.alex.motoproject.event.ConfirmShareLocationInChatEvent;
import com.example.alex.motoproject.event.ShareLocationInChatAllowedEvent;
import com.example.alex.motoproject.event.ShowAlertEvent;
import com.example.alex.motoproject.screenMap.ScreenMapFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;

public class AlertControl implements ScreenMapFragment.MapFragmentHolder {

    public static final int ALERT_GPS_OFF = 20;
    public static final int ALERT_INTERNET_OFF = 21;
    private static final int ALERT_PERMISSION_RATIONALE = 22;
    private static final int ALERT_PERMISSION_NEVER_ASK_AGAIN = 23;
    private static final int ALERT_SHARE_LOCATION_CONFIRMATION = 24;
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 10;

    @Inject
    NetworkStateReceiver mNetworkStateReceiver;

    AlertDialog mAlert;
    private ArrayList<Integer> mActiveAlerts = new ArrayList<>();
    private MainActivity mainActivity;


    public AlertControl(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

    }

    void plusNetworkStateReceiver() {
        ((App) mainActivity.getApplicationContext()).plusNetworkStateReceiverComponent();
    }

    void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void showAlert(final int alertType) {
        if (mActiveAlerts.contains(alertType)) {
            return; //do nothing if this mAlert has already been created
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        switch (alertType) {
            case ALERT_GPS_OFF:
                //show when there is no GPS connection
                alertDialogBuilder.setMessage(R.string.gps_turned_off_alert)
                        .setPositiveButton(R.string.to_settings,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent callGPSSettingIntent = new Intent(
                                                android.provider.Settings
                                                        .ACTION_LOCATION_SOURCE_SETTINGS)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        mainActivity.startActivity(callGPSSettingIntent);

                                        handleLocation();
                                    }

                                });
                break;
            case ALERT_INTERNET_OFF:
                //show when there is no Internet connection
                alertDialogBuilder.setMessage(R.string.internet_turned_off_alert)
                        .setPositiveButton(R.string.turn_on_mobile_internet,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent callWirelessSettingIntent = new Intent(
                                                Settings.ACTION_WIRELESS_SETTINGS);
                                        mainActivity.startActivity(callWirelessSettingIntent);
                                    }
                                });
                alertDialogBuilder.setNeutralButton(R.string.turn_on_wifi,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callWifiSettingIntent = new Intent(
                                        Settings.ACTION_WIFI_SETTINGS);
                                mainActivity.startActivity(callWifiSettingIntent);
                            }
                        });
                break;
            case ALERT_PERMISSION_RATIONALE:
                //show when user declines gps permission
                alertDialogBuilder.setMessage(R.string.location_rationale)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        requestLocationPermission();
                                    }
                                });
                alertDialogBuilder.setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
            case ALERT_SHARE_LOCATION_CONFIRMATION:
                //ask user if he really wants to share his location in chat
                alertDialogBuilder.setMessage(R.string.confirm_sharing_location_in_chat)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        EventBus.getDefault().post(new ShareLocationInChatAllowedEvent());
                                    }
                                });
                alertDialogBuilder.setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;

            case ALERT_PERMISSION_NEVER_ASK_AGAIN:
                //show when user declines gps permission and checks never ask again
                alertDialogBuilder.setMessage(R.string.how_to_change_location_setting)
                        .setCancelable(false)
                        .setPositiveButton(R.string.to_settings,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        Uri uri = Uri.fromParts(
                                                "package", mainActivity.getPackageName(), null);
                                        intent.setData(uri);
                                        mainActivity.startActivity(intent);
                                    }
                                });
                alertDialogBuilder.setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
        }


        mAlert = alertDialogBuilder.create();
        mAlert.setOnDismissListener(new DialogInterface.OnDismissListener()

                                    {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            if (mActiveAlerts.contains(alertType))
                                                mActiveAlerts.remove((Integer) alertType);
                                        }
                                    }

        );
        mAlert.show();
        if (!mActiveAlerts.contains(alertType))
            mActiveAlerts.add(alertType);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                handleLocation();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //user did not check never ask again, show rationale
                    showAlert(ALERT_PERMISSION_RATIONALE);
                } else {
                    //user checked never ask again
                    showAlert(ALERT_PERMISSION_NEVER_ASK_AGAIN);
                }
            }
        }

    }


    @Override
    public void handleLocation() {

        if (checkLocationPermission()) { //permission granted

            mainActivity.screenMapFragment.onLocationAllowed();

        } else { //permission was not granted, show the permission prompt
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(mainActivity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_LOCATION_REQUEST_CODE);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(mainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

    }

    @Subscribe
    //the method called when received an event from EventBus asking for showing mAlert
    public void onShouldShowAlertEvent(ShowAlertEvent event) {
        int receivedAlertType = event.alertType;
        if (!mActiveAlerts.contains(receivedAlertType))
            showAlert(event.alertType);
    }


    @Subscribe
    //the method called when received an event from EventBus asking for canceling mAlert
    public void onShouldCancelEvent(CancelAlertEvent event) {
        int receivedAlertType = event.alertType;
        if (mActiveAlerts.contains(receivedAlertType)) {
            if (mAlert != null) {
                mAlert.dismiss();
            }
        }

    }

    void registerNetworkStateReceiver() {
        //if LocationListenerService is on, this receiver has already been registered
        if (!isServiceOn()) {
            IntentFilter intentFilter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);

            mainActivity.registerReceiver(
                    mNetworkStateReceiver, intentFilter);
        }
    }

    void unregisterNetworkStateReceiver() {
        if (!isServiceOn()) {
            try {
                mainActivity.unregisterReceiver(mNetworkStateReceiver);
            } catch (IllegalArgumentException e) {
                Log.v("log", "receiver was unregistered before onStop");
            }
        }
    }

    boolean isServiceOn() {
        return ((App) mainActivity.getApplication()).isLocationListenerServiceOn();
    }

    @Subscribe
    public void onConfirmShareLocationInChatEvent(ConfirmShareLocationInChatEvent event) {
        showAlert(ALERT_SHARE_LOCATION_CONFIRMATION);
    }
}


