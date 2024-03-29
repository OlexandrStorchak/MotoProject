package com.example.alex.motoproject.screenMain;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.event.ConfirmShareLocationInChatEvent;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.event.InternetStatusChangedEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.screenMap.MapFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;

public class AlertControl implements MapFragment.MapFragmentHolder {
    private static final int ALERT_GPS_OFF = 20;
    private static final int ALERT_INTERNET_OFF = 21;
    private static final int ALERT_PERMISSION_RATIONALE = 22;
    private static final int ALERT_PERMISSION_NEVER_ASK_AGAIN = 23;
    private static final int ALERT_SHARE_LOCATION_CONFIRMATION = 24;
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 10;

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    AlertDialog alert;
    private ArrayList<Integer> mActiveAlerts = new ArrayList<>();
    private MainActivity mainActivity;

    AlertControl(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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
            return; //do nothing if this alert has already been created
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainActivity);
        switch (alertType) {
            case ALERT_GPS_OFF:
                makeGpsAlert(alertDialogBuilder);
                break;
            case ALERT_INTERNET_OFF:
                makeInternetAlert(alertDialogBuilder);
                break;
            case ALERT_PERMISSION_RATIONALE:
                makePermissionRationale(alertDialogBuilder);
                break;
            case ALERT_SHARE_LOCATION_CONFIRMATION:
                makeShareLocationConfirmation(alertDialogBuilder);
                break;
            case ALERT_PERMISSION_NEVER_ASK_AGAIN:
                makeOnNeverAksPermissionsAgainAlert(alertDialogBuilder);
                break;
            default:
                Log.e(AlertControl.class.getSimpleName(), "Unexpected alert, skipping creation!");
        }

        alert = alertDialogBuilder.create();
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                       @Override
                                       public void onDismiss(DialogInterface dialogInterface) {
                                           if (mActiveAlerts.contains(alertType))
                                               mActiveAlerts.remove((Integer) alertType);
                                       }
                                   }
        );
        alert.show();
//        if (!mActiveAlerts.contains(alertType))
        mActiveAlerts.add(alertType);
    }

    private void makeGpsAlert(AlertDialog.Builder builder) {
        //show when there is no GPS connection
        builder.setMessage(R.string.gps_turned_off_alert)
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
    }

    private void makeInternetAlert(AlertDialog.Builder builder) {
        //show when there is no Internet connection
        builder.setMessage(R.string.internet_turned_off_alert)
                .setPositiveButton(R.string.turn_on_mobile_internet,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callWirelessSettingIntent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                mainActivity.startActivity(callWirelessSettingIntent);
                            }
                        });
        builder.setNeutralButton(R.string.turn_on_wifi,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callWifiSettingIntent = new Intent(
                                Settings.ACTION_WIFI_SETTINGS);
                        mainActivity.startActivity(callWifiSettingIntent);
                    }
                });
    }

    private void makePermissionRationale(AlertDialog.Builder builder) {
        //show when user declines gps permission
        builder.setMessage(R.string.location_rationale)
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                requestLocationPermission();
                            }
                        });
        builder.setNegativeButton(R.string.close,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
    }

    private void makeShareLocationConfirmation(AlertDialog.Builder builder) {
        //ask user if he really wants to share his location in chat
        builder.setMessage(R.string.confirm_sharing_location_in_chat)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mFirebaseDatabaseHelper.shareLocationInChat();
                            }
                        });
        builder.setNegativeButton(R.string.close,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
    }

    private void makeOnNeverAksPermissionsAgainAlert(AlertDialog.Builder builder) {
        //show when user declines gps permission and checks never ask again
        builder.setMessage(R.string.how_to_change_location_setting)
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
        builder.setNegativeButton(R.string.close,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
    }

    void onRequestPermissionsResult(int requestCode,
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
        if (checkLocationPermission()) {//permission granted
            mainActivity.startLocationListenerService();
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
    public void onConfirmShareLocationInChatEvent(ConfirmShareLocationInChatEvent event) {
        showAlert(ALERT_SHARE_LOCATION_CONFIRMATION);
    }

    //These two methods fire to hide or show an alert of given type
    @Subscribe
    public void onInternetStatusChangedEvent(InternetStatusChangedEvent event) {
        handleConnectionChange(event.isInternetOn(), ALERT_INTERNET_OFF);
    }

    @Subscribe
    public void onGpsStatusChangedEvent(GpsStatusChangedEvent event) {
        handleConnectionChange(event.isGpsOn(), ALERT_GPS_OFF);
    }

    private void handleConnectionChange(boolean on, int alertType) {
        if (on) { //dismiss alert
            if (alert != null && mActiveAlerts.contains(alertType)) alert.dismiss();
        } else { //show alert
            if (!mActiveAlerts.contains(alertType)) showAlert(alertType);
        }
    }
}


