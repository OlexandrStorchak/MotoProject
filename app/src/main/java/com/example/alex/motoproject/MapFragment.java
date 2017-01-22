package com.example.alex.motoproject;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import static android.content.Context.LOCATION_SERVICE;
import static com.example.alex.motoproject.R.id.map;


/**
 * The fragment that contains a map from Google Play Services.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {
    //TODO: fix fps
    public static final int PERMISSION_LOCATION_REQUEST_CODE = 10;
    public static final int ALERT_GPS_OFF = 20;
    public static final int ALERT_INTERNET_OFF = 21;
    public static final int ALERT_PERMISSION_RATIONALE = 22;
    public static final int ALERT_PERMISSION_NEVER_ASK_AGAIN = 23;

    private GoogleMap mMap;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //lines for custom fragment support
        MapView mMapView = (MapView) view.findViewById(map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();


        mMapView.getMapAsync(this);

        Button buttonStartDriving = (Button) view.findViewById(R.id.button_drive_start);
        buttonStartDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLocation();

                //TODO: check if there is a GPS connection
                LocationManager locationManager =
                        (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                    showAlert("gps");
                }
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //make map accessible from other methods
        mMap = map;

        LatLng cherkasy = new LatLng(49.443, 32.0727);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cherkasy, 11));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            // Check the request was not cancelled
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                handleLocation();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //user checked never ask again
                    showAlert(ALERT_PERMISSION_NEVER_ASK_AGAIN);

                } else {
                    //user did not check never ask again, show rationale
                    showAlert(ALERT_PERMISSION_RATIONALE);
                }
            }
        }
    }

    //handle location runtime permission and setup listener service
    private void handleLocation() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //the app is allowed to get location, setup service
            getActivity().startService(new Intent(getActivity(), LocationListenerService.class));
            mMap.setMyLocationEnabled(true);
        } else {
            //show the permission prompt
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }
    }

    //handles showing variety of alerts in MapFragment
    private void showAlert(int alertType) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        switch (alertType) {
            case ALERT_GPS_OFF:
                //show when there is no GPS connection
                alertDialogBuilder.setMessage("GPS вимкнено. Не хотіли б ви ввімкнути його?")
                        .setPositiveButton("В налаштування",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent callGPSSettingIntent = new Intent(
                                                android.provider.Settings
                                                        .ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(callGPSSettingIntent);
                                    }
                                });
                break;
            case ALERT_INTERNET_OFF:
                //show when there is no Internet connection
                alertDialogBuilder.setMessage("Інтернет вимкнено. Не хотіли б ви ввімкнути його?")
                        .setPositiveButton("Увімкнути моб. Інтернет",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent callWirelessSettingIntent = new Intent(
                                                Settings
                                                        .ACTION_WIRELESS_SETTINGS);
                                        startActivity(callWirelessSettingIntent);
                                    }
                                });
                alertDialogBuilder.setPositiveButton("Увімкнути WIFI",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callWifiSettingIntent = new Intent(
                                        Settings
                                                .ACTION_WIFI_SETTINGS);
                                startActivity(callWifiSettingIntent);
                            }
                        });
                break;
            case ALERT_PERMISSION_RATIONALE:
                //show when user declines gps permission
                alertDialogBuilder.setMessage(R.string.location_rationale)
                        .setCancelable(false)
                        .setPositiveButton("Дозволити",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        requestPermissions(
                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                PERMISSION_LOCATION_REQUEST_CODE);
                                    }
                                });
                alertDialogBuilder.setNegativeButton("Закрити",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
            case ALERT_PERMISSION_NEVER_ASK_AGAIN:
                //show when user declines gps permission and checks never ask again
                alertDialogBuilder.setMessage("Ви можете змінити свій вибір у налаштуваннях. Дозволи --> Ваше місцезнаходження")
                        .setCancelable(false)
                        .setPositiveButton("В налаштування",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                        Uri uri = Uri.fromParts(
                                                "package", getContext().getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                });
                alertDialogBuilder.setNegativeButton("Закрити",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
        }

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}

