package com.example.alex.motoproject.services;

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
import android.widget.Toast;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.MainActivity;
import com.example.alex.motoproject.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

/**
 * The Service that listens for location changes and sends them to Firebase
 */
public class LocationListenerService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    //TODO: do something if there`s no Internet or GPS connection
    private static final String LOG_TAG = "LocationListenerService";
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    String mRequestFrequency = "default";
    private DatabaseReference mDatabase;

    public LocationListenerService() {
        // Required empty public constructor
    }

    @Override
    public void onCreate() {
        ((App) this.getApplication()).setIsLocationListenerServiceOn(true);
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

        mDatabase = FirebaseDatabase.getInstance().getReference();

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        Log.d(LOG_TAG, "onDestroy");
        ((App) this.getApplication()).setIsLocationListenerServiceOn(false);
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
                intent.getExtras().getBoolean("isShouldStopService")) {
            Log.d(LOG_TAG, "onStartCommand with action stop service");
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                mCurrentLocation = mLastLocation;
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            }
        }
        startLocationUpdates();

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

    protected void startLocationUpdates() {
        //handle unexpected permission absence
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this);
        }
    }

    private void updateFirebaseData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            DatabaseReference userLocationReference =
                    mDatabase.child("location").child(uid);
            Double lat = mCurrentLocation.getLatitude();
            Double lng = mCurrentLocation.getLongitude();
            String updateTime = mLastUpdateTime;

            userLocationReference.child("lat").setValue(lat);
            userLocationReference.child("lng").setValue(lng);
            userLocationReference.child("updateTime").setValue(updateTime);
        }

        //TODO: delete this, only for testing purposes
        Toast.makeText(this, "Lat " + mCurrentLocation.getLatitude() +
                " Lon " + mCurrentLocation.getLongitude() +
                " Time " + mLastUpdateTime, Toast.LENGTH_LONG).show();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    private void createNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("MotoProject")
                        .setContentText("Місцезнаходження відстежується.")
                        .setShowWhen(false);

        //create pending intent used when tapping on the app notification
        //open up MapFragment
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

        //create pending intent user when tapping on big notification`s button
        //finish this service
        Intent stopSelfIntent = new Intent(this, LocationListenerService.class);
        stopSelfIntent.putExtra("isShouldStopService", true);
        //TODO: make a better logic for service killing
        PendingIntent StopSelfPendingIntent =
                PendingIntent.getService(
                        this,
                        0,
                        stopSelfIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.addAction(R.drawable.ic_clear_gray_24dp, "Прибрати мене з мапи", StopSelfPendingIntent);

        // set an ID for the notification
        int mNotificationId = 1;
        // send notification
        startForeground(mNotificationId, mBuilder.build());
    }
}
