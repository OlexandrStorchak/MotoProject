package com.example.alex.motoproject;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.text.DateFormat;
import java.util.Date;

public class LocationListenerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String LOG_TAG = "LocationListenerService";

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Location mCurrentLocation;
    String mLastUpdateTime;
    boolean mRequestingLocationUpdates = true;

    public LocationListenerService() {
        // Required empty public constructor
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        //TODO: prompt user to change these settings, they`re useless now
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());

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
        Log.d(LOG_TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //TODO: remove try/catch and implement asking for permissions
        if (checkLocationPermission()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }

        if (mLastLocation != null) {
            Log.d("LocationListenService", "Location is " + mLastLocation.getLatitude());
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    protected void startLocationUpdates() {
        if (checkLocationPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateFirebaseData();
    }

    private void updateFirebaseData() {
//        mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
//        mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
//        mLastUpdateTimeTextView.setText(mLastUpdateTime);
        Log.d(LOG_TAG, "Lat " + mCurrentLocation.getLatitude() +
                " Lon " + mCurrentLocation.getLongitude() +
                " Time " + mLastUpdateTime);
        //TODO: delete this line, only for debugging purposes
        stopSelf();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
