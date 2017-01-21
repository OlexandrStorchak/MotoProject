package com.example.alex.motoproject;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    boolean mRequestingLocationUpdates = true;
    String mRequestFrequency = "default";

    public LocationListenerService() {
        // Required empty public constructor
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        // Create an instance of GoogleAPIClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        createNotification();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    //The service is not designed for binding so I restricted it from binding
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras() != null &&
                intent.getExtras().getBoolean("isShouldStopService")) {
            Log.d(LOG_TAG, "onStartCommand with action stop service");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //TODO: delete this line on push
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateFirebaseData();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

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
                break;
            case "lowFrequency":
                mLocationRequest.setInterval(30000); //30 secs
                mLocationRequest.setFastestInterval(20000); //20 secs
                break;
        }
        return mLocationRequest;
    }

    protected void startLocationUpdates() {
        if (checkLocationPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this);
        }
    }

    private void updateFirebaseData() {
        //TODO: push data to the server
        Log.d(LOG_TAG, "Lat " + mCurrentLocation.getLatitude() +
                " Lon " + mCurrentLocation.getLongitude() +
                " Time " + mLastUpdateTime);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_map_black_48dp)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!")
                        .setShowWhen(false);

        //TODO: open MapFragment
        //create pending intent used when tapping on the app notification
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("isShouldLaunchMapFragment", true);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        Intent stopSelfIntent = new Intent(this, LocationListenerService.class);
//        stopSelfIntent.setAction(ACTION_STOP_SERVICE);
        stopSelfIntent.putExtra("isShouldStopService", true);
        PendingIntent StopSelfPendingIntent =
                PendingIntent.getService(
                        this,
                        0,
                        stopSelfIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.addAction(R.drawable.ic_menu_gallery, "Астанавітєсь", StopSelfPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 1;
// send notification
        startForeground(mNotificationId, mBuilder.build());
    }
}
