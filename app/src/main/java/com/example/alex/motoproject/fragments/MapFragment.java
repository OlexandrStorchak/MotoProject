package com.example.alex.motoproject.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.services.LocationListenerService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.alex.motoproject.R.id.map;


/**
 * The fragment that contains a map from Google Play Services.
 */

//TODO: custom pins
public class MapFragment extends Fragment implements OnMapReadyCallback {
    public static final String LOG_TAG = MapFragment.class.getSimpleName();

    public static final int PERMISSION_LOCATION_REQUEST_CODE = 10;
    private static MapFragment mapFragmentInstance;
    private final BroadcastReceiver mNetworkStateReceiver = new NetworkStateReceiver();
    MapFragmentListener mMapFragmentListener;
    //for methods calling, like creating pins
    private GoogleMap mMap;
    //for lifecycle
    private MapView mMapView;
    private DatabaseReference mDatabase;
    private String userUid;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment getInstance() {
        if (mapFragmentInstance == null) {
            mapFragmentInstance = new MapFragment();
        }
        return mapFragmentInstance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMapFragmentListener = (MapFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnMapFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //add Google map
        mMapView = (MapView) view.findViewById(map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userUid = auth.getCurrentUser().getUid();
        } else {
            auth.signOut();
        }

        FloatingActionButton drivingToggleButton =
                (FloatingActionButton) view.findViewById(R.id.button_drive_toggle);
        drivingToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isServiceOn =
                        ((App) getActivity().getApplication()).isLocationListenerServiceOn();
                if (!isServiceOn) {
                    mMapFragmentListener.handleLocation();
                    //TODO check by asking MainActivity
                } else if (checkLocationPermission()) {
                    mMap.setMyLocationEnabled(false);
                    getActivity().stopService(
                            new Intent(getActivity(), LocationListenerService.class));
                }
            }
        });

        fetchUserLocations();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //make map accessible from other methods
        mMap = map;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        LatLng cherkasy = new LatLng(49.443, 32.0727);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cherkasy, 11));
    }

    @Override
    public void onDestroyView() {
        mMapView.onDestroy();
        super.onDestroyView();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String permissions[],
//                                           @NonNull int[] grantResults) {
//        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
//            // Check the request was not cancelled
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // permission was granted
//                handleLocation();
//            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                        Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    //user checked never ask again
//                    mMapFragmentListener.showAlert(ALERT_PERMISSION_NEVER_ASK_AGAIN);
//
//                } else {
//                    //user did not check never ask again, show rationale
//                    mMapFragmentListener.showAlert(ALERT_PERMISSION_RATIONALE);
//                }
//            }
//        }
//    }

    //handle location runtime permission and setup listener service
//    private void handleLocation() {
//        if (checkLocationPermission()) {
////            //check gps connection
////            LocationManager locationManager =
////                    (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
////            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
////                showAlert(ALERT_GPS_OFF);
////            } else { //the app is allowed to get location, setup service
//            onLocationAllowed();
////            }
//        } else {
//            //show the permission prompt
//            requestPermissions(
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSION_LOCATION_REQUEST_CODE);
//        }
//    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onStop() {
        mMapView.onStop();
        super.onStop();
    }

    @Override
    public void onPause() {
//        boolean isServiceOn =
//                ((App) getActivity().getApplication()).isLocationListenerServiceOn();
//        if (!isServiceOn) {
//            try {
//                getActivity().unregisterReceiver(mNetworkStateReceiver);
//            } catch (IllegalArgumentException e) {
//                Log.v(LOG_TAG, "receiver was unregistered before onPause");
//            }
//        }

        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onStart() {
        mMapView.onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
//        //receiver that notifies when there`s no Internet connection
//        boolean isServiceOn =
//                ((App) getActivity().getApplication()).isLocationListenerServiceOn();
//        if (!isServiceOn) {
//            IntentFilter intentFilter = new IntentFilter(
//                    ConnectivityManager.CONNECTIVITY_ACTION);
//            intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
//            getActivity().registerReceiver(
//                    mNetworkStateReceiver, intentFilter);
//        }


        mMapView.onResume();
        super.onResume();
    }

    private void fetchUserLocations() {
        mDatabase.child("location").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, dataSnapshot.toString());
                String uid = dataSnapshot.getKey();
                if (!uid.equals(userUid)) {
                    Double lat = (Double) dataSnapshot.child("lat").getValue();
                    Double lng = (Double) dataSnapshot.child("lng").getValue();
                    if (lat != null && lng != null) {
                        LatLng latLng = new LatLng(lat, lng);
                        createPinOnMap(latLng, uid);
                    }
                    Log.d(LOG_TAG, lat + " " + lng);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String uid = dataSnapshot.getKey();
                if (!uid.equals(userUid)) {
                    Double lat = (Double) dataSnapshot.child("lat").getValue();
                    Double lng = (Double) dataSnapshot.child("lng").getValue();
                    LatLng latLng = new LatLng(lat, lng);
                    createPinOnMap(latLng, uid);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //handles showing alerts in MapFragment
    //TODO change implementation to create fragments
//    private void showAlert(int alertType) {
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
//        switch (alertType) {
////            case ALERT_GPS_OFF:
////                //show when there is no GPS connection
////                alertDialogBuilder.setMessage(R.string.gps_turned_off_alert)
////                        .setPositiveButton(R.string.to_settings,
////                                new DialogInterface.OnClickListener() {
////                                    public void onClick(DialogInterface dialog, int id) {
////                                        Intent callGPSSettingIntent = new Intent(
////                                                android.provider.Settings
////                                                        .ACTION_LOCATION_SOURCE_SETTINGS)
////                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////                                                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
////                                                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
////                                        startActivity(callGPSSettingIntent);
////
////                                        if (checkLocationPermission()) {
////                                            onLocationAllowed();
////                                        }
////
////                                    }
////                                });
////                break;
////            case ALERT_INTERNET_OFF:
////                //show when there is no Internet connection
////                alertDialogBuilder.setMessage(R.string.internet_turned_off_alert)
////                        .setPositiveButton(R.string.turn_on_mobile_internet,
////                                new DialogInterface.OnClickListener() {
////                                    public void onClick(DialogInterface dialog, int id) {
////                                        Intent callWirelessSettingIntent = new Intent(
////                                                Settings.ACTION_WIRELESS_SETTINGS);
////                                        startActivity(callWirelessSettingIntent);
////                                    }
////                                });
////                alertDialogBuilder.setPositiveButton(R.string.turn_on_wifi,
////                        new DialogInterface.OnClickListener() {
////                            public void onClick(DialogInterface dialog, int id) {
////                                Intent callWifiSettingIntent = new Intent(
////                                        Settings
////                                                .ACTION_WIFI_SETTINGS);
////                                startActivity(callWifiSettingIntent);
////                            }
////                        });
////                break;
//            case ALERT_PERMISSION_RATIONALE:
//                //show when user declines gps permission
//                alertDialogBuilder.setMessage(R.string.location_rationale)
//                        .setCancelable(false)
//                        .setPositiveButton(R.string.ok,
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        requestPermissions(
//                                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                                PERMISSION_LOCATION_REQUEST_CODE);
//                                    }
//                                });
//                alertDialogBuilder.setNegativeButton(R.string.close,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//                break;
//            case ALERT_PERMISSION_NEVER_ASK_AGAIN:
//                //show when user declines gps permission and checks never ask again
//                alertDialogBuilder.setMessage(R.string.how_to_change_location_setting)
//                        .setCancelable(false)
//                        .setPositiveButton(R.string.to_settings,
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        Intent intent = new Intent();
//                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                                        Uri uri = Uri.fromParts(
//                                                "package", getContext().getPackageName(), null);
//                                        intent.setData(uri);
//                                        startActivity(intent);
//                                    }
//                                });
//                alertDialogBuilder.setNegativeButton(R.string.close,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//                break;
//        }
//        alert = alertDialogBuilder.create();
//        alert.show();
//    }

    private void createPinOnMap(LatLng latLng, String uid) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(uid));
        marker.setTag(uid);
        Log.d(LOG_TAG, "pin created!");
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void onLocationAllowed() {
        getActivity().startService(new Intent(getActivity(), LocationListenerService.class));
        try {
            getActivity().unregisterReceiver(mNetworkStateReceiver);
        } catch (IllegalArgumentException e) {
            Log.v(LOG_TAG, "mNetworkReceiver has already been unregistered");
        }

        try { //suppress SecurityException
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    //TODO a better interface name
    public interface MapFragmentListener {
        void showAlert(int alertType);

        void handleLocation();
    }
}

