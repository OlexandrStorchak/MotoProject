package com.example.alex.motoproject.service;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import static com.example.alex.motoproject.screenProfile.ScreenMyProfileFragment.PROFSET;


/**
 * The Service that listens for location changes and sends them to Firebase
 */
public class LocationListenerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    public static final String LOCATION_REQUEST_FREQUENCY_HIGH = "high";
    public static final String LOCATION_REQUEST_FREQUENCY_DEFAULT = "default";
    public static final String LOCATION_REQUEST_FREQUENCY_LOW = "low";


    private static final String LOG_TAG = "LocationListenerService";
    private static final String SHOULD_STOP_SERVICE_EXTRA = "isShouldStopService";
    public static final String GPS_RATE = "gpsRate";
    //TODO: where to store notification ids?
    int mNotificationId = 3;
    GoogleApiClient mGoogleApiClient;
    String mRequestFrequency = "default";
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    @Inject
    NetworkStateReceiver mNetworkStateReceiver;
    FirebaseAuth mFirebaseAuth;

    public LocationListenerService() {
        // Required empty public constructor
    }

    @Override
    public void onCreate() {
        App.getCoreComponent().inject(this);
        ((App) getApplication()).plusNetworkStateReceiverComponent();
        Log.d(LOG_TAG, "onCreate");

        // create an instance of GoogleAPIClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        mFirebaseAuth = FirebaseAuth.getInstance();

        showNotification();
        registerReceiver();

        SharedPreferences preferences = getApplicationContext()
                .getSharedPreferences(PROFSET, Context.MODE_PRIVATE);

        mFirebaseDatabaseHelper.setUserOnline(preferences.getString(
                mFirebaseDatabaseHelper.getCurrentUser().getUid(), null));


        ((App) getApplication()).setLocationListenerServiceOn(true);


        startLocationUpdates();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        unregisterReceiver(mNetworkStateReceiver);
        cleanupNotifications();

        ((App) getApplication()).setLocationListenerServiceOn(false);
        if (((App) getApplication()).isMainActivityDestroyed()) {
            mFirebaseDatabaseHelper.setUserOffline();
        } else {
            if (mFirebaseDatabaseHelper.getCurrentUser() != null) {
                SharedPreferences preferences = getApplicationContext()
                        .getSharedPreferences(PROFSET, Context.MODE_PRIVATE);

                mFirebaseDatabaseHelper.
                        setUserOnline(preferences.getString(
                                mFirebaseDatabaseHelper.getCurrentUser().getUid(), null));
            }
        }


        super.onDestroy();
    }

    //The service is not designed for binding
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //stop service if that was the purpose of intent
        if (intent.getExtras() != null &&
                intent.getExtras().getBoolean(SHOULD_STOP_SERVICE_EXTRA)) {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkLocationPermission()) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (lastLocation != null) {
                mFirebaseDatabaseHelper.updateUserLocation(lastLocation);
            }
        }
        startLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
        mFirebaseDatabaseHelper.updateUserLocation(location);
        Log.i("loc", "UPDATE");
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    //different variants of LocationRequest that might be changed via settings
    protected LocationRequest createLocationRequest() {
        SharedPreferences preferencesRate = getApplicationContext()
                .getSharedPreferences(GPS_RATE, Context.MODE_PRIVATE);

        mRequestFrequency = preferencesRate
                .getString(mFirebaseDatabaseHelper.getCurrentUser().getUid(), null);

        if (mRequestFrequency == null) {
            mRequestFrequency = LOCATION_REQUEST_FREQUENCY_DEFAULT;
        }

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.i("loc", "createLocationRequest: " + mRequestFrequency);
        switch (mRequestFrequency) {
            case LOCATION_REQUEST_FREQUENCY_HIGH:
                mLocationRequest.setInterval(5000); //5 secs
                //mLocationRequest.setFastestInterval(2000); //2 secs
                mLocationRequest.setSmallestDisplacement(14f); //14 m

                break;
            case LOCATION_REQUEST_FREQUENCY_DEFAULT:
                mLocationRequest.setInterval(20000); //20 secs
                mLocationRequest.setFastestInterval(10000); //10 secs
                mLocationRequest.setSmallestDisplacement(10f); //10 m
                break;
            case LOCATION_REQUEST_FREQUENCY_LOW:
                mLocationRequest.setInterval(30000); //30 secs
                mLocationRequest.setFastestInterval(20000); //20 secs
                mLocationRequest.setSmallestDisplacement(50f); //50 m
                break;
        }
        return mLocationRequest;
    }

    private void startLocationUpdates() {
        //handle unexpected permission absence
        if (checkLocationPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this);
        }
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    private void showNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_v1)
                        .setContentTitle("MotoProject")
                        .setContentText("Місцезнаходження відстежується.")

                        .setShowWhen(false);

        //create pending intent used when tapping on the app notification
        //open up ScreenMapFragment
        Intent mapIntent = new Intent(this, MainActivity.class);
        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        mapIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(mapPendingIntent);

        //create pending intent to finish this service
        Intent stopSelfIntent = new Intent(this, LocationListenerService.class);
        stopSelfIntent.putExtra(SHOULD_STOP_SERVICE_EXTRA, true);
        PendingIntent StopSelfPendingIntent =
                PendingIntent.getService(
                        this,
                        0,
                        stopSelfIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.addAction(R.drawable.ic_clear_gray_24dp,
                "Прибрати мене з мапи",
                StopSelfPendingIntent);

        // send notification
        startForeground(mNotificationId, mBuilder.build());
    }

    private void registerReceiver() {
        IntentFilter intentFilterNetwork = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(
                mNetworkStateReceiver, intentFilterNetwork);
    }

    private void cleanupNotifications() {
        //cleanup notifications, no need of them if the app is off
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancelAll();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


}
