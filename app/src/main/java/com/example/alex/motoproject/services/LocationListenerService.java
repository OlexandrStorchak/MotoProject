package com.example.alex.motoproject.services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.util.Date;

/**
 * The Service that listens for location changes and sends them to Firebase
 */
public class LocationListenerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String LOG_TAG = "LocationListenerService";
    private static final String SHOULD_STOP_SERVICE_EXTRA = "isShouldStopService";
    //TODO: where to store notification ids?
    int mNotificationId = 3;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    String mRequestFrequency = "default";

    FirebaseDatabaseHelper firebaseDatabaseHelper = new FirebaseDatabaseHelper();
    FirebaseAuth firebaseAuth;

    private NetworkStateReceiver mNetworkStateReceiver;



    public LocationListenerService() {
        // Required empty public constructor
    }

    @Override
    public void onCreate() {
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

        firebaseAuth = FirebaseAuth.getInstance();

        showNotification();
        registerReceiver();

        super.onCreate();
        ((App) this.getApplication()).setLocationListenerServiceOn(true);
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        unregisterReceiver(mNetworkStateReceiver);
        cleanupNotifications();

        ((App) this.getApplication()).setLocationListenerServiceOn(false);
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
            Log.d(LOG_TAG, "onStartCommand with action stop service");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkLocationPermission()) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                mCurrentLocation = mLastLocation;
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateFirebaseData(mLastLocation);
            }
        }
        startLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateFirebaseData(location);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    //different variants of LocationRequest that might be changed via settings
    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        switch (mRequestFrequency) {
            case "highFrequency":
                mLocationRequest.setInterval(10000); //10 secs
                mLocationRequest.setFastestInterval(50000); //5 secs
                break;
            case "default":
                mLocationRequest.setInterval(20000); //20 secs
                mLocationRequest.setFastestInterval(10000); //10 secs
                mLocationRequest.setSmallestDisplacement(10f); //10 m
                break;
            case "lowFrequency":
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


    private void updateFirebaseData(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        firebaseDatabaseHelper.updateOnlineUserLocation(lat, lon);
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
        //open up MapFragment
        Intent resultIntent = new Intent(this, MainActivity.class);
//        //TODO is this line still needed?
//        resultIntent.putExtra("isShouldLaunchMapFragment", true);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

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
        mNetworkStateReceiver = new NetworkStateReceiver();
        registerReceiver(
                mNetworkStateReceiver, intentFilterNetwork);
    }

    private void cleanupNotifications() {
        //cleanup unneeded notifications
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
